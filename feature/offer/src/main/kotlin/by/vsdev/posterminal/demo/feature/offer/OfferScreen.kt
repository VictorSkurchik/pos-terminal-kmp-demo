package by.vsdev.posterminal.demo.feature.offer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.vsdev.posterminal.demo.core.ui.components.StoryProgressBar
import by.vsdev.posterminal.demo.core.ui.theme.PosTheme
import org.koin.androidx.compose.koinViewModel

private const val SLIDE_MILLIS = 5000

/**
 * Full-screen attract loop shown while the terminal is idle in kiosk mode. Slides auto-advance every
 * 5 s with Instagram-stories progress bars on top; a tap anywhere (or the "Tap to continue" CTA)
 * returns to POS. Each slide's visuals come from [OnboardingSlide]; the index/advance lives in
 * [OfferViewModel].
 */
@Composable
fun OfferScreen(onExit: () -> Unit, modifier: Modifier = Modifier, viewModel: OfferViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                OfferSideEffect.Exit -> onExit()
            }
        }
    }

    OfferContent(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

@Composable
private fun OfferContent(state: OfferUiState, onIntent: (OfferIntent) -> Unit, modifier: Modifier = Modifier) {
    val slide = state.current ?: return
    val progress = remember { Animatable(0f) }

    LaunchedEffect(state.index) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = SLIDE_MILLIS, easing = LinearEasing))
        onIntent(OfferIntent.SlideCompleted)
    }

    Box(
        modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { onIntent(OfferIntent.Dismiss) } },
    ) {
        OnboardingSlide(
            slide = slide,
            onContinue = { onIntent(OfferIntent.Dismiss) },
            modifier = Modifier.fillMaxSize(),
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            state.offers.forEachIndexed { i, _ ->
                StoryProgressBar(
                    progress = when {
                        i < state.index -> 1f
                        i == state.index -> progress.value
                        else -> 0f
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(widthDp = 375, heightDp = 812)
@Composable
private fun OfferPreview() {
    PosTheme {
        OfferContent(state = OfferUiState(), onIntent = {})
    }
}
