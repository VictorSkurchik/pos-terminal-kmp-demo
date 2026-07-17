package by.vsdev.posterminal.demo.feature.mdm.domain.usecase

import by.vsdev.posterminal.demo.domain.result.AppResult
import by.vsdev.posterminal.demo.domain.result.DomainError
import by.vsdev.posterminal.demo.feature.mdm.domain.model.EnrollmentToken
import by.vsdev.posterminal.demo.feature.mdm.fakes.FakeDeviceRepository
import by.vsdev.posterminal.demo.feature.mdm.fakes.FakeSettingsRepository
import by.vsdev.posterminal.demo.feature.mdm.fakes.FakeTokenParser
import by.vsdev.posterminal.demo.feature.mdm.fakes.RecordingScheduler
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EnrollWithTokenUseCaseTest {

    private val device = FakeDeviceRepository()
    private val settings = FakeSettingsRepository()
    private val scheduler = RecordingScheduler()

    @Test
    fun `invalid QR returns InvalidToken and does not register`() = runTest {
        val useCase = EnrollWithTokenUseCase(device, settings, scheduler, FakeTokenParser(null))

        val result = useCase("Front Till", "garbage")

        assertEquals(EnrollmentResult.InvalidToken, result)
        assertEquals(0, device.registerCount)
        assertFalse(scheduler.scheduled)
    }

    @Test
    fun `valid QR registers, points at the scanned backend, and schedules polling`() = runTest {
        val parser = FakeTokenParser(EnrollmentToken(token = "tok-1", serverUrl = "https://scanned.test"))
        val useCase = EnrollWithTokenUseCase(device, settings, scheduler, parser)

        val result = useCase("Front Till", "payload")

        assertIs<EnrollmentResult.Success>(result)
        assertEquals("https://scanned.test", settings.serverUrl())
        assertTrue(settings.enrolledState.value)
        assertTrue(scheduler.scheduled)
    }

    @Test
    fun `backend failure surfaces the error without enrolling`() = runTest {
        device.registerResult = AppResult.Failure(DomainError.Timeout)
        val parser = FakeTokenParser(EnrollmentToken(token = "tok-1", serverUrl = "https://scanned.test"))
        val useCase = EnrollWithTokenUseCase(device, settings, scheduler, parser)

        val result = useCase("Front Till", "payload")

        val failed = assertIs<EnrollmentResult.Failed>(result)
        assertEquals(DomainError.Timeout, failed.error)
        assertFalse(settings.enrolledState.value)
        assertFalse(scheduler.scheduled)
    }
}
