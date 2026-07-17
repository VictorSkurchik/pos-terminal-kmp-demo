package by.vsdev.posterminal.demo.feature.mdm.fakes

import by.vsdev.posterminal.demo.domain.policy.DevicePolicy
import by.vsdev.posterminal.demo.domain.result.AppResult
import by.vsdev.posterminal.demo.feature.mdm.domain.model.Device
import by.vsdev.posterminal.demo.feature.mdm.domain.model.DeviceCommand
import by.vsdev.posterminal.demo.feature.mdm.domain.model.DeviceStatus
import by.vsdev.posterminal.demo.feature.mdm.domain.model.EnrollmentToken
import by.vsdev.posterminal.demo.feature.mdm.domain.repository.DeviceRepository
import by.vsdev.posterminal.demo.feature.mdm.domain.repository.SettingsRepository
import by.vsdev.posterminal.demo.feature.mdm.domain.service.EnrollmentTokenParser
import by.vsdev.posterminal.demo.feature.mdm.domain.service.MdmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(var serverUrlValue: String = "https://example.test") : SettingsRepository {
    val enrolledState = MutableStateFlow(false)
    val nameState = MutableStateFlow("POS Terminal")
    var cleared = false

    override val enrolled: Flow<Boolean> = enrolledState
    override val deviceName: Flow<String> = nameState

    override suspend fun deviceId(): String = "pos-test01"
    override suspend fun serverUrl(): String = serverUrlValue
    override suspend fun setServerUrl(url: String) {
        serverUrlValue = url
    }
    override suspend fun setEnrolled(enrolled: Boolean, name: String?, serverUrl: String?) {
        enrolledState.value = enrolled
        name?.let { nameState.value = it }
        serverUrl?.let { serverUrlValue = it }
    }
    override suspend fun setDeviceName(name: String) {
        nameState.value = name
    }
    override suspend fun clearEnrollment() {
        cleared = true
        enrolledState.value = false
    }
}

class FakeDevicePolicy : DevicePolicy {
    val restrictState = MutableStateFlow(false)
    val kioskState = MutableStateFlow(false)
    var reset = false

    override val restrictPayment: Flow<Boolean> = restrictState
    override val kioskActive: Flow<Boolean> = kioskState

    override suspend fun setRestrictPayment(value: Boolean) {
        restrictState.value = value
    }
    override suspend fun setKioskActive(value: Boolean) {
        kioskState.value = value
    }
    override suspend fun reset() {
        reset = true
        restrictState.value = false
        kioskState.value = false
    }
}

class FakeDeviceRepository : DeviceRepository {
    var registerResult: AppResult<Device> = AppResult.Success(sampleDevice())
    var registerCount = 0

    override suspend fun register(enrollmentToken: String?): AppResult<Device> {
        registerCount++
        return registerResult
    }
    override suspend fun heartbeat(batteryLevel: Int?): AppResult<Device> = registerResult
    override suspend fun fetchCommands(): AppResult<List<DeviceCommand>> = AppResult.Success(emptyList())
    override suspend fun ack(commandId: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun logout(): AppResult<Unit> = AppResult.Success(Unit)
}

class RecordingScheduler : MdmScheduler {
    var scheduled = false
    var cancelled = false
    var triggered = false
    override fun schedulePeriodic() {
        scheduled = true
    }
    override fun triggerOnce() {
        triggered = true
    }
    override fun cancel() {
        cancelled = true
    }
}

class FakeTokenParser(private val token: EnrollmentToken?) : EnrollmentTokenParser {
    override fun parse(payload: String): EnrollmentToken? = token
}

fun sampleDevice(id: String = "pos-test01") = Device(
    id = id,
    name = "POS Terminal",
    lastSeenAt = 0L,
    status = DeviceStatus.ONLINE,
)
