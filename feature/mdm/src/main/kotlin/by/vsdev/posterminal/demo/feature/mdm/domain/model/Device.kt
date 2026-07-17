package by.vsdev.posterminal.demo.feature.mdm.domain.model

enum class DeviceStatus { ONLINE, OFFLINE, LOCKED, KIOSK }

/**
 * A device managed by MDM, as the app understands it. Unlike the wire DTO this carries no
 * transport-only fields (e.g. the enrollment token) — those stay in the data layer.
 * [lastSeenAt] is epoch millis.
 */
data class Device(
    val id: String,
    val name: String,
    val model: String? = null,
    val lastSeenAt: Long,
    val status: DeviceStatus,
    val batteryLevel: Int? = null,
    val kioskActive: Boolean = false,
)
