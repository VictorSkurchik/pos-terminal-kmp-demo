package by.vsdev.posterminal.demo.feature.offer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import by.vsdev.posterminal.demo.core.ui.components.StoryProgressBar
import coil3.compose.AsyncImage

/** One promotional slide. Real images are user-provided; [emoji] is the large placeholder. */
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

private const val SLIDE_MILLIS = 5000

/**
 * Full-screen attract loop shown while the terminal is idle in kiosk mode. [offers] rotate every
 * 5 s with Instagram-stories progress bars on top; a large image/emoji peeks in from a different
 * edge each slide. Any tap calls [onExit].
 */
@Composable
fun OfferScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    offers: List<OfferItem> = sampleOffers,
) {
    if (offers.isEmpty()) return

    var index by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(index) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = SLIDE_MILLIS, easing = LinearEasing))
        index = (index + 1) % offers.size
    }

    val offer = offers[index]
    val palette = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
    )
    val onColor = Color.Black.copy(alpha = 0.82f)

    Box(
        modifier
            .fillMaxSize()
            .background(palette[index % palette.size])
            .pointerInput(Unit) { detectTapGestures { onExit() } },
    ) {
        OfferArt(offer, index)

        Column(Modifier.align(Alignment.BottomStart).padding(32.dp)) {
            Text(
                offer.title,
                fontSize = 46.sp,
                fontWeight = FontWeight.Bold,
                color = onColor,
                lineHeight = 50.sp,
            )
            Text(
                offer.subtitle,
                fontSize = 24.sp,
                color = onColor.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            offers.forEachIndexed { i, _ ->
                StoryProgressBar(
                    progress = when {
                        i < index -> 1f
                        i == index -> progress.value
                        else -> 0f
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Text(
            "tap to exit",
            fontSize = 13.sp,
            color = onColor.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        )
    }
}

/** A large (~half-screen) image/emoji that peeks in from a different edge each slide. */
@Composable
private fun androidx.compose.foundation.layout.BoxScope.OfferArt(offer: OfferItem, index: Int) {
    // Alignment + outward offset so roughly half of the art sits off-screen; the edge rotates.
    val (alignment, dx, dy) = when (index % 4) {
        0 -> Triple(Alignment.CenterEnd, 130.dp, 0.dp)      // from the right
        1 -> Triple(Alignment.CenterStart, (-130).dp, 0.dp) // from the left
        2 -> Triple(Alignment.TopCenter, 0.dp, (-120).dp)   // from the top
        else -> Triple(Alignment.BottomCenter, 0.dp, 120.dp) // from the bottom
    }
    val art = Modifier.align(alignment).offset(x = dx, y = dy)
    if (offer.imageUrl != null) {
        AsyncImage(
            model = offer.imageUrl,
            contentDescription = offer.title,
            contentScale = ContentScale.Fit,
            modifier = art.fillMaxSize(0.7f),
        )
    } else {
        Text(offer.emoji, fontSize = 280.sp, modifier = art)
    }
}
