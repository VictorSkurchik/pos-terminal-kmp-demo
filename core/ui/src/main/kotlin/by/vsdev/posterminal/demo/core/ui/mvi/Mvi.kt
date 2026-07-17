package by.vsdev.posterminal.demo.core.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Immutable screen state rendered by the UI (state down). */
interface UiState

/** A user (or system) action sent into a ViewModel (intent up). */
interface UiIntent

/** A one-shot effect the UI performs once — navigation, snackbar, launching a system intent. */
interface UiSideEffect

/**
 * Base for the MVI presentation layer, enforcing a unidirectional data flow:
 * - **state**: a single immutable [UiState] exposed as a [StateFlow];
 * - **onIntent**: the only entry point for actions;
 * - **sideEffect**: one-shot [UiSideEffect]s delivered exactly once via a [Channel].
 *
 * Subclasses reduce domain streams and intents into state with [setState] and fire effects with
 * [postSideEffect].
 */
abstract class MviViewModel<S : UiState, I : UiIntent, E : UiSideEffect>(initialState: S) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val sideEffects = Channel<E>(Channel.BUFFERED)
    val sideEffect: Flow<E> = sideEffects.receiveAsFlow()

    protected val currentState: S get() = _state.value

    /** Single point of entry for all user/system actions. */
    abstract fun onIntent(intent: I)

    protected fun setState(reducer: S.() -> S) = _state.update(reducer)

    protected fun postSideEffect(effect: E) {
        viewModelScope.launch { sideEffects.send(effect) }
    }
}
