package by.vsdev.posterminal.demo.feature.mdm.admin

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context

/** Device Admin receiver — registered with the system, grants the right to call lockNow(). */
class PosDeviceAdminReceiver : DeviceAdminReceiver() {
    companion object {
        fun componentName(context: Context): ComponentName =
            ComponentName(context.applicationContext, PosDeviceAdminReceiver::class.java)
    }
}
