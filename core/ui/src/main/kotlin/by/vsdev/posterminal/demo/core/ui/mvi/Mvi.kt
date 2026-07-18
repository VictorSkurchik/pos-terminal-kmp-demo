package by.vsdev.posterminal.demo.core.ui.mvi

import androidx.lifecycle.SavedStateHandle
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
 *
 * The whole [UiState] is persisted through [savedStateHandle] so it survives process death: the
 * initial state is restored from the handle when present, and every [setState] writes the new state
 * back. This requires each concrete [S] (and its members) to be `Parcelable`.
 */
abstract class MviViewModel<S : UiState, I : UiIntent, E : UiSideEffect>(
    initialState: S,
    protected val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(savedStateHandle[STATE_KEY] ?: initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val sideEffects = Channel<E>(Channel.BUFFERED)
    val sideEffect: Flow<E> = sideEffects.receiveAsFlow()

    protected val currentState: S get() = _state.value

    /** Single point of entry for all user/system actions. */
    abstract fun onIntent(intent: I)

    protected fun setState(reducer: S.() -> S) {
        _state.update(reducer)
        savedStateHandle[STATE_KEY] = _state.value
    }

    protected fun postSideEffect(effect: E) {
        viewModelScope.launch { sideEffects.send(effect) }
    }

    private companion object {
        const val STATE_KEY = "mvi_ui_state"
    }
}
