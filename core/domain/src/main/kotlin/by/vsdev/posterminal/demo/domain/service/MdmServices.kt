package by.vsdev.posterminal.demo.domain.service

import by.vsdev.posterminal.demo.domain.model.DeviceCommand
import by.vsdev.posterminal.demo.domain.model.EnrollmentToken
import kotlinx.coroutines.flow.StateFlow

/** Schedules the background MDM sync (WorkManager on Android). */
interface MdmScheduler {
    fun schedulePeriodic()

    fun triggerOnce()

    fun cancel()
}

/** Executes a single MDM command on the device (LOCK, KIOSK, SHOW_MESSAGE, RESTRICT_APP, WIPE). */
interface MdmCommandExecutor {
    suspend fun execute(command: DeviceCommand)
}

/** Live kiosk (screen-pinning) state, observed by the UI to drive the attract loop. */
interface KioskController {
    val kioskActive: StateFlow<Boolean>
}

/** Device Admin (DevicePolicyManager) surface: current status and the real lock. */
interface DeviceAdminRepository {
    fun isAdminActive(): Boolean

    fun lockNow()
}

/** Decodes a scanned QR payload into an [EnrollmentToken], or null if it is not a valid token. */
interface EnrollmentTokenParser {
    fun parse(payload: String): EnrollmentToken?
}
