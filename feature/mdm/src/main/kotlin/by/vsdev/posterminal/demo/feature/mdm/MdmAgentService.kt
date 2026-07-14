package by.vsdev.posterminal.demo.feature.mdm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import by.vsdev.posterminal.demo.core.data.prefs.SettingsRepository
import by.vsdev.posterminal.demo.core.data.repo.RemoteRepository
import by.vsdev.posterminal.demo.model.DeviceCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Always-on MDM agent. Runs as a foreground service so command polling keeps working when the app
 * is backgrounded — otherwise Android freezes the process and blocks its network.
 *
 * Single command consumer: heartbeat → pull pending commands → surface (snackbar via [MdmController]
 * when the UI is visible, and a notification always) → execute → ack. Declared in :feature:mdm
 * because it IS the device-management agent; :app:androidApp only starts it.
 */
class MdmAgentService : Service(), KoinComponent {

    private val settings: SettingsRepository by inject()
    private val remote: RemoteRepository by inject()
    private val executor: CommandExecutor by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var polling = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureChannels()
        ServiceCompat.startForeground(
            this,
            STATUS_ID,
            statusNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            },
        )
        if (!polling) {
            polling = true
            scope.launch { pollLoop() }
        }
        return START_STICKY
    }

    private suspend fun pollLoop() {
        while (true) {
            runCatching {
                if (settings.enrolled.first()) {
                    remote.heartbeat(batteryLevel())
                    remote.fetchCommands().forEach { command ->
                        notifyCommand(command)     // notification (also visible when backgrounded)
                        executor.execute(command)  // LOCK / KIOSK / SHOW_MESSAGE / RESTRICT_APP / WIPE
                        remote.ack(command.id)
                    }
                }
            }
            delay(POLL_MS)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun batteryLevel(): Int? {
        val bm = applicationContext.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.takeIf { it in 0..100 }
    }

    private fun ensureChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java) ?: return
            nm.createNotificationChannel(
                NotificationChannel(STATUS_CHANNEL, "MDM agent", NotificationManager.IMPORTANCE_LOW),
            )
            nm.createNotificationChannel(
                NotificationChannel(CMD_CHANNEL, "MDM commands", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
    }

    private fun statusNotification(): Notification =
        NotificationCompat.Builder(this, STATUS_CHANNEL)
            .setContentTitle("POS MDM agent")
            .setContentText("Listening for remote commands")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .build()

    private fun notifyCommand(command: DeviceCommand) {
        val notification = NotificationCompat.Builder(this, CMD_CHANNEL)
            .setContentTitle("MDM command: ${command.type.name}")
            .setContentText(command.payload ?: command.type.name)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setAutoCancel(true)
            .build()
        // POST_NOTIFICATIONS may be denied — notify() then no-ops, the service keeps running.
        runCatching { NotificationManagerCompat.from(this).notify(command.id.hashCode(), notification) }
    }

    companion object {
        private const val STATUS_CHANNEL = "mdm_status"
        private const val CMD_CHANNEL = "mdm_commands"
        private const val STATUS_ID = 1001
        private const val POLL_MS = 4000L

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, MdmAgentService::class.java))
        }
    }
}
