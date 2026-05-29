package com.daycounter.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A circular progress ring whose arc fills `min(1, days / target)` of the circle.
 *
 * Progress is conveyed by the swept arc *and* an explicit [contentDescription] (the days/target
 * are typically also shown as text in the center slot) — never by color alone (Principle I).
 *
 * @param days Current streak day count.
 * @param target Goal milestone target (ring denominator); coerced to at least 1.
 * @param contentDescription Spoken description of the progress (required, non-null).
 * @param center Optional center content (e.g. the streak number).
 */
@Composable
fun ProgressRing(
    days: Int,
    target: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    diameter: Dp = 96.dp,
    strokeWidth: Dp = 10.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    center: @Composable (() -> Unit)? = null,
) {
    val safeTarget = target.coerceAtLeast(1)
    val fraction = (days.toFloat() / safeTarget).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .size(diameter)
            .clearAndSetSemantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(diameter)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = strokeWidth.toPx() / 2f
            val arcSize = androidx.compose.ui.geometry.Size(
                width = size.width - strokeWidth.toPx(),
                height = size.height - strokeWidth.toPx(),
            )
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            // Full track.
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            // Progress arc, starting from 12 o'clock clockwise.
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * fraction,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
        center?.invoke()
    }
}
