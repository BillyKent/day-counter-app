package com.daycounter.presentation.theme

import com.daycounter.domain.model.AppearanceMode

/**
 * Resolves the effective dark-theme flag for an [AppearanceMode] (US7).
 *
 * - [AppearanceMode.SYSTEM] follows [systemInDark].
 * - [AppearanceMode.LIGHT] / [AppearanceMode.DARK] override it.
 */
fun AppearanceMode.resolveDarkTheme(systemInDark: Boolean): Boolean = when (this) {
    AppearanceMode.SYSTEM -> systemInDark
    AppearanceMode.LIGHT -> false
    AppearanceMode.DARK -> true
}
