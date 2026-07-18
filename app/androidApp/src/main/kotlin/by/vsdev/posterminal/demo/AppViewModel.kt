package by.vsdev.posterminal.demo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import by.vsdev.posterminal.demo.core.ui.mvi.MviViewModel
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.ObserveEnrollmentUseCase
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.ObserveKioskStateUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Owns app-level session logic: the enrollment-driven start destination, enrollment/logout redirects
 * and the kiosk idle timer. State (start route) flows down; the UI sends [AppIntent.UserInteracted]
 * and performs the emitted [AppSideEffect] navigation.
 */
class AppViewModel(
    observeEnrollment: ObserveEnrollmentUseCase,
    observeKiosk: ObserveKioskStateUseCase,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<AppUiState, AppIntent, AppSideEffect>(AppUiState(), savedStateHandle) {

    // Conflated: only the most recent interaction matters for idle detection.
    private val interactions = Channel<Unit>(Channel.CONFLATED)

    init {
        viewModelScope.launch {
            val enrolled = observeEnrollment().first()
            setState { copy(startRoute = if (enrolled) AppRoute.Pos else AppRoute.Registration) }
        }
        observeEnrollment().drop(1).distinctUntilChanged()
            .onEach { enrolled ->
                postSideEffect(AppSideEffect.Reset(if (enrolled) AppRoute.Pos else AppRoute.Registration))
            }
            .launchIn(viewModelScope)
        viewModelScope.launch {
            observeKiosk().collectLatest { kioskActive ->
                if (!kioskActive) {
                    postSideEffect(AppSideEffect.DismissOffer)
                    return@collectLatest
                }
                runIdleLoop()
            }
        }
    }

    override fun onIntent(intent: AppIntent) {
        when (intent) {
            AppIntent.UserInteracted -> interactions.trySend(Unit)
        }
    }

    /** While kiosk is on: show Offer after [KIOSK_IDLE_MS] of no interaction; resume on the next touch. */
    private suspend fun runIdleLoop() {
        while (true) {
            val interacted = withTimeoutOrNull(KIOSK_IDLE_MS) { interactions.receive() } != null
            if (!interacted) {
                postSideEffect(AppSideEffect.ShowOffer)
                interactions.receive() // block until the user returns before arming the timer again
            }
        }
    }

    private companion object {
        const val KIOSK_IDLE_MS = 10_000L
    }
}
