package com.daycounter.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Type-safe Navigation 3 route keys. Every destination is a `@Serializable` object/class
 * implementing [NavKey] — there are no string routes. Replaces the Navigation 2 `Screen` sealed
 * class. See `contracts/navigation-contract.md`.
 */

// ---- Top-level (tab) keys — each owns its own back stack ----

/** Counters tab. Start key; the app exits through this tab. */
@Serializable
data object Contadores : NavKey

/** Statistics tab. */
@Serializable
data object Estadisticas : NavKey

/** Settings tab; hosts the milestone-notification toggle. */
@Serializable
data object Ajustes : NavKey

// ---- One-time onboarding (rendered before the tab shell) ----

@Serializable
data object Onboarding : NavKey

// ---- Full-screen child keys (hide the bottom bar) ----

/** Counter detail; also the deep-link target. */
@Serializable
data class Detail(val counterId: Long) : NavKey

/** Per-counter history / calendar; reachable only from [Detail]. */
@Serializable
data class History(val counterId: Long) : NavKey

/** Full-screen milestone celebration overlay (auto-launch + Revivir). */
@Serializable
data class Celebration(val counterId: Long, val milestone: Int) : NavKey

// ---- Bottom-sheet keys (entries carrying BottomSheetSceneStrategy metadata) ----

@Serializable
data object CreateCounter : NavKey

@Serializable
data class EditCounter(val counterId: Long) : NavKey

@Serializable
data class ResetConfirm(val counterId: Long) : NavKey

/** The three top-level tab keys, in display order. */
val TOP_LEVEL_KEYS: List<NavKey> = listOf(Contadores, Estadisticas, Ajustes)
