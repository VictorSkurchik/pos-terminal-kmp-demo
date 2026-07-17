package by.vsdev.posterminal.demo.feature.mdm.enrollment

import app.cash.turbine.test
import by.vsdev.posterminal.demo.domain.repository.SettingsRepository
import by.vsdev.posterminal.demo.domain.result.AppResult
import by.vsdev.posterminal.demo.domain.result.DomainError
import by.vsdev.posterminal.demo.domain.service.DeviceAdminRepository
import by.vsdev.posterminal.demo.domain.usecase.mdm.LogoutUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.ObserveEnrollmentUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.ObserveKioskStateUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.SyncNowUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val syncNow = mockk<SyncNowUseCase>(relaxed = true)
    private val logout = mockk<LogoutUseCase>()
    private val settings = mockk<SettingsRepository>()
    private val deviceAdmin = mockk<DeviceAdminRepository>()
    private val observeEnrollment = mockk<ObserveEnrollmentUseCase>()
    private val observeKiosk = mockk<ObserveKioskStateUseCase>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { settings.deviceId() } returns "pos-1"
        every { settings.deviceName } returns flowOf("POS Terminal")
        every { deviceAdmin.isAdminActive() } returns false
        every { observeEnrollment() } returns flowOf(true)
        every { observeKiosk() } returns MutableStateFlow(false)
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() =
        SettingsViewModel(syncNow, logout, settings, deviceAdmin, observeEnrollment, observeKiosk)

    @Test
    fun `SyncNow emits SyncRequested`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.sideEffect.test {
            vm.onIntent(SettingsIntent.SyncNow)
            advanceUntilIdle()
            assertEquals(SettingsSideEffect.SyncRequested, awaitItem())
        }
    }

    @Test
    fun `EnableAdmin asks the UI to launch the admin screen`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.sideEffect.test {
            vm.onIntent(SettingsIntent.EnableAdmin)
            advanceUntilIdle()
            assertEquals(SettingsSideEffect.LaunchDeviceAdmin, awaitItem())
        }
    }

    @Test
    fun `FactoryReset success emits LoggedOut`() = runTest(dispatcher) {
        coEvery { logout() } returns AppResult.Success(Unit)
        val vm = viewModel()
        vm.sideEffect.test {
            vm.onIntent(SettingsIntent.FactoryReset)
            advanceUntilIdle()
            assertEquals(SettingsSideEffect.LoggedOut, awaitItem())
        }
    }

    @Test
    fun `FactoryReset failure emits Failed`() = runTest(dispatcher) {
        coEvery { logout() } returns AppResult.Failure(DomainError.Timeout)
        val vm = viewModel()
        vm.sideEffect.test {
            vm.onIntent(SettingsIntent.FactoryReset)
            advanceUntilIdle()
            assertEquals(SettingsSideEffect.Failed(DomainError.Timeout), awaitItem())
        }
    }
}
