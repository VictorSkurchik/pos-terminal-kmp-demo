package by.vsdev.posterminal.demo.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** Branded restaurant-POS theme: warm Material3 palette, bolder headings, rounded shapes. */
@Composable
fun PosTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        typography = PosTypography,
        shapes = PosShapes,
        content = content,
    )
}
