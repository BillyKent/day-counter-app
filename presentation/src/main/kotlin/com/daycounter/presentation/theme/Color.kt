package com.daycounter.presentation.theme

import androidx.compose.ui.graphics.Color

// ─── Claude Design brand palette (research R1/R2) ────────────────────────────
// All MD3 colors are sourced here (Principle I: no hardcoded colors in screens). Dynamic color
// (Material You) is intentionally disabled in favor of this fixed brand palette — sanctioned by
// constitution v2.3.0.

// Light — MD3 roles
internal val Primary = Color(0xFF0F5F6E)            // deep teal brand
internal val OnPrimary = Color(0xFFFFFFFF)
internal val PrimaryContainer = Color(0xFFD8EAEC)   // brand-soft
internal val OnPrimaryContainer = Color(0xFF08404B)
internal val Secondary = Color(0xFF6FA88B)          // sage — streak growing
internal val OnSecondary = Color(0xFFFFFFFF)
internal val SecondaryContainer = Color(0xFFE1EDE3)
internal val OnSecondaryContainer = Color(0xFF0B2A1E)
internal val Tertiary = Color(0xFFD9876A)           // terracotta — milestone
internal val OnTertiary = Color(0xFFFFFFFF)

internal val ErrorColor = Color(0xFFC97062)         // muted terracotta — danger
internal val OnError = Color(0xFFFFFFFF)

internal val BackgroundLight = Color(0xFFFBF6EE)    // warm cream
internal val OnBackgroundLight = Color(0xFF1B2A33)
internal val SurfaceLight = Color(0xFFFBF6EE)
internal val OnSurfaceLight = Color(0xFF1B2A33)
internal val SurfaceVariantLight = Color(0xFFF4EDDF) // sunken
internal val OnSurfaceVariantLight = Color(0xFF5F7079)
internal val OutlineLight = Color(0xFFD6CCB8)

// Dark — warm near-black derivation (research R2)
internal val PrimaryDark = Color(0xFF5FD0DE)
internal val OnPrimaryDark = Color(0xFF003640)
internal val PrimaryContainerDark = Color(0xFF0B4E5B)
internal val OnPrimaryContainerDark = Color(0xFFB8ECF2)
internal val SecondaryDark = Color(0xFF92C9AC)
internal val OnSecondaryDark = Color(0xFF0B2A1E)
internal val SecondaryContainerDark = Color(0xFF31463B)
internal val OnSecondaryContainerDark = Color(0xFFCDEBD7)
internal val TertiaryDark = Color(0xFFF0A98E)
internal val OnTertiaryDark = Color(0xFF4B1E0E)

internal val BackgroundDark = Color(0xFF14181A)
internal val OnBackgroundDark = Color(0xFFE7EEF0)
internal val SurfaceDark = Color(0xFF14181A)
internal val OnSurfaceDark = Color(0xFFE7EEF0)
internal val SurfaceVariantDark = Color(0xFF1F2A2E)
internal val OnSurfaceVariantDark = Color(0xFFA9BBC0)
internal val OutlineDark = Color(0xFF49585C)
