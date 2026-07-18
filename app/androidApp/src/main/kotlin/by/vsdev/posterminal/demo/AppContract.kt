package by.vsdev.posterminal.demo

import android.os.Parcelable
import by.vsdev.posterminal.demo.core.ui.mvi.UiIntent
import by.vsdev.posterminal.demo.core.ui.mvi.UiSideEffect
import by.vsdev.posterminal.demo.core.ui.mvi.UiState
import kotlinx.parcelize.Parcelize

/** MVI contract for the app shell (session + navigation). */
@Parcelize
data class AppUiState(val startRoute: AppRoute? = null) :
    UiState,
    Parcelable

sealed interface AppIntent : UiIntent {
    /** Any user touch — resets the kiosk idle timer. */
    data object UserInteracted : AppIntent
}

sealed interface AppSideEffect : UiSideEffect {
    /** Clear the back stack and land on [route] (enrollment / logout transitions). */
    data class Reset(val route: AppRoute) : AppSideEffect

    /** Kiosk idle: show the Offer attract loop. */
    data object ShowOffer : AppSideEffect

    /** Kiosk turned off: leave the Offer attract loop. */
    data object DismissOffer : AppSideEffect
}
