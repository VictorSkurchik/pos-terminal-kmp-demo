package by.vsdev.posterminal.demo.network

import by.vsdev.posterminal.demo.dto.AckRequest
import by.vsdev.posterminal.demo.dto.HeartbeatRequest
import by.vsdev.posterminal.demo.dto.NewCommandRequest
import by.vsdev.posterminal.demo.dto.RegisterRequest
import by.vsdev.posterminal.demo.model.Device
import by.vsdev.posterminal.demo.model.DeviceCommand
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

/**
 * Ktor-backed [PosApi]. The base URL is resolved per request via [baseUrlProvider] so a QR-scanned
 * `serverUrl` takes effect immediately without rebuilding the client. `expectSuccess` turns non-2xx
 * responses into exceptions; device-scoped 404s are surfaced as [DeviceNotFoundException] so the
 * repository can self-heal.
 *
 * Shared by the Android agent; `:core` compiles for android/jvm (OkHttp) and js (fetch).
 */
class KtorPosApiClient(
    private val baseUrlProvider: suspend () -> String,
    private val apiKey: String? = null,
    private val client: HttpClient = defaultClient(),
) : PosApi {

    private suspend fun base(): String = baseUrlProvider().trimEnd('/')

    override suspend fun register(request: RegisterRequest): Device =
        client.post("${base()}/devices/register") { jsonBody(request) }.body()

    override suspend fun heartbeat(deviceId: String, request: HeartbeatRequest): Device = deviceScoped(deviceId) {
        client.post("${base()}/devices/$deviceId/heartbeat") { jsonBody(request) }.body()
    }

    override suspend fun getCommands(deviceId: String): List<DeviceCommand> = deviceScoped(deviceId) {
        client.get("${base()}/devices/$deviceId/commands") { auth() }.body()
    }

    override suspend fun ackCommand(deviceId: String, commandId: String, request: AckRequest) {
        deviceScoped(deviceId) {
            client.post("${base()}/devices/$deviceId/commands/$commandId/ack") { jsonBody(request) }
        }
    }

    override suspend fun postCommand(deviceId: String, request: NewCommandRequest): DeviceCommand =
        client.post("${base()}/devices/$deviceId/commands") { jsonBody(request) }.body()

    override suspend fun listDevices(): List<Device> = client.get("${base()}/devices") { auth() }.body()

    override suspend fun deleteDevice(deviceId: String) {
        client.delete("${base()}/devices/$deviceId") { auth() }
    }

    fun close() = client.close()

    private suspend fun <T> deviceScoped(deviceId: String, block: suspend () -> T): T = try {
        block()
    } catch (e: ClientRequestException) {
        if (e.response.status == HttpStatusCode.NotFound) {
            throw DeviceNotFoundException(deviceId)
        }
        throw e
    }

    private fun HttpRequestBuilder.auth() {
        apiKey?.let { header("X-Api-Key", it) }
    }

    private fun HttpRequestBuilder.jsonBody(body: Any) {
        auth()
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    companion object {
        fun defaultClient(): HttpClient = HttpClient(httpClientEngineFactory) {
            expectSuccess = true
            install(ContentNegotiation) { json(posJson) }
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
            }
            install(Logging) { level = LogLevel.INFO }
        }
    }
}
