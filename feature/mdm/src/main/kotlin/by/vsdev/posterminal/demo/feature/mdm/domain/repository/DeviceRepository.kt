package by.vsdev.posterminal.demo.feature.mdm.domain.repository

import by.vsdev.posterminal.demo.domain.result.AppResult
import by.vsdev.posterminal.demo.feature.mdm.domain.model.Device
import by.vsdev.posterminal.demo.feature.mdm.domain.model.DeviceCommand

/** The device's view of the MDM backend: register, heartbeat, poll and acknowledge commands. */
interface DeviceRepository {
    suspend fun register(enrollmentToken: String? = null): AppResult<Device>

    suspend fun heartbeat(batteryLevel: Int?): AppResult<Device>

    suspend fun fetchCommands(): AppResult<List<DeviceCommand>>

    suspend fun ack(commandId: String): AppResult<Unit>

    /** Intentional reset (Factory reset / WIPE): delete from backend + clear local enrollment. */
    suspend fun logout(): AppResult<Unit>
}
