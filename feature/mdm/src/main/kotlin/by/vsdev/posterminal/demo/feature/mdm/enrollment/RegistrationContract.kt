package by.vsdev.posterminal.demo.feature.mdm.enrollment

import android.os.Parcelable
import by.vsdev.posterminal.demo.core.ui.mvi.UiIntent
import by.vsdev.posterminal.demo.core.ui.mvi.UiSideEffect
import by.vsdev.posterminal.demo.core.ui.mvi.UiState
import by.vsdev.posterminal.demo.domain.result.DomainError
import kotlinx.parcelize.Parcelize

/** What to enroll once Device Admin is confirmed active; part of the saved state. */
sealed interface Pending : Parcelable {
    @Parcelize
    data object Manual : Pending

    @Parcelize
    data class WithToken(val payload: String) : Pending
}

/** MVI contract for the Registration screen. */
@Parcelize
data class RegistrationUiState(
    val deviceId: String = "…",
    val name: String = "POS Terminal",
    val busy: Boolean = false,
    /** True once the device has been registered but Device Admin must be enabled before reaching POS. */
    val awaitingAdmin: Boolean = false,
    val adminActive: Boolean = false,
    /** The deferred enrollment awaiting Device Admin; survives the external admin round-trip. */
    val pending: Pending? = null,
) : UiState,
    Parcelable

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
