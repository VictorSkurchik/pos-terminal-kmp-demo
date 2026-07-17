package by.vsdev.posterminal.demo.feature.mdm.enrollment

import app.cash.turbine.test
import by.vsdev.posterminal.demo.domain.result.AppResult
import by.vsdev.posterminal.demo.domain.result.DomainError
import by.vsdev.posterminal.demo.feature.mdm.domain.model.Device
import by.vsdev.posterminal.demo.feature.mdm.domain.model.DeviceStatus
import by.vsdev.posterminal.demo.feature.mdm.domain.repository.SettingsRepository
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.EnrollDeviceUseCase
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.EnrollWithTokenUseCase
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.EnrollmentResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val enrollDevice = mockk<EnrollDeviceUseCase>()
    private val enrollWithToken = mockk<EnrollWithTokenUseCase>()
    private val settings = mockk<SettingsRepository>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { settings.deviceId() } returns "pos-1"
        every { settings.deviceName } returns flowOf("POS Terminal")
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = RegistrationViewModel(enrollDevice, enrollWithToken, settings)

    @Test
    fun `manual enroll success emits Enrolled`() = runTest(dispatcher) {
        coEvery { enrollDevice(any()) } returns AppResult.Success(device())
        val vm = viewModel()
        vm.sideEffect.test {
            vm.onIntent(RegistrationIntent.RegisterManually)
            advanceUntilIdle()
            assertIs<RegistrationSideEffect.Enrolled>(awaitItem())
        }
    }

    @Test
    fun `invalid QR emits InvalidQr`() = runTest(dispatcher) {
        coEvery { enrollWithToken(any(), any()) } returns EnrollmentResult.InvalidToken
        val vm = viewModel()
        vm.sideEffect.test {
            vm.onIntent(RegistrationIntent.RegisterWithToken("garbage"))
            advanceUntilIdle()
            assertEquals(RegistrationSideEffect.InvalidQr, awaitItem())
        }
    }

    @Test
    fun `backend failure emits Failed with the error`() = runTest(dispatcher) {
        coEvery { enrollDevice(any()) } returns AppResult.Failure(DomainError.Network)
        val vm = viewModel()
        vm.sideEffect.test {
            vm.onIntent(RegistrationIntent.RegisterManually)
            advanceUntilIdle()
            assertEquals(RegistrationSideEffect.Failed(DomainError.Network), awaitItem())
        }
    }

    private fun device() = Device(id = "pos-1", name = "POS Terminal", lastSeenAt = 0L, status = DeviceStatus.ONLINE)
}
