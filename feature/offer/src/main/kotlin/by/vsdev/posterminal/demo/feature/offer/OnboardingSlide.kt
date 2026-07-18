package by.vsdev.posterminal.demo.feature.offer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import by.vsdev.posterminal.demo.core.ui.components.PillOutlineButton

/**
 * One onboarding slide rendered from an [OfferSlide]: brand [SwooshBackground], a food cutout
 * bleeding off an edge, a 36sp/Bold headline (app font, only size/weight/color from the design), and
 * a single "Tap to continue" CTA in the Sign Up style (always white, as in the design).
 */
@Composable
fun OnboardingSlide(slide: OfferSlide, onContinue: () -> Unit, modifier: Modifier = Modifier) {
    SwooshBackground(color = slide.background, modifier = modifier) {
        Image(
            painter = painterResource(slide.image),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(if (slide.headlineOnTop) Alignment.BottomCenter else Alignment.TopCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.6f),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            if (slide.headlineOnTop) {
                Headline(slide)
                Spacer(Modifier.weight(1f))
            } else {
                Spacer(Modifier.weight(1f))
                Headline(slide)
                Spacer(Modifier.height(24.dp))
            }
            PillOutlineButton(
                text = stringResource(R.string.offer_tap_to_continue),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                contentColor = Color.White,
            )
        }
    }
}

@Composable
private fun Headline(slide: OfferSlide, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = stringResource(slide.headline),
        color = slide.contentColor,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Preview(widthDp = 375, heightDp = 812)
@Composable
private fun OnboardingSlidePreview() {
    OnboardingSlide(slide = sampleOffers.first(), onContinue = {})
}
