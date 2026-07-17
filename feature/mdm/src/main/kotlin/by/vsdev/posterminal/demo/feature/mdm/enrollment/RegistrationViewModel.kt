package by.vsdev.posterminal.demo.feature.mdm.enrollment

import androidx.lifecycle.viewModelScope
import by.vsdev.posterminal.demo.core.ui.mvi.MviViewModel
import by.vsdev.posterminal.demo.domain.repository.SettingsRepository
import by.vsdev.posterminal.demo.domain.result.AppResult
import by.vsdev.posterminal.demo.domain.usecase.mdm.EnrollDeviceUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.EnrollWithTokenUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.EnrollmentResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val enrollDevice: EnrollDeviceUseCase,
    private val enrollWithToken: EnrollWithTokenUseCase,
    private val settings: SettingsRepository,
) : MviViewModel<RegistrationUiState, RegistrationIntent, RegistrationSideEffect>(RegistrationUiState()) {

    init {
        viewModelScope.launch {
            val id = settings.deviceId()
            val name = settings.deviceName.first()
            setState { copy(deviceId = id, name = name) }
        }
    }

    override fun onIntent(intent: RegistrationIntent) {
        when (intent) {
            RegistrationIntent.RegisterManually -> registerManually()
            is RegistrationIntent.RegisterWithToken -> registerWithQr(intent.payload)
        }
    }

    private fun registerManually() = runExclusively {
        when (val result = enrollDevice(currentState.name)) {
            is AppResult.Success -> postSideEffect(RegistrationSideEffect.Enrolled(result.data.id))
            is AppResult.Failure -> postSideEffect(RegistrationSideEffect.Failed(result.error))
        }
    }

    private fun registerWithQr(payload: String) = runExclusively {
        when (val result = enrollWithToken(currentState.name, payload)) {
            is EnrollmentResult.Success -> postSideEffect(RegistrationSideEffect.Enrolled(result.device.id))
            EnrollmentResult.InvalidToken -> postSideEffect(RegistrationSideEffect.InvalidQr)
            is EnrollmentResult.Failed -> postSideEffect(RegistrationSideEffect.Failed(result.error))
        }
    }

    private fun runExclusively(block: suspend () -> Unit) {
        if (currentState.busy) return
        viewModelScope.launch {
            setState { copy(busy = true) }
            try {
                block()
            } finally {
                setState { copy(busy = false) }
            }
        }
    }
}
