package by.vsdev.posterminal.demo.data

import by.vsdev.posterminal.demo.db.AppDatabase
import by.vsdev.posterminal.demo.db.CommandEntity
import by.vsdev.posterminal.demo.db.DeviceEntity
import by.vsdev.posterminal.demo.dto.AckRequest
import by.vsdev.posterminal.demo.dto.HeartbeatRequest
import by.vsdev.posterminal.demo.dto.NewCommandRequest
import by.vsdev.posterminal.demo.dto.RegisterRequest
import by.vsdev.posterminal.demo.model.CommandStatus
import by.vsdev.posterminal.demo.model.CommandType
import by.vsdev.posterminal.demo.model.Device
import by.vsdev.posterminal.demo.model.DeviceCommand
import by.vsdev.posterminal.demo.model.DeviceStatus
import java.util.UUID

/** Single source of truth for devices and the command queue. */
class DeviceRepository(db: AppDatabase) {
    private val devices = db.deviceDao()
    private val commands = db.commandDao()

    suspend fun register(request: RegisterRequest, now: Long): Device {
        val entity = DeviceEntity(
            id = request.deviceId,
            name = request.name,
            model = request.model,
            lastSeenAt = now,
            status = DeviceStatus.ONLINE.name,
            batteryLevel = null,
            enrollmentToken = request.enrollmentToken,
        )
        devices.upsert(entity)
        return entity.toModel()
    }

    suspend fun heartbeat(deviceId: String, request: HeartbeatRequest): Device? {
        val existing = devices.getById(deviceId) ?: return null
        val updated = existing.copy(
            lastSeenAt = request.timestamp,
            batteryLevel = request.batteryLevel ?: existing.batteryLevel,
            status = DeviceStatus.ONLINE.name,
        )
        devices.upsert(updated)
        return updated.toModel()
    }

    suspend fun listDevices(): List<Device> = devices.getAll().map { it.toModel() }

    suspend fun exists(deviceId: String): Boolean = devices.getById(deviceId) != null

    /** Device pulls unfinished commands; PENDING ones are moved to DELIVERED. */
    suspend fun pendingCommands(deviceId: String): List<DeviceCommand> {
        val pending = commands.getPending(deviceId)
        pending.filter { it.status == CommandStatus.PENDING.name }
            .forEach { commands.updateStatus(it.id, CommandStatus.DELIVERED.name) }
        return pending.map { it.toModel() }
    }

    suspend fun enqueue(deviceId: String, request: NewCommandRequest, now: Long): DeviceCommand {
        val entity = CommandEntity(
            id = UUID.randomUUID().toString(),
            deviceId = deviceId,
            type = request.type.name,
            payload = request.payload,
            status = CommandStatus.PENDING.name,
            createdAt = now,
        )
        commands.insert(entity)
        return entity.toModel()
    }

    /** @return true if the command was found and acknowledged. */
    suspend fun ack(commandId: String, request: AckRequest): Boolean {
        commands.getById(commandId) ?: return false
        commands.updateStatus(commandId, request.status.name)
        return true
    }

    /** Removes the device and its command queue. @return true if the device existed. */
    suspend fun deleteDevice(deviceId: String): Boolean {
        commands.deleteForDevice(deviceId)
        return devices.delete(deviceId) > 0
    }
}

private fun DeviceEntity.toModel() = Device(
    id = id,
    name = name,
    model = model,
    lastSeenAt = lastSeenAt,
    status = DeviceStatus.valueOf(status),
    batteryLevel = batteryLevel,
    enrollmentToken = enrollmentToken,
)

private fun CommandEntity.toModel() = DeviceCommand(
    id = id,
    deviceId = deviceId,
    type = CommandType.valueOf(type),
    payload = payload,
    status = CommandStatus.valueOf(status),
    createdAt = createdAt,
)
