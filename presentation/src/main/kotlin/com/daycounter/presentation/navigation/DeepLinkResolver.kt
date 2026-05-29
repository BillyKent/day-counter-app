package com.daycounter.presentation.navigation

import android.net.Uri
import androidx.navigation3.runtime.NavKey

/**
 * Resolves the launch `daycounter://counter/{counterId}` deep link (widget / notification taps)
 * into a synthetic Navigation 3 back stack (Principle VI, navigation-contract).
 *
 * - A valid `counterId` → `[Contadores, Detail(id)]` so back/up lands on Contadores (FR-031).
 * - A missing / malformed id → `[Contadores]` (no implicit trust of the intent extra).
 *
 * Existence of the counter is validated separately (the Detail screen navigates back to
 * Contadores when its id resolves to no counter), keeping this resolver pure and synchronous.
 */
object DeepLinkResolver {

    private const val SCHEME = "daycounter"
    private const val HOST = "counter"

    /** Returns the synthetic back stack for the given launch [uri], or `[Contadores]` if absent/invalid. */
    fun resolve(uri: Uri?): List<NavKey> {
        val counterId = parseCounterId(uri) ?: return listOf(Contadores)
        return listOf(Contadores, Detail(counterId))
    }

    private fun parseCounterId(uri: Uri?): Long? {
        if (uri == null) return null
        if (uri.scheme != SCHEME || uri.host != HOST) return null
        val segment = uri.pathSegments.firstOrNull() ?: return null
        return segment.toLongOrNull()?.takeIf { it > 0L }
    }
}
