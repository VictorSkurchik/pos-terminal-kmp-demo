package by.vsdev.posterminal.demo.feature.offer

import by.vsdev.posterminal.demo.core.ui.mvi.MviViewModel

class OfferViewModel : MviViewModel<OfferUiState, OfferIntent, OfferSideEffect>(OfferUiState()) {

    override fun onIntent(intent: OfferIntent) {
        when (intent) {
            OfferIntent.SlideCompleted -> advance()
            OfferIntent.Dismiss -> postSideEffect(OfferSideEffect.Exit)
        }
    }

    private fun advance() {
        val count = currentState.offers.size
        if (count == 0) return
        setState { copy(index = (index + 1) % count) }
    }
}
