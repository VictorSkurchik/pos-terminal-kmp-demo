package by.vsdev.posterminal.demo.feature.offer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Solid brand-color background with the Comida "swoosh": a few thin concentric arcs sweeping from
 * the lower-left, in a slightly lighter shade. Drawn on a Canvas so it's crisp at any density and
 * needs no asset.
 */
@Composable
fun SwooshBackground(
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(color)
                val center = Offset(size.width * 0.12f, size.height * 0.98f)
                val stroke = Stroke(width = size.minDimension * 0.055f)
                val tint = Color.White.copy(alpha = 0.06f)
                for (i in 0..3) {
                    drawCircle(
                        color = tint,
                        radius = size.width * (0.55f + i * 0.30f),
                        center = center,
                        style = stroke,
                    )
                }
            },
        content = content,
    )
}
