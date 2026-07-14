package by.vsdev.posterminal.demo.core.data.repo

import by.vsdev.posterminal.demo.core.data.prefs.SettingsRepository
import by.vsdev.posterminal.demo.dto.AckRequest
import by.vsdev.posterminal.demo.dto.HeartbeatRequest
import by.vsdev.posterminal.demo.dto.RegisterRequest
import by.vsdev.posterminal.demo.model.Device
import by.vsdev.posterminal.demo.model.DeviceCommand
import by.vsdev.posterminal.demo.network.PosApiClient
import kotlinx.coroutines.flow.first

/** Wrapper over the shared [PosApiClient] for the Android agent (register/heartbeat/poll/ack). */
class RemoteRepository(
    private val api: PosApiClient,
    private val settings: SettingsRepository,
) {
    suspend fun registerThisDevice(enrollmentToken: String? = null): Device {
        val id = settings.getOrCreateDeviceId()
        val name = settings.deviceName.first()
        return api.register(
            RegisterRequest(
                deviceId = id,
                name = name,
                model = android.os.Build.MODEL,
                enrollmentToken = enrollmentToken,
            ),
        )
    }

    suspend fun heartbeat(batteryLevel: Int?): Device {
        val id = settings.getOrCreateDeviceId()
        return api.heartbeat(id, HeartbeatRequest(timestamp = System.currentTimeMillis(), batteryLevel = batteryLevel))
    }

    suspend fun fetchCommands(): List<DeviceCommand> {
        val id = settings.getOrCreateDeviceId()
        return api.getCommands(id)
    }

    suspend fun ack(commandId: String) {
        val id = settings.getOrCreateDeviceId()
        api.ackCommand(id, commandId, AckRequest())
    }
}
