package by.vsdev.posterminal.demo.feature.offer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import by.vsdev.posterminal.demo.core.ui.mvi.UiIntent
import by.vsdev.posterminal.demo.core.ui.mvi.UiSideEffect
import by.vsdev.posterminal.demo.core.ui.mvi.UiState

// Brand colors taken from the Comida onboarding designs.
private val Coral = Color(0xFFFF6B57)
private val Mint = Color(0xFF7BD3A5)
private val Yellow = Color(0xFFFFC529)
private val Ink = Color(0xFF24272F)

/** One onboarding slide: a food cutout on a brand color with a headline and a single CTA. */
data class OfferSlide(
    @StringRes val headline: Int,
    @DrawableRes val image: Int,
    val background: Color,
    val contentColor: Color,
    val headlineOnTop: Boolean,
)

val sampleOffers = listOf(
    OfferSlide(R.string.offer_headline_1, R.drawable.offer_pizza, Coral, Ink, headlineOnTop = false),
    OfferSlide(R.string.offer_headline_2, R.drawable.offer_donut, Mint, Color.White, headlineOnTop = true),
    OfferSlide(R.string.offer_headline_3, R.drawable.offer_burger, Yellow, Ink, headlineOnTop = false),
)

/** MVI contract for the Offer attract loop. */
data class OfferUiState(val offers: List<OfferSlide> = sampleOffers, val index: Int = 0) : UiState {
    val current: OfferSlide? get() = offers.getOrNull(index)
}

sealed interface OfferIntent : UiIntent {
    /** The current slide's progress animation finished — advance to the next. */
    data object SlideCompleted : OfferIntent

    /** The user tapped "Tap to continue" (or the background) — leave the attract loop. */
    data object Dismiss : OfferIntent
}

sealed interface OfferSideEffect : UiSideEffect {
    data object Exit : OfferSideEffect
}
