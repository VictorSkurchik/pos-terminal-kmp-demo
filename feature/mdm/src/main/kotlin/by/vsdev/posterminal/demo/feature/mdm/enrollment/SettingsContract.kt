package by.vsdev.posterminal.demo.feature.mdm.enrollment

import by.vsdev.posterminal.demo.core.ui.mvi.UiIntent
import by.vsdev.posterminal.demo.core.ui.mvi.UiSideEffect
import by.vsdev.posterminal.demo.core.ui.mvi.UiState
import by.vsdev.posterminal.demo.domain.result.DomainError

/** MVI contract for the Settings screen. */
data class SettingsUiState(
    val deviceId: String = "…",
    val name: String = "POS Terminal",
    val enrolled: Boolean = false,
    val busy: Boolean = false,
    val kioskActive: Boolean = false,
    val adminActive: Boolean = false,
) : UiState

sealed interface SettingsIntent : UiIntent {
    data object SyncNow : SettingsIntent

    data object FactoryReset : SettingsIntent
}

sealed interface SettingsSideEffect : UiSideEffect {
    data object SyncRequested : SettingsSideEffect

    data object LoggedOut : SettingsSideEffect

    data class Failed(val error: DomainError) : SettingsSideEffect
}
