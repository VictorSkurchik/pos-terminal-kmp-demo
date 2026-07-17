package by.vsdev.posterminal.demo.feature.mdm.data

import by.vsdev.posterminal.demo.domain.policy.DevicePolicy
import by.vsdev.posterminal.demo.domain.result.AppResult
import by.vsdev.posterminal.demo.dto.HeartbeatRequest
import by.vsdev.posterminal.demo.dto.RegisterRequest
import by.vsdev.posterminal.demo.feature.mdm.data.DeviceInfoProvider
import by.vsdev.posterminal.demo.feature.mdm.data.TimeProvider
import by.vsdev.posterminal.demo.feature.mdm.data.toDomain
import by.vsdev.posterminal.demo.feature.mdm.data.toDomainError
import by.vsdev.posterminal.demo.feature.mdm.domain.model.Device
import by.vsdev.posterminal.demo.feature.mdm.domain.model.DeviceCommand
import by.vsdev.posterminal.demo.feature.mdm.domain.repository.DeviceRepository
import by.vsdev.posterminal.demo.feature.mdm.domain.repository.SettingsRepository
import by.vsdev.posterminal.demo.network.DeviceNotFoundException
import by.vsdev.posterminal.demo.network.PosApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/**
 * Talks to the backend via [PosApi], mapping wire models to domain and transport exceptions to
 * [AppResult]/DomainError. If the backend forgets this device (404, e.g. the free-tier DB was
 * wiped) it self-heals by re-registering and retrying once, instead of logging the terminal out.
 */
class DeviceRepositoryImpl(
    private val api: PosApi,
    private val settings: SettingsRepository,
    private val policy: DevicePolicy,
    private val deviceInfo: DeviceInfoProvider,
    private val time: TimeProvider,
) : DeviceRepository {

    override suspend fun register(enrollmentToken: String?): AppResult<Device> = apiCall {
        api.register(
            RegisterRequest(
                deviceId = settings.deviceId(),
                name = settings.deviceName.first(),
                model = deviceInfo.model,
                enrollmentToken = enrollmentToken,
            ),
        ).toDomain()
    }

    override suspend fun heartbeat(batteryLevel: Int?): AppResult<Device> = apiCall {
        selfHealOn404 {
            api.heartbeat(
                settings.deviceId(),
                HeartbeatRequest(
                    timestamp = time.nowMillis(),
                    batteryLevel = batteryLevel,
                    kioskActive = policy.kioskActive.first(),
                ),
            ).toDomain()
        }
    }

    override suspend fun fetchCommands(): AppResult<List<DeviceCommand>> = apiCall {
        selfHealOn404 { api.getCommands(settings.deviceId()).map { it.toDomain() } }
    }

    override suspend fun ack(commandId: String): AppResult<Unit> = apiCall {
        selfHealOn404 { api.ackCommand(settings.deviceId(), commandId) }
    }

    override suspend fun logout(): AppResult<Unit> {
        // Intentional reset: clear local enrollment regardless of whether the backend delete succeeds.
        val result = apiCall { api.deleteDevice(settings.deviceId()) }
        settings.clearEnrollment()
        policy.reset()
        return result
    }

    @Suppress("TooGenericExceptionCaught") // Deliberate IO boundary: classify everything except cancellation.
    private inline fun <T> apiCall(block: () -> T): AppResult<T> =
        try {
            AppResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            AppResult.Failure(e.toDomainError())
        }

    @Suppress("SwallowedException") // 404 is the signal to self-heal, not an error to propagate.
    private suspend fun <T> selfHealOn404(block: suspend () -> T): T =
        try {
            block()
        } catch (e: DeviceNotFoundException) {
            // Never resurrect a device that was intentionally un-enrolled (WIPE / factory reset):
            // otherwise the ack right after a WIPE would 404 and silently re-register it.
            if (!settings.enrolled.first()) throw e
            api.register(
                RegisterRequest(
                    deviceId = settings.deviceId(),
                    name = settings.deviceName.first(),
                    model = deviceInfo.model,
                ),
            )
            block()
        }
}
