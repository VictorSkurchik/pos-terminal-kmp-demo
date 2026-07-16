package by.vsdev.posterminal.demo.core.data.platform

import android.os.Build

/** The device hardware model string. Behind an interface so tests don't touch `android.os.Build`. */
interface DeviceInfoProvider {
    val model: String
}

class AndroidDeviceInfoProvider : DeviceInfoProvider {
    override val model: String get() = Build.MODEL
}

/** Epoch-millis clock. Injectable so heartbeat timestamps are deterministic under test. */
interface TimeProvider {
    fun nowMillis(): Long
}

class SystemTimeProvider : TimeProvider {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
