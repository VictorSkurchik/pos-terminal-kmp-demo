package by.vsdev.posterminal.demo

import by.vsdev.posterminal.demo.dto.EnrollmentToken
import by.vsdev.posterminal.demo.dto.parseEnrollmentToken
import by.vsdev.posterminal.demo.dto.toQrPayload
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EnrollmentQrTest {

    @Test
    fun roundTrip() {
        val original = EnrollmentToken(token = "enr-abc123", serverUrl = "http://localhost:8080")
        val payload = original.toQrPayload()
        val parsed = parseEnrollmentToken(payload)
        assertEquals(original, parsed)
    }

    @Test
    fun invalidPayloadReturnsNull() {
        assertNull(parseEnrollmentToken("not a token"))
        assertNull(parseEnrollmentToken("{\"unexpected\":true}"))
    }
}
