package by.vsdev.posterminal.demo.feature.offer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.vsdev.posterminal.demo.core.ui.components.StoryProgressBar
import by.vsdev.posterminal.demo.core.ui.theme.PosTheme
import coil3.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel

private const val SLIDE_MILLIS = 5000

// Scattered decorative motif: (xFraction, yFraction, sizeSp) — small emojis of varied sizes.
private val scatter = listOf(
    Triple(0.06f, 0.08f, 40), Triple(0.34f, 0.05f, 30), Triple(0.60f, 0.10f, 52),
    Triple(0.84f, 0.07f, 34), Triple(0.16f, 0.24f, 60), Triple(0.46f, 0.20f, 30),
    Triple(0.74f, 0.26f, 44), Triple(0.90f, 0.22f, 28), Triple(0.08f, 0.44f, 48),
    Triple(0.40f, 0.42f, 34), Triple(0.66f, 0.46f, 56), Triple(0.88f, 0.42f, 30),
    Triple(0.20f, 0.60f, 38), Triple(0.52f, 0.64f, 30), Triple(0.80f, 0.66f, 46),
    Triple(0.30f, 0.80f, 34),
)

/**
 * Full-screen attract loop shown while the terminal is idle in kiosk mode. Slides rotate every 5 s
 * with Instagram-stories progress bars on top; a tap exits. The slide index lives in [OfferViewModel];
 * the composable only animates the progress bar and reports completion / taps as intents.
 */
@Composable
fun OfferScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OfferViewModel = koinViewModel(),
) {
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
private fun OfferContent(
    state: OfferUiState,
    onIntent: (OfferIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val offer = state.current ?: return
    val progress = remember { Animatable(0f) }

    LaunchedEffect(state.index) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = SLIDE_MILLIS, easing = LinearEasing))
        onIntent(OfferIntent.SlideCompleted)
    }

    val scheme = MaterialTheme.colorScheme
    // Container + matching on-container colors keep text legible in both light and dark themes.
    val palette = remember(scheme) {
        listOf(
            scheme.primaryContainer to scheme.onPrimaryContainer,
            scheme.secondaryContainer to scheme.onSecondaryContainer,
            scheme.tertiaryContainer to scheme.onTertiaryContainer,
        )
    }
    val (background, onColor) = palette[state.index % palette.size]

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(background)
            .pointerInput(Unit) { detectTapGestures { onIntent(OfferIntent.Dismiss) } },
    ) {
        val w = maxWidth
        val h = maxHeight

        if (offer.imageUrl != null) {
            AsyncImage(
                model = offer.imageUrl,
                contentDescription = offer.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            scatter.forEach { (fx, fy, size) ->
                Text(
                    text = offer.emoji,
                    fontSize = size.sp,
                    modifier = Modifier
                        .offset(x = w * fx, y = h * fy)
                        .alpha(0.85f),
                )
            }
        }

        Surface(
            color = Color.Black.copy(alpha = 0.28f),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, end = 24.dp, bottom = 56.dp),
        ) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                Text(
                    offer.title,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = onColor,
                )
                Text(
                    offer.subtitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = onColor.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

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

        Text(
            stringResource(R.string.offer_tap_to_exit),
            style = MaterialTheme.typography.labelMedium,
            color = onColor.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        )
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun OfferPreview() {
    PosTheme {
        OfferContent(state = OfferUiState(), onIntent = {})
    }
}
