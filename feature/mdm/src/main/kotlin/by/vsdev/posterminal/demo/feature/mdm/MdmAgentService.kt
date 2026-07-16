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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import by.vsdev.posterminal.demo.domain.repository.SettingsRepository
import by.vsdev.posterminal.demo.domain.result.AppResult
import by.vsdev.posterminal.demo.domain.usecase.mdm.SyncDeviceUseCase
import by.vsdev.posterminal.demo.domain.util.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Always-on MDM agent. Runs as a foreground service so command polling keeps working when the app
 * is backgrounded. Each cycle delegates to [SyncDeviceUseCase] (heartbeat → pull → execute → ack);
 * the notification per command lives in the executor, so this and the WorkManager fallback behave
 * identically. Failures are logged, never swallowed silently.
 */
class MdmAgentService : Service(), KoinComponent {

    private val settings: SettingsRepository by inject()
    private val sync: SyncDeviceUseCase by inject()
    private val dispatchers: DispatcherProvider by inject()

    private val scope by lazy { CoroutineScope(SupervisorJob() + dispatchers.io) }
    private var polling = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureStatusChannel()
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
        while (currentCoroutineContext().isActive) {
            if (settings.enrolled.first()) {
                when (val result = sync(batteryLevel())) {
                    is AppResult.Failure -> Log.w(TAG, "MDM sync cycle failed: ${result.error}")
                    is AppResult.Success -> Unit
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

    private fun ensureStatusChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java) ?: return
            nm.createNotificationChannel(
                NotificationChannel(STATUS_CHANNEL, "MDM agent", NotificationManager.IMPORTANCE_LOW),
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

    companion object {
        private const val TAG = "MdmAgentService"
        private const val STATUS_CHANNEL = "mdm_status"
        private const val STATUS_ID = 1001
        private const val POLL_MS = 4000L

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, MdmAgentService::class.java))
        }
    }
}
