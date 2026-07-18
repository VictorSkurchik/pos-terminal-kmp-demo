package by.vsdev.posterminal.demo.feature.mdm.enrollment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import by.vsdev.posterminal.demo.core.ui.mvi.MviViewModel
import by.vsdev.posterminal.demo.domain.result.AppResult
import by.vsdev.posterminal.demo.feature.mdm.domain.repository.SettingsRepository
import by.vsdev.posterminal.demo.feature.mdm.domain.service.DeviceAdminRepository
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.EnrollDeviceUseCase
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.EnrollWithTokenUseCase
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.EnrollmentResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val enrollDevice: EnrollDeviceUseCase,
    private val enrollWithToken: EnrollWithTokenUseCase,
    private val settings: SettingsRepository,
    private val deviceAdmin: DeviceAdminRepository,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<RegistrationUiState, RegistrationIntent, RegistrationSideEffect>(
    RegistrationUiState(),
    savedStateHandle,
) {

    init {
        viewModelScope.launch {
            val id = settings.deviceId()
            val name = settings.deviceName.first()
            setState { copy(deviceId = id, name = name) }
        }
    }

    override fun onIntent(intent: RegistrationIntent) {
        when (intent) {
            RegistrationIntent.RegisterManually -> requestEnrollment(Pending.Manual)
            is RegistrationIntent.RegisterWithToken -> requestEnrollment(Pending.WithToken(intent.payload))
            RegistrationIntent.EnableAdmin -> postSideEffect(RegistrationSideEffect.LaunchDeviceAdmin)
            RegistrationIntent.AdminResult -> onAdminResult()
        }
    }

    /**
     * The user chose how to register. Device Admin is a hard prerequisite for reaching POS, so we
     * only complete enrollment once it is active; otherwise we show the admin gate and defer.
     */
    private fun requestEnrollment(request: Pending) {
        setState { copy(pending = request) }
        if (deviceAdmin.isAdminActive()) {
            completeEnrollment()
        } else {
            setState { copy(awaitingAdmin = true, adminActive = false) }
        }
    }

    private fun onAdminResult() {
        val active = deviceAdmin.isAdminActive()
        setState { copy(adminActive = active) }
        if (active) completeEnrollment()
    }

    private fun completeEnrollment() {
        val request = currentState.pending ?: return
        runExclusively {
            when (request) {
                Pending.Manual -> when (val result = enrollDevice(currentState.name)) {
                    is AppResult.Success -> postSideEffect(RegistrationSideEffect.Enrolled(result.data.id))
                    is AppResult.Failure -> failBack { postSideEffect(RegistrationSideEffect.Failed(result.error)) }
                }

                is Pending.WithToken -> when (val result = enrollWithToken(currentState.name, request.payload)) {
                    is EnrollmentResult.Success -> postSideEffect(RegistrationSideEffect.Enrolled(result.device.id))
                    EnrollmentResult.InvalidToken -> failBack { postSideEffect(RegistrationSideEffect.InvalidQr) }
                    is EnrollmentResult.Failed -> failBack {
                        postSideEffect(RegistrationSideEffect.Failed(result.error))
                    }
                }
            }
        }
    }

    /** On a failed completion, drop back to the registration options so the user can retry. */
    private fun failBack(emit: () -> Unit) {
        setState { copy(awaitingAdmin = false, pending = null) }
        emit()
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
