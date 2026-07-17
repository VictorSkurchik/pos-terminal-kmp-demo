package by.vsdev.posterminal.demo.feature.offer

import by.vsdev.posterminal.demo.core.ui.mvi.UiIntent
import by.vsdev.posterminal.demo.core.ui.mvi.UiSideEffect
import by.vsdev.posterminal.demo.core.ui.mvi.UiState

/** One promotional slide. Real images are user-provided; [emoji] is the placeholder motif. */
data class OfferItem(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val imageUrl: String? = null,
)

val sampleOffers = listOf(
    OfferItem("2-for-1 Burgers", "Every weekday, 4–6 PM", "🍔"),
    OfferItem("Free Dessert", "With any main course", "🍰"),
    OfferItem("Happy Hour", "20% off all drinks til 7 PM", "🍹"),
)

/** MVI contract for the Offer attract loop. */
data class OfferUiState(
    val offers: List<OfferItem> = sampleOffers,
    val index: Int = 0,
) : UiState {
    val current: OfferItem? get() = offers.getOrNull(index)
}

sealed interface OfferIntent : UiIntent {
    /** The current slide's progress animation finished — advance to the next. */
    data object SlideCompleted : OfferIntent

    /** The user tapped the screen — leave the attract loop. */
    data object Dismiss : OfferIntent
}

sealed interface OfferSideEffect : UiSideEffect {
    data object Exit : OfferSideEffect
}
