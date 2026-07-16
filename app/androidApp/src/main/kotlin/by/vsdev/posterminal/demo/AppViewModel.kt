package by.vsdev.posterminal.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.vsdev.posterminal.demo.domain.usecase.mdm.ObserveEnrollmentUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.ObserveKioskStateUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Owns app-level session logic that previously lived in the NavHost composable: the enrollment-driven
 * start destination, enrollment/logout redirects, and the kiosk idle timer. The UI only reports user
 * interactions and performs the emitted [AppNavEvent]s.
 */
class AppViewModel(
    observeEnrollment: ObserveEnrollmentUseCase,
    observeKiosk: ObserveKioskStateUseCase,
) : ViewModel() {

    val startRoute: StateFlow<AppRoute?> =
        observeEnrollment()
            .map { enrolled -> if (enrolled) AppRoute.Pos else AppRoute.Registration }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    private val navChannel = Channel<AppNavEvent>(Channel.BUFFERED)
    val navEvents: Flow<AppNavEvent> = navChannel.receiveAsFlow()

    // Conflated: only the most recent interaction matters for idle detection.
    private val interactions = Channel<Unit>(Channel.CONFLATED)

    init {
        viewModelScope.launch {
            observeEnrollment().drop(1).distinctUntilChanged().collect { enrolled ->
                navChannel.send(AppNavEvent.Reset(if (enrolled) AppRoute.Pos else AppRoute.Registration))
            }
        }
        viewModelScope.launch {
            observeKiosk().collectLatest { kioskActive ->
                if (!kioskActive) {
                    navChannel.send(AppNavEvent.DismissOffer)
                    return@collectLatest
                }
                runIdleLoop()
            }
        }
    }

    /** While kiosk is on: show Offer after [KIOSK_IDLE_MS] of no interaction; resume on the next touch. */
    private suspend fun runIdleLoop() {
        while (true) {
            val interacted = withTimeoutOrNull(KIOSK_IDLE_MS) { interactions.receive() } != null
            if (!interacted) {
                navChannel.send(AppNavEvent.ShowOffer)
                interactions.receive() // block until the user returns before arming the timer again
            }
        }
    }

    fun onUserInteraction() {
        interactions.trySend(Unit)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        const val KIOSK_IDLE_MS = 10_000L
    }
}
