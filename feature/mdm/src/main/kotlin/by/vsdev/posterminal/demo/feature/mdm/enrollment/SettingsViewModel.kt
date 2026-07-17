package by.vsdev.posterminal.demo.feature.mdm.enrollment

import androidx.lifecycle.viewModelScope
import by.vsdev.posterminal.demo.core.ui.mvi.MviViewModel
import by.vsdev.posterminal.demo.domain.result.AppResult
import by.vsdev.posterminal.demo.feature.mdm.domain.repository.SettingsRepository
import by.vsdev.posterminal.demo.feature.mdm.domain.service.DeviceAdminRepository
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.LogoutUseCase
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.ObserveEnrollmentUseCase
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.ObserveKioskStateUseCase
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.SyncNowUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val syncNow: SyncNowUseCase,
    private val logout: LogoutUseCase,
    private val settings: SettingsRepository,
    private val deviceAdmin: DeviceAdminRepository,
    observeEnrollment: ObserveEnrollmentUseCase,
    observeKiosk: ObserveKioskStateUseCase,
) : MviViewModel<SettingsUiState, SettingsIntent, SettingsSideEffect>(
    SettingsUiState(adminActive = deviceAdmin.isAdminActive()),
) {

    init {
        viewModelScope.launch {
            val id = settings.deviceId()
            val name = settings.deviceName.first()
            setState { copy(deviceId = id, name = name) }
        }
        observeEnrollment().onEach { enrolled -> setState { copy(enrolled = enrolled) } }.launchIn(viewModelScope)
        observeKiosk().onEach { kiosk -> setState { copy(kioskActive = kiosk) } }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.SyncNow -> {
                syncNow()
                postSideEffect(SettingsSideEffect.SyncRequested)
            }

            SettingsIntent.FactoryReset -> factoryReset()
        }
    }

    private fun factoryReset() {
        if (currentState.busy) return
        viewModelScope.launch {
            setState { copy(busy = true) }
            try {
                when (val result = logout()) {
                    is AppResult.Success -> postSideEffect(SettingsSideEffect.LoggedOut)
                    is AppResult.Failure -> postSideEffect(SettingsSideEffect.Failed(result.error))
                }
            } finally {
                setState { copy(busy = false) }
            }
        }
    }
}
