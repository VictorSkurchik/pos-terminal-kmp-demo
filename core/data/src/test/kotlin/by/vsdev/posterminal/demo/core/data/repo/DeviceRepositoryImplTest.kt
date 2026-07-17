package by.vsdev.posterminal.demo.core.data.repo

import by.vsdev.posterminal.demo.core.data.platform.DeviceInfoProvider
import by.vsdev.posterminal.demo.core.data.platform.TimeProvider
import by.vsdev.posterminal.demo.domain.policy.DevicePolicy
import by.vsdev.posterminal.demo.domain.repository.SettingsRepository
import by.vsdev.posterminal.demo.domain.result.AppResult
import by.vsdev.posterminal.demo.domain.result.DomainError
import by.vsdev.posterminal.demo.network.KtorPosApiClient
import by.vsdev.posterminal.demo.network.posJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DeviceRepositoryImplTest {

    private val settings = FakeSettings()
    private val policy = FakeDevicePolicy()
    private val deviceInfo = object : DeviceInfoProvider {
        override val model = "Pixel-Test"
    }
    private val time = object : TimeProvider {
        override fun nowMillis() = 42L
    }

    private fun repo(handler: MockRequestHandler): DeviceRepositoryImpl {
        val client = HttpClient(MockEngine(handler)) {
            expectSuccess = true
            install(ContentNegotiation) { json(posJson) }
        }
        val api = KtorPosApiClient(baseUrlProvider = { "https://backend.test" }, client = client)
        return DeviceRepositoryImpl(api, settings, policy, deviceInfo, time)
    }

    @Test
    fun `register maps wire device to domain`() = runTest {
        val repo = repo { respond(deviceJson(), HttpStatusCode.OK, jsonHeaders) }

        val result = repo.register()

        val success = assertIs<AppResult.Success<*>>(result)
        assertEquals("pos-test01", (success.data as by.vsdev.posterminal.demo.domain.model.Device).id)
    }

    @Test
    fun `heartbeat self-heals on 404 by re-registering and retrying`() = runTest {
        val paths = mutableListOf<String>()
        val repo = repo { request ->
            val path = request.url.encodedPath
            paths += path
            val firstHeartbeat = path.endsWith("/heartbeat") && paths.count { it.endsWith("/heartbeat") } == 1
            if (firstHeartbeat) {
                respond("", HttpStatusCode.NotFound)
            } else {
                respond(deviceJson(), HttpStatusCode.OK, jsonHeaders)
            }
        }

        val result = repo.heartbeat(batteryLevel = 80)

        assertIs<AppResult.Success<*>>(result)
        // Must have re-registered between the failed and successful heartbeat.
        assertTrue(paths.any { it.endsWith("/devices/register") })
        assertEquals(2, paths.count { it.endsWith("/heartbeat") })
    }

    @Test
    fun `5xx maps to a typed Server error`() = runTest {
        val repo = repo { respond("", HttpStatusCode.InternalServerError) }

        val result = repo.heartbeat(batteryLevel = null)

        val failure = assertIs<AppResult.Failure>(result)
        assertEquals(DomainError.Server(500), failure.error)
    }

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun deviceJson(id: String = "pos-test01") =
        """
        {"id":"$id","name":"POS Terminal","lastSeenAt":0,
         "status":"ONLINE","kioskActive":false,"restrictPayment":false}
        """.trimIndent()

    private class FakeSettings : SettingsRepository {
        override val enrolled: Flow<Boolean> = MutableStateFlow(true)
        override val deviceName: Flow<String> = MutableStateFlow("POS Terminal")
        override suspend fun deviceId() = "pos-test01"
        override suspend fun serverUrl() = "https://backend.test"
        override suspend fun setServerUrl(url: String) = Unit
        override suspend fun setEnrolled(enrolled: Boolean, name: String?, serverUrl: String?) = Unit
        override suspend fun setDeviceName(name: String) = Unit
        override suspend fun clearEnrollment() = Unit
    }

    private class FakeDevicePolicy : DevicePolicy {
        override val restrictPayment: Flow<Boolean> = MutableStateFlow(false)
        override val kioskActive: Flow<Boolean> = MutableStateFlow(false)
        override suspend fun setRestrictPayment(value: Boolean) = Unit
        override suspend fun setKioskActive(value: Boolean) = Unit
        override suspend fun reset() = Unit
    }
}
