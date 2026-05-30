package com.daycounter.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Brand shape scale (research R1): generous, curved radii.
 * - extraSmall 8, small 12, medium 16 (inputs/chips), large 24 (cards), extraLarge 32 (sheets).
 * Pill buttons use [PillShape].
 */
internal val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Full pill (100%) used for primary/secondary buttons and chips. */
val PillShape = RoundedCornerShape(percent = 50)

/** Top-rounded 32dp shape for bottom sheets. */
val SheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
