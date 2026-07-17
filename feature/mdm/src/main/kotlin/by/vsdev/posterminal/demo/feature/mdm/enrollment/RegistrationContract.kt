package by.vsdev.posterminal.demo.feature.mdm.enrollment

import by.vsdev.posterminal.demo.core.ui.mvi.UiIntent
import by.vsdev.posterminal.demo.core.ui.mvi.UiSideEffect
import by.vsdev.posterminal.demo.core.ui.mvi.UiState
import by.vsdev.posterminal.demo.domain.result.DomainError

/** MVI contract for the Registration screen. */
data class RegistrationUiState(
    val deviceId: String = "…",
    val name: String = "POS Terminal",
    val busy: Boolean = false,
    /** True once the device has been registered but Device Admin must be enabled before reaching POS. */
    val awaitingAdmin: Boolean = false,
    val adminActive: Boolean = false,
) : UiState

sealed interface RegistrationIntent : UiIntent {
    data object RegisterManually : RegistrationIntent

    data class RegisterWithToken(val payload: String) : RegistrationIntent

    /** User tapped "Enable Device Admin" during the admin gate. */
    data object EnableAdmin : RegistrationIntent

    /** User returned from the system Device-Admin screen; re-check + finish enrollment if granted. */
    data object AdminResult : RegistrationIntent
}

sealed interface RegistrationSideEffect : UiSideEffect {
    data class Enrolled(val deviceId: String) : RegistrationSideEffect

    data object InvalidQr : RegistrationSideEffect

    data class Failed(val error: DomainError) : RegistrationSideEffect

    /** Ask the UI to launch the system "enable Device Admin" screen. */
    data object LaunchDeviceAdmin : RegistrationSideEffect
}
