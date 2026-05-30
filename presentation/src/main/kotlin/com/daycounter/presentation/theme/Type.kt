package com.daycounter.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// Brand type pairing (research R9): Outfit for display + the hero numeral; Plus Jakarta Sans for body.
// TODO(T001): bundle Outfit and Plus Jakarta Sans .ttf under res/font (Latin subset) and replace
// FontFamily.Default below with the loaded families. The type SCALE (sizes/weights/spacing) below is
// already the brand scale; swapping the family is a drop-in once the font assets are added.
internal val DisplayFamily = FontFamily.Default // → Outfit
internal val BodyFamily = FontFamily.Default // → Plus Jakarta Sans

/**
 * Style for the large tabular streak numeral (the "hero"). Used by Detail/Stats/Celebration.
 */
val HeroNumeralStyle = TextStyle(
    fontFamily = DisplayFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 96.sp,
    lineHeight = 96.sp,
    letterSpacing = (-0.04).em,
)

internal val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 44.sp,
        lineHeight = 48.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)
