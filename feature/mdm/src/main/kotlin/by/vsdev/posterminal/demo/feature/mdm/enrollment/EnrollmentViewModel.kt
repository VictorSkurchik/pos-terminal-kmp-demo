package by.vsdev.posterminal.demo.feature.mdm.enrollment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.vsdev.posterminal.demo.domain.repository.SettingsRepository
import by.vsdev.posterminal.demo.domain.result.AppResult
import by.vsdev.posterminal.demo.domain.result.DomainError
import by.vsdev.posterminal.demo.domain.service.DeviceAdminRepository
import by.vsdev.posterminal.demo.domain.usecase.mdm.EnrollDeviceUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.EnrollWithTokenUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.EnrollmentResult
import by.vsdev.posterminal.demo.domain.usecase.mdm.LogoutUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.ObserveEnrollmentUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.ObserveKioskStateUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.SyncNowUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EnrollmentUiState(
    val deviceId: String = "…",
    val name: String = "POS Terminal",
    val enrolled: Boolean = false,
    val busy: Boolean = false,
)

/** Semantic one-shot events; the screen maps them to localized snackbar text. */
sealed interface EnrollmentEvent {
    data class Enrolled(val deviceId: String) : EnrollmentEvent

    data object InvalidQr : EnrollmentEvent

    data class Failed(val error: DomainError) : EnrollmentEvent

    data object SyncRequested : EnrollmentEvent

    data object LoggedOut : EnrollmentEvent
}

class EnrollmentViewModel(
    private val enrollDevice: EnrollDeviceUseCase,
    private val enrollWithToken: EnrollWithTokenUseCase,
    private val syncNow: SyncNowUseCase,
    private val logout: LogoutUseCase,
    private val settings: SettingsRepository,
    private val deviceAdmin: DeviceAdminRepository,
    observeEnrollment: ObserveEnrollmentUseCase,
    observeKiosk: ObserveKioskStateUseCase,
) : ViewModel() {

    private val deviceId = MutableStateFlow("…")
    private val name = MutableStateFlow("POS Terminal")
    private val busy = MutableStateFlow(false)

    val kioskActive: StateFlow<Boolean> = observeKiosk()

    private val _adminActive = MutableStateFlow(deviceAdmin.isAdminActive())
    val adminActive: StateFlow<Boolean> = _adminActive

    /** Re-reads Device Admin status (call after returning from the system enable-admin screen). */
    fun refreshAdminState() {
        _adminActive.value = deviceAdmin.isAdminActive()
    }

    val uiState: StateFlow<EnrollmentUiState> =
        combine(deviceId, name, observeEnrollment(), busy) { id, nm, enrolled, isBusy ->
            EnrollmentUiState(id, nm, enrolled, isBusy)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), EnrollmentUiState())

    private val eventsChannel = Channel<EnrollmentEvent>(Channel.BUFFERED)
    val events: Flow<EnrollmentEvent> = eventsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            deviceId.value = settings.deviceId()
            name.value = settings.deviceName.first()
        }
    }

    fun onNameChange(value: String) {
        name.value = value
    }

    fun enroll() = runExclusively {
        when (val result = enrollDevice(name.value)) {
            is AppResult.Success -> eventsChannel.send(EnrollmentEvent.Enrolled(result.data.id))
            is AppResult.Failure -> eventsChannel.send(EnrollmentEvent.Failed(result.error))
        }
    }

    /** QR enrollment: parse the token, register with it, and store the serverUrl from the QR. */
    fun enrollWithToken(payload: String) = runExclusively {
        when (val result = enrollWithToken.invoke(name.value, payload)) {
            is EnrollmentResult.Success -> eventsChannel.send(EnrollmentEvent.Enrolled(result.device.id))
            is EnrollmentResult.InvalidToken -> eventsChannel.send(EnrollmentEvent.InvalidQr)
            is EnrollmentResult.Failed -> eventsChannel.send(EnrollmentEvent.Failed(result.error))
        }
    }

    fun syncNow() {
        syncNow.invoke()
        viewModelScope.launch { eventsChannel.send(EnrollmentEvent.SyncRequested) }
    }

    /** Logout / factory reset: local state is cleared regardless; a backend error is still surfaced. */
    fun logout() = runExclusively {
        when (val result = logout.invoke()) {
            is AppResult.Success -> eventsChannel.send(EnrollmentEvent.LoggedOut)
            is AppResult.Failure -> eventsChannel.send(EnrollmentEvent.Failed(result.error))
        }
    }

    private fun runExclusively(block: suspend () -> Unit) {
        if (busy.value) return
        viewModelScope.launch {
            busy.value = true
            try {
                block()
            } finally {
                busy.value = false
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
