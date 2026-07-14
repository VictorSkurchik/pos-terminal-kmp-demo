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
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Single HTTP client to the backend. Reused by the Android agent and the web admin:
 * the shared `:core` compiles for android/jvm (OkHttp) and js (fetch).
 *
 * [baseUrl] e.g. `https://pos-mdm-backend.onrender.com`. [apiKey] is an optional static key
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
        }.body()

    suspend fun getCommands(deviceId: String): List<DeviceCommand> =
        client.get("$base/devices/$deviceId/commands") {
            auth()
        }.body()

    suspend fun ackCommand(deviceId: String, commandId: String, request: AckRequest = AckRequest()) {
        client.post("$base/devices/$deviceId/commands/$commandId/ack") {
            jsonBody(request)
        }
    }

    suspend fun postCommand(deviceId: String, request: NewCommandRequest): DeviceCommand =
        client.post("$base/devices/$deviceId/commands") {
            jsonBody(request)
        }.body()

    suspend fun listDevices(): List<Device> =
        client.get("$base/devices") {
            auth()
        }.body()

    fun close() = client.close()

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
