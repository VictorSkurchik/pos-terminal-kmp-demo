package by.vsdev.posterminal.demo.feature.offer

import androidx.lifecycle.SavedStateHandle
import by.vsdev.posterminal.demo.core.ui.mvi.MviViewModel

class OfferViewModel(savedStateHandle: SavedStateHandle) :
    MviViewModel<OfferUiState, OfferIntent, OfferSideEffect>(OfferUiState(), savedStateHandle) {

    override fun onIntent(intent: OfferIntent) {
        when (intent) {
            OfferIntent.SlideCompleted -> advance()
            OfferIntent.Dismiss -> postSideEffect(OfferSideEffect.Exit)
        }
    }

    private fun advance() {
        val count = sampleOffers.size
        if (count == 0) return
        setState { copy(index = (index + 1) % count) }
    }
}
