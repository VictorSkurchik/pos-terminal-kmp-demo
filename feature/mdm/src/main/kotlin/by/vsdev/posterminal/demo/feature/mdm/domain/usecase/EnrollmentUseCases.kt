package by.vsdev.posterminal.demo.feature.mdm.domain.usecase

import by.vsdev.posterminal.demo.domain.result.AppResult
import by.vsdev.posterminal.demo.domain.result.DomainError
import by.vsdev.posterminal.demo.domain.result.onSuccess
import by.vsdev.posterminal.demo.feature.mdm.domain.model.Device
import by.vsdev.posterminal.demo.feature.mdm.domain.repository.DeviceRepository
import by.vsdev.posterminal.demo.feature.mdm.domain.repository.SettingsRepository
import by.vsdev.posterminal.demo.feature.mdm.domain.service.EnrollmentTokenParser
import by.vsdev.posterminal.demo.feature.mdm.domain.service.KioskController
import by.vsdev.posterminal.demo.feature.mdm.domain.service.MdmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** Streams whether this terminal is currently enrolled. */
class ObserveEnrollmentUseCase(private val settings: SettingsRepository) {
    operator fun invoke(): Flow<Boolean> = settings.enrolled
}

/** Streams the live kiosk (screen-pinning) state. */
class ObserveKioskStateUseCase(private val kiosk: KioskController) {
    operator fun invoke(): StateFlow<Boolean> = kiosk.kioskActive
}

/** Manual enrollment against the currently configured backend. */
class EnrollDeviceUseCase(
    private val device: DeviceRepository,
    private val settings: SettingsRepository,
    private val scheduler: MdmScheduler,
) {
    suspend operator fun invoke(name: String): AppResult<Device> {
        settings.setDeviceName(name)
        return device.register().onSuccess {
            settings.setEnrolled(enrolled = true, name = name)
            scheduler.schedulePeriodic()
        }
    }
}

/** Outcome of a QR enrollment, distinguishing a bad payload from a backend failure. */
sealed interface EnrollmentResult {
    data class Success(val device: Device) : EnrollmentResult

    data object InvalidToken : EnrollmentResult

    data class Failed(val error: DomainError) : EnrollmentResult
}

/** QR enrollment: parse the token, point at its backend, register, then persist enrollment. */
class EnrollWithTokenUseCase(
    private val device: DeviceRepository,
    private val settings: SettingsRepository,
    private val scheduler: MdmScheduler,
    private val parser: EnrollmentTokenParser,
) {
    suspend operator fun invoke(name: String, payload: String): EnrollmentResult {
        val token = parser.parse(payload) ?: return EnrollmentResult.InvalidToken
        settings.setServerUrl(token.serverUrl)
        settings.setDeviceName(name)
        return when (val result = device.register(enrollmentToken = token.token)) {
            is AppResult.Success -> {
                settings.setEnrolled(enrolled = true, name = name, serverUrl = token.serverUrl)
                scheduler.schedulePeriodic()
                EnrollmentResult.Success(result.data)
            }

            is AppResult.Failure -> EnrollmentResult.Failed(result.error)
        }
    }
}

/** Requests an immediate background sync. */
class SyncNowUseCase(private val scheduler: MdmScheduler) {
    operator fun invoke() = scheduler.triggerOnce()
}

/** Logout / factory reset: cancel the agent, delete from backend, clear local enrollment. */
class LogoutUseCase(private val device: DeviceRepository, private val scheduler: MdmScheduler) {
    suspend operator fun invoke(): AppResult<Unit> {
        scheduler.cancel()
        return device.logout()
    }
}
