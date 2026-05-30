package com.daycounter.presentation.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Teal-tinted card elevation (research R1). Brand shadows are tinted teal, never pure black.
 *
 * @param elevation shadow blur/spread.
 * @param shape clip/shadow shape (defaults to the 24dp card squircle).
 * @param tint ambient/spot shadow color (defaults to the brand teal).
 */
fun Modifier.cardShadow(
    elevation: Dp = 12.dp,
    shape: Shape = RoundedCornerShape(24.dp),
    tint: Color = Color(0xFF0F5F6E),
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = tint,
    spotColor = tint,
)
