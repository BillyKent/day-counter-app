package com.daycounter.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Brand/semantic colors that have no direct Material 3 role. Layered on top of `MaterialTheme` and
 * provided via [LocalDayCounterColors]. Screens read these instead of hardcoding hex (Principle I).
 *
 * State meaning (paused, milestone) is always reinforced with text/shape, never color alone.
 */
@Immutable
data class DayCounterColors(
    val card: Color,            // elevated card fill over the cream background
    val sunken: Color,          // grouped/sunken section fill
    val sage: Color,            // streak growing
    val sageSoft: Color,
    val milestone: Color,       // milestone reached
    val milestoneSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val brandSoft: Color,
    val brandSofter: Color,
    val pausedRing: Color,      // muted ring used for a paused counter
    val ringGradientStart: Color,
    val ringGradientEnd: Color,
)

internal val LightDayCounterColors = DayCounterColors(
    card = Color(0xFFFFFFFF),
    sunken = Color(0xFFF4EDDF),
    sage = Color(0xFF6FA88B),
    sageSoft = Color(0xFFE1EDE3),
    milestone = Color(0xFFD9876A),
    milestoneSoft = Color(0xFFFBE7DD),
    warning = Color(0xFFD9A05B),
    warningSoft = Color(0xFFFAEED7),
    danger = Color(0xFFC97062),
    dangerSoft = Color(0xFFF6DDD8),
    brandSoft = Color(0xFFD8EAEC),
    brandSofter = Color(0xFFECF4F5),
    pausedRing = Color(0xFF8B9AA3),
    ringGradientStart = Color(0xFF0F5F6E),
    ringGradientEnd = Color(0xFF2A8597),
)

internal val DarkDayCounterColors = DayCounterColors(
    card = Color(0xFF1B2226),
    sunken = Color(0xFF1F2A2E),
    sage = Color(0xFF92C9AC),
    sageSoft = Color(0xFF243A30),
    milestone = Color(0xFFF0A98E),
    milestoneSoft = Color(0xFF3A241B),
    warning = Color(0xFFE6B877),
    warningSoft = Color(0xFF3A2F1C),
    danger = Color(0xFFE69486),
    dangerSoft = Color(0xFF3A211C),
    brandSoft = Color(0xFF0B4E5B),
    brandSofter = Color(0xFF123A42),
    pausedRing = Color(0xFF6A7B82),
    ringGradientStart = Color(0xFF2A8597),
    ringGradientEnd = Color(0xFF5FD0DE),
)

/** Access the brand/extended palette: `LocalDayCounterColors.current`. */
val LocalDayCounterColors = staticCompositionLocalOf { LightDayCounterColors }
