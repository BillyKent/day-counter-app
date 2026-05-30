package com.daycounter.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.daycounter.presentation.theme.LocalDayCounterColors

/**
 * A circular progress ring whose arc fills `min(1, days / target)` of the circle.
 *
 * Progress and state are conveyed by the swept arc, the ring *style* (a paused ring is dashed and
 * muted), and an explicit [contentDescription] — never by color alone (Principle I). A [milestone]
 * ring uses the terracotta accent plus a soft glow halo.
 *
 * @param days Current effective streak day count.
 * @param target Goal milestone target (ring denominator); coerced to at least 1.
 * @param contentDescription Spoken description of the progress (required, non-null).
 * @param paused When true, renders a muted, dashed, fully-frozen ring (US2).
 * @param milestone When true, renders the milestone accent + glow.
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
    paused: Boolean = false,
    milestone: Boolean = false,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    center: @Composable (() -> Unit)? = null,
) {
    val dc = LocalDayCounterColors.current
    val safeTarget = target.coerceAtLeast(1)
    val fraction = (days.toFloat() / safeTarget).coerceIn(0f, 1f)
    val arcColor = when {
        paused -> dc.pausedRing
        milestone -> dc.milestone
        else -> progressColor
    }

    Box(
        modifier = modifier.size(diameter),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .size(diameter)
                .semantics { this.contentDescription = contentDescription },
        ) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val dashedStroke = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(strokeWidth.toPx() * 1.6f, strokeWidth.toPx() * 1.4f),
                ),
            )
            val inset = strokeWidth.toPx() / 2f
            val arcSize = androidx.compose.ui.geometry.Size(
                width = size.width - strokeWidth.toPx(),
                height = size.height - strokeWidth.toPx(),
            )
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)

            // Milestone glow halo behind the ring.
            if (milestone) {
                drawArc(
                    color = dc.milestone.copy(alpha = 0.18f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth.toPx() * 2.2f, cap = StrokeCap.Round),
                )
            }
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
            // Progress arc (12 o'clock clockwise). Paused → dashed + muted.
            drawArc(
                color = arcColor,
                startAngle = -90f,
                sweepAngle = 360f * fraction,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = if (paused) dashedStroke else stroke,
            )
        }
        center?.invoke()
    }
}
