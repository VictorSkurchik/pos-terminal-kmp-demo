package by.vsdev.posterminal.demo.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val base = Typography()

/** Slightly bolder, tighter headings for a confident POS look; body stays Material3 default. */
val PosTypography = Typography(
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp),
    // Keep the default font family/metrics; only make it emphatic for the branded display style.
    displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Bold),
)
