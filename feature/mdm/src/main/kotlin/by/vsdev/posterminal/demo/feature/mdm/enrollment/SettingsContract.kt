package by.vsdev.posterminal.demo.feature.mdm.enrollment

import android.os.Parcelable
import by.vsdev.posterminal.demo.core.ui.mvi.UiIntent
import by.vsdev.posterminal.demo.core.ui.mvi.UiSideEffect
import by.vsdev.posterminal.demo.core.ui.mvi.UiState
import by.vsdev.posterminal.demo.domain.result.DomainError
import kotlinx.parcelize.Parcelize

/** MVI contract for the Settings screen. */
@Parcelize
data class SettingsUiState(
    val deviceId: String = "…",
    val name: String = "POS Terminal",
    val enrolled: Boolean = false,
    val busy: Boolean = false,
    val kioskActive: Boolean = false,
    val adminActive: Boolean = false,
) : UiState,
    Parcelable

sealed interface SettingsIntent : UiIntent {
    data object SyncNow : SettingsIntent

    data object FactoryReset : SettingsIntent
}

sealed interface SettingsSideEffect : UiSideEffect {
    data object SyncRequested : SettingsSideEffect

    data object LoggedOut : SettingsSideEffect

    data class Failed(val error: DomainError) : SettingsSideEffect
}
