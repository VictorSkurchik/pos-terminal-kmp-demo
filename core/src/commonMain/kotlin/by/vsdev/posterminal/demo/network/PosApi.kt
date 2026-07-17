package by.vsdev.posterminal.demo.network

import by.vsdev.posterminal.demo.dto.AckRequest
import by.vsdev.posterminal.demo.dto.HeartbeatRequest
import by.vsdev.posterminal.demo.dto.NewCommandRequest
import by.vsdev.posterminal.demo.dto.RegisterRequest
import by.vsdev.posterminal.demo.model.Device
import by.vsdev.posterminal.demo.model.DeviceCommand

/**
 * Transport contract for the MDM backend. Returns wire models ([Device], [DeviceCommand]); the
 * Android data layer maps them to domain models. Behind an interface so tests can supply a fake
 * (or a Ktor MockEngine-backed [KtorPosApiClient]).
 */
interface PosApi {
    suspend fun register(request: RegisterRequest): Device

    suspend fun heartbeat(deviceId: String, request: HeartbeatRequest): Device

    suspend fun getCommands(deviceId: String): List<DeviceCommand>

    suspend fun ackCommand(deviceId: String, commandId: String, request: AckRequest = AckRequest())

    suspend fun postCommand(deviceId: String, request: NewCommandRequest): DeviceCommand

    suspend fun listDevices(): List<Device>

    suspend fun deleteDevice(deviceId: String)
}

/** Thrown when a device-scoped call returns 404 — the backend no longer knows this device. */
class DeviceNotFoundException(val deviceId: String) : RuntimeException("Device not found on backend: $deviceId")
