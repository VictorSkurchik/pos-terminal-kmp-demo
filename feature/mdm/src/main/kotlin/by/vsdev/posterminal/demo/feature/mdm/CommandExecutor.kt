package by.vsdev.posterminal.demo.feature.mdm

import android.app.admin.DevicePolicyManager
import android.content.Context
import by.vsdev.posterminal.demo.core.data.prefs.SettingsRepository
import by.vsdev.posterminal.demo.core.data.repo.CartRepository
import by.vsdev.posterminal.demo.feature.mdm.admin.PosDeviceAdminReceiver
import by.vsdev.posterminal.demo.model.CommandType
import by.vsdev.posterminal.demo.model.DeviceCommand

/**
 * Executor of remote MDM commands. Some are backed by real Android APIs
 * (LOCK, KIOSK), some are emulated within the MVP scope (WIPE, RESTRICT_APP).
 */
class CommandExecutor(
    private val context: Context,
    private val settings: SettingsRepository,
    private val cart: CartRepository,
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

            CommandType.KIOSK_ON -> { controller.startKiosk(); true }
            CommandType.KIOSK_OFF -> { controller.stopKiosk(); true }

            CommandType.SHOW_MESSAGE -> {
                controller.showMessage(command.payload ?: "Message from admin")
                true
            }

            // Wipe emulation: clear local user data (cart) and reset policies.
            // NOT a real factory reset; enrollment is preserved so the device stays manageable.
            CommandType.WIPE -> {
                cart.clear()
                settings.setRestrictApp(false)
                controller.showMessage("Local data wiped (emulated)")
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
