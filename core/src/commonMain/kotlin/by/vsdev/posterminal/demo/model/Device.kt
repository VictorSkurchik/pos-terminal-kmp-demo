package by.vsdev.posterminal.demo.model

import kotlinx.serialization.Serializable

@Serializable
enum class DeviceStatus { ONLINE, OFFLINE, LOCKED, KIOSK }

/**
 * A device managed by MDM.
 * [lastSeenAt] is the epoch millis of the last heartbeat (Long, to avoid pulling in kotlinx-datetime).
 */
@Serializable
data class Device(
    val id: String,
    val name: String,
    val model: String? = null,
    val lastSeenAt: Long,
    val status: DeviceStatus,
    val batteryLevel: Int? = null,
    val enrollmentToken: String? = null,
    val kioskActive: Boolean = false,
    val restrictPayment: Boolean = false,
)
