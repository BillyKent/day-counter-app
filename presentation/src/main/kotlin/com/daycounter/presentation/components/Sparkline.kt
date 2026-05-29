package com.daycounter.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A minimal sparkline: a polyline over per-day streak [values] (one point per day since the
 * counter's start date). A single point (same-day counter) is drawn as a dot.
 *
 * The visual is decorative; the spoken summary is supplied via [contentDescription] so screen
 * readers convey the trend without relying on the drawing (Principle I).
 */
@Composable
fun Sparkline(
    values: List<Int>,
    contentDescription: String,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 2.dp,
) {
    Canvas(
        modifier = modifier
            .height(height)
            .clearAndSetSemantics { this.contentDescription = contentDescription },
    ) {
        if (values.isEmpty()) return@Canvas
        val maxValue = (values.maxOrNull() ?: 0).coerceAtLeast(1)
        val strokePx = strokeWidth.toPx()
        val usableHeight = size.height - strokePx
        val usableWidth = size.width - strokePx
        val half = strokePx / 2f

        fun yFor(value: Int): Float = half + usableHeight * (1f - value.toFloat() / maxValue)

        if (values.size == 1) {
            drawCircle(
                color = lineColor,
                radius = strokePx,
                center = Offset(size.width / 2f, yFor(values.first())),
            )
            return@Canvas
        }

        val stepX = usableWidth / (values.size - 1)
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = half + stepX * index
            val y = yFor(value)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
    }
}
