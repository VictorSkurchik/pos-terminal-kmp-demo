package by.vsdev.posterminal.demo.domain.policy

import kotlinx.coroutines.flow.Flow

/**
 * Cross-cutting device-policy flags set remotely by MDM commands and read by other features:
 * [restrictPayment] (written by mdm's command executor, read by POS to gate the Pay button and by
 * the heartbeat) and [kioskActive] (reported in the heartbeat). Kept in the shared kernel so neither
 * feature has to depend on the other; the concrete store is provided by the app.
 */
interface DevicePolicy {
    val restrictPayment: Flow<Boolean>
    val kioskActive: Flow<Boolean>

    suspend fun setRestrictPayment(value: Boolean)
    suspend fun setKioskActive(value: Boolean)

    /** Resets both flags (e.g. on WIPE / logout). */
    suspend fun reset()
}
