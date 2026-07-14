package by.vsdev.posterminal.demo.network

import by.vsdev.posterminal.demo.dto.AckRequest
import by.vsdev.posterminal.demo.dto.HeartbeatRequest
import by.vsdev.posterminal.demo.dto.NewCommandRequest
import by.vsdev.posterminal.demo.dto.RegisterRequest
import by.vsdev.posterminal.demo.model.Device
import by.vsdev.posterminal.demo.model.DeviceCommand
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Thrown when a device-scoped call returns 404 — the backend no longer knows this device. */
class DeviceNotFoundException(val deviceId: String) :
    RuntimeException("Device not found on backend: $deviceId")

/**
 * Single HTTP client to the backend. Reused by the Android agent and the web admin:
 * the shared `:core` compiles for android/jvm (OkHttp) and js (fetch).
 *
 * [baseUrl] e.g. `https://pos-terminal-kmp-demo.onrender.com`. [apiKey] is an optional static key
 * (TODO for prod).
 */
class PosApiClient(
    baseUrl: String,
    private val apiKey: String? = null,
    private val client: HttpClient = defaultClient(),
) {
    private val base: String = baseUrl.trimEnd('/')

    suspend fun register(request: RegisterRequest): Device =
        client.post("$base/devices/register") {
            jsonBody(request)
        }.body()

    suspend fun heartbeat(deviceId: String, request: HeartbeatRequest): Device =
        client.post("$base/devices/$deviceId/heartbeat") {
            jsonBody(request)
        }.requireFound(deviceId).body()

    suspend fun getCommands(deviceId: String): List<DeviceCommand> =
        client.get("$base/devices/$deviceId/commands") {
            auth()
        }.requireFound(deviceId).body()

    suspend fun ackCommand(deviceId: String, commandId: String, request: AckRequest = AckRequest()) {
        client.post("$base/devices/$deviceId/commands/$commandId/ack") {
            jsonBody(request)
        }.requireFound(deviceId)
    }

    suspend fun postCommand(deviceId: String, request: NewCommandRequest): DeviceCommand =
        client.post("$base/devices/$deviceId/commands") {
            jsonBody(request)
        }.body()

    suspend fun listDevices(): List<Device> =
        client.get("$base/devices") {
            auth()
        }.body()

    /** Removes the device from the backend (Wipe / Logout). */
    suspend fun deleteDevice(deviceId: String) {
        client.delete("$base/devices/$deviceId") { auth() }
    }

    fun close() = client.close()

    private fun HttpResponse.requireFound(deviceId: String): HttpResponse {
        if (status == HttpStatusCode.NotFound) throw DeviceNotFoundException(deviceId)
        return this
    }

    private fun io.ktor.client.request.HttpRequestBuilder.auth() {
        apiKey?.let { header("X-Api-Key", it) }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.jsonBody(body: Any) {
        auth()
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    companion object {
        val json: Json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
        }

        fun defaultClient(): HttpClient = HttpClient(httpClientEngineFactory) {
            install(ContentNegotiation) { json(json) }
        }
    }
}
