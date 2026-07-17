package by.vsdev.posterminal.demo.feature.mdm.data

import by.vsdev.posterminal.demo.feature.mdm.domain.model.CommandStatus
import by.vsdev.posterminal.demo.feature.mdm.domain.model.CommandType
import by.vsdev.posterminal.demo.feature.mdm.domain.model.Device
import by.vsdev.posterminal.demo.feature.mdm.domain.model.DeviceCommand
import by.vsdev.posterminal.demo.feature.mdm.domain.model.DeviceStatus
import by.vsdev.posterminal.demo.model.CommandStatus as WireCommandStatus
import by.vsdev.posterminal.demo.model.CommandType as WireCommandType
import by.vsdev.posterminal.demo.model.Device as WireDevice
import by.vsdev.posterminal.demo.model.DeviceCommand as WireCommand
import by.vsdev.posterminal.demo.model.DeviceStatus as WireDeviceStatus

/**
 * Wire (`:core` @Serializable) → domain mappers. Deliberately drops transport-only fields such as
 * `enrollmentToken`, keeping the domain model free of backend concerns.
 */
fun WireDevice.toDomain(): Device = Device(
    id = id,
    name = name,
    model = model,
    lastSeenAt = lastSeenAt,
    status = status.toDomain(),
    batteryLevel = batteryLevel,
    kioskActive = kioskActive,
)

fun WireCommand.toDomain(): DeviceCommand = DeviceCommand(
    id = id,
    deviceId = deviceId,
    type = type.toDomain(),
    payload = payload,
    status = status.toDomain(),
    createdAt = createdAt,
)

fun WireDeviceStatus.toDomain(): DeviceStatus = when (this) {
    WireDeviceStatus.ONLINE -> DeviceStatus.ONLINE
    WireDeviceStatus.OFFLINE -> DeviceStatus.OFFLINE
    WireDeviceStatus.LOCKED -> DeviceStatus.LOCKED
    WireDeviceStatus.KIOSK -> DeviceStatus.KIOSK
}

fun WireCommandType.toDomain(): CommandType = when (this) {
    WireCommandType.LOCK -> CommandType.LOCK
    WireCommandType.KIOSK_ON -> CommandType.KIOSK_ON
    WireCommandType.KIOSK_OFF -> CommandType.KIOSK_OFF
    WireCommandType.SHOW_MESSAGE -> CommandType.SHOW_MESSAGE
    WireCommandType.WIPE -> CommandType.WIPE
}

fun WireCommandStatus.toDomain(): CommandStatus = when (this) {
    WireCommandStatus.PENDING -> CommandStatus.PENDING
    WireCommandStatus.DELIVERED -> CommandStatus.DELIVERED
    WireCommandStatus.DONE -> CommandStatus.DONE
}
