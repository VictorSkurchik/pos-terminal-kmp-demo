package by.vsdev.posterminal.demo.domain.policy

import kotlinx.coroutines.flow.Flow

/**
 * Cross-cutting device-policy state set remotely by MDM commands and read by other features:
 * [kioskActive] (reported in the heartbeat). Kept in the shared kernel so neither feature has to
 * depend on the other; the concrete store is provided by the app.
 */
interface DevicePolicy {
    val kioskActive: Flow<Boolean>

    suspend fun setKioskActive(value: Boolean)

    /** Resets policy state (e.g. on WIPE / logout). */
    suspend fun reset()
}
