package by.vsdev.posterminal.demo.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pill CTA in the "Sign Up" onboarding style: stadium shape, a 1px outline in [contentColor], a
 * subtly translucent fill of the same color, and centered 16sp / SemiBold text. The font family is
 * the app default — only size, weight and color are taken from the design.
 */
@Composable
fun PillOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        enabled = enabled,
        shape = RoundedCornerShape(percent = 50),
        border = BorderStroke(1.dp, contentColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
            containerColor = contentColor.copy(alpha = 0.12f),
        ),
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(backgroundColor = 0xFFFF6B57, showBackground = true)
@Composable
private fun PillOutlineButtonPreview() {
    PillOutlineButton(text = "Tap to continue", onClick = {})
}
