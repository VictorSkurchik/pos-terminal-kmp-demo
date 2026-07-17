package by.vsdev.posterminal.demo.feature.mdm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import by.vsdev.posterminal.demo.domain.model.CommandType
import by.vsdev.posterminal.demo.domain.model.DeviceCommand
import by.vsdev.posterminal.demo.domain.policy.DevicePolicy
import by.vsdev.posterminal.demo.domain.service.DeviceAdminRepository
import by.vsdev.posterminal.demo.domain.service.MdmCommandExecutor
import by.vsdev.posterminal.demo.domain.usecase.mdm.LogoutUseCase

/**
 * Executes remote MDM commands. LOCK/KIOSK use real Android APIs; SHOW_MESSAGE and RESTRICT_APP
 * affect only the POS app. Kiosk/restrict state is persisted so it can be reported to the admin via
 * heartbeat. WIPE is the admin-initiated reset. Every command also surfaces a notification, so the
 * service and the WorkManager fallback behave identically.
 */
class CommandExecutor(
    private val context: Context,
    private val policy: DevicePolicy,
    private val controller: MdmController,
    private val deviceAdmin: DeviceAdminRepository,
    private val logout: LogoutUseCase,
) : MdmCommandExecutor {

    override suspend fun execute(command: DeviceCommand) {
        notify(command)
        when (command.type) {
            CommandType.LOCK ->
                if (deviceAdmin.isAdminActive()) {
                    deviceAdmin.lockNow()
                } else {
                    controller.showMessage("Cannot LOCK: grant Device Admin first")
                }

            CommandType.KIOSK_ON -> {
                policy.setKioskActive(true)
                controller.startKiosk()
            }

            CommandType.KIOSK_OFF -> {
                policy.setKioskActive(false)
                controller.stopKiosk()
            }

            CommandType.SHOW_MESSAGE ->
                controller.showMessage(command.payload ?: "Message from admin")

            // Emulation: payload "off" removes the restriction, otherwise it blocks the "Pay" button.
            CommandType.RESTRICT_APP ->
                policy.setRestrictPayment(!command.payload.equals("off", ignoreCase = true))

            // Admin reset: un-enroll + delete from backend → the app returns to Registration.
            CommandType.WIPE -> logout()
        }
    }

    private fun notify(command: DeviceCommand) {
        ensureChannel()
        val notification = NotificationCompat.Builder(context, CMD_CHANNEL)
            .setContentTitle("MDM command: ${command.type.name}")
            .setContentText(command.payload ?: command.type.name)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setAutoCancel(true)
            .build()
        // POST_NOTIFICATIONS may be denied — notify() then no-ops, execution continues.
        runCatching { NotificationManagerCompat.from(context).notify(command.id.hashCode(), notification) }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java) ?: return
            nm.createNotificationChannel(
                NotificationChannel(CMD_CHANNEL, "MDM commands", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
    }

    private companion object {
        const val CMD_CHANNEL = "mdm_commands"
    }
}
