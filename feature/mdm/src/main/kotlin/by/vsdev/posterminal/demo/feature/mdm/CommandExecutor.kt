package by.vsdev.posterminal.demo.feature.mdm

import android.app.admin.DevicePolicyManager
import android.content.Context
import by.vsdev.posterminal.demo.core.data.prefs.SettingsRepository
import by.vsdev.posterminal.demo.feature.mdm.admin.PosDeviceAdminReceiver
import by.vsdev.posterminal.demo.model.CommandType
import by.vsdev.posterminal.demo.model.DeviceCommand

/**
 * Executor of remote MDM commands. LOCK and KIOSK use real Android APIs; SHOW_MESSAGE and
 * RESTRICT_APP affect only the POS app. (WIPE is no longer a command — the admin deletes the
 * device via DELETE /devices/{id}, and the agent un-enrolls on the resulting 404.)
 */
class CommandExecutor(
    private val context: Context,
    private val settings: SettingsRepository,
    private val controller: MdmController,
) {
    private val dpm: DevicePolicyManager
        get() = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    /** @return true if the command can be acknowledged (ack). */
    suspend fun execute(command: DeviceCommand): Boolean {
        return when (command.type) {
            CommandType.LOCK -> {
                val admin = PosDeviceAdminReceiver.componentName(context)
                if (dpm.isAdminActive(admin)) {
                    dpm.lockNow()
                } else {
                    controller.showMessage("Cannot LOCK: grant Device Admin first")
                }
                true
            }

            // Real screen pinning. KIOSK_ON also arms the idle screensaver (see AppNavHost, which
            // observes MdmController.kioskActive and navigates to the Offer screen after 5s idle).
            CommandType.KIOSK_ON -> { controller.startKiosk(); true }
            CommandType.KIOSK_OFF -> { controller.stopKiosk(); true }

            CommandType.SHOW_MESSAGE -> {
                controller.showMessage(command.payload ?: "Message from admin")
                true
            }

            // Emulation: payload "off" removes the restriction, otherwise it blocks the "Pay" button.
            CommandType.RESTRICT_APP -> {
                val restrict = !command.payload.equals("off", ignoreCase = true)
                settings.setRestrictApp(restrict)
                true
            }
        }
    }
}
