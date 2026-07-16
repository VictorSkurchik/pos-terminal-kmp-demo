package by.vsdev.posterminal.demo.feature.mdm.admin

import android.app.admin.DevicePolicyManager
import android.content.Context
import by.vsdev.posterminal.demo.domain.service.DeviceAdminRepository

/** [DeviceAdminRepository] backed by [DevicePolicyManager] + [PosDeviceAdminReceiver]. */
class DeviceAdminRepositoryImpl(private val context: Context) : DeviceAdminRepository {

    private val dpm: DevicePolicyManager
        get() = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val admin get() = PosDeviceAdminReceiver.componentName(context)

    override fun isAdminActive(): Boolean = dpm.isAdminActive(admin)

    override fun lockNow() {
        if (dpm.isAdminActive(admin)) dpm.lockNow()
    }
}
