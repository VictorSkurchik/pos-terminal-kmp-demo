package by.vsdev.posterminal.demo.core.data.repo

import by.vsdev.posterminal.demo.core.data.prefs.SettingsRepository
import by.vsdev.posterminal.demo.dto.AckRequest
import by.vsdev.posterminal.demo.dto.HeartbeatRequest
import by.vsdev.posterminal.demo.dto.RegisterRequest
import by.vsdev.posterminal.demo.model.Device
import by.vsdev.posterminal.demo.model.DeviceCommand
import by.vsdev.posterminal.demo.network.DeviceNotFoundException
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
        val kiosk = settings.kioskActive.first()
        val restrict = settings.restrictApp.first()
        return selfHealOn404 {
            api.heartbeat(
                id,
                HeartbeatRequest(
                    timestamp = System.currentTimeMillis(),
                    batteryLevel = batteryLevel,
                    kioskActive = kiosk,
                    restrictPayment = restrict,
                ),
            )
        }
    }

    suspend fun fetchCommands(): List<DeviceCommand> {
        val id = settings.getOrCreateDeviceId()
        return selfHealOn404 { api.getCommands(id) }
    }

    suspend fun ack(commandId: String) {
        val id = settings.getOrCreateDeviceId()
        runCatching { api.ackCommand(id, commandId, AckRequest()) }
    }

    /** Intentional reset (Factory reset / WIPE command): delete from backend + clear local state. */
    suspend fun logout() {
        val id = settings.getOrCreateDeviceId()
        runCatching { api.deleteDevice(id) }
        settings.clearEnrollment()
    }

    /**
     * If the backend reports the device is gone (404) — e.g. the free-tier DB was reset when the
     * instance spun down — re-create this device on the backend and retry once, instead of logging
     * the terminal out. Intentional removal is handled by [logout] / the WIPE command.
     */
    private suspend fun <T> selfHealOn404(block: suspend () -> T): T {
        return try {
            block()
        } catch (e: DeviceNotFoundException) {
            registerThisDevice()
            block()
        }
    }
}
