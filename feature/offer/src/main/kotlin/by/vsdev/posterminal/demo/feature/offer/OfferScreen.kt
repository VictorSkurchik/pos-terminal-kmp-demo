package by.vsdev.posterminal.demo.feature.offer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
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
import by.vsdev.posterminal.demo.core.ui.components.StoryProgressBar
import coil3.compose.AsyncImage

/** One promotional slide. Images are user-provided; null shows a colored placeholder. */
data class OfferItem(
    val title: String,
    val subtitle: String,
    val imageUrl: String? = null,
)

val sampleOffers = listOf(
    OfferItem("2-for-1 Burgers", "Every weekday, 4–6 PM"),
    OfferItem("Free Dessert", "With any main course"),
    OfferItem("Happy Hour", "20% off all drinks til 7 PM"),
)

private const val SLIDE_MILLIS = 5000

/**
 * Full-screen attract loop shown while the terminal is idle in kiosk mode. [offers] rotate every
 * 5 s with Instagram-stories progress bars on top. Any tap calls [onExit] (back to POS).
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
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
    )

    Box(
        modifier
            .fillMaxSize()
            .background(palette[index % palette.size])
            .pointerInput(Unit) { detectTapGestures { onExit() } },
    ) {
        offer.imageUrl?.let {
            AsyncImage(
                model = it,
                contentDescription = offer.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(Modifier.align(Alignment.BottomStart).padding(28.dp)) {
            Text(
                offer.title,
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                offer.subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f),
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
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp),
        )
    }
}
