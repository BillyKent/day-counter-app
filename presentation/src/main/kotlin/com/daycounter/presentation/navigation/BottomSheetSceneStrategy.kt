package com.daycounter.presentation.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/**
 * Vendored bottom-sheet [SceneStrategy] for Navigation 3.
 *
 * Adapted from the official AndroidX Navigation 3 recipes
 * (https://github.com/android/nav3-recipes — `BottomSheetSceneStrategy`), as the bottom-sheet
 * Scene is not yet part of core Navigation 3 (plan Complexity Tracking). Renders any [NavEntry]
 * carrying [bottomSheet] metadata inside a Material 3 [ModalBottomSheet] over the previous entries.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal class BottomSheetScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable () -> Unit = {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onBack,
            sheetState = sheetState,
        ) {
            entry.Content()
        }
    }
}

class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(
        entries: List<NavEntry<T>>,
    ): Scene<T>? {
        val lastEntry = entries.lastOrNull() ?: return null
        if (!lastEntry.metadata.containsKey(BOTTOM_SHEET_KEY)) return null

        val previous = entries.dropLast(1)
        return BottomSheetScene(
            key = lastEntry.contentKey,
            previousEntries = previous,
            overlaidEntries = previous,
            entry = lastEntry,
            onBack = onBack,
        )
    }

    companion object {
        internal const val BOTTOM_SHEET_KEY = "bottomSheet"

        /** Metadata that marks an entry as a bottom-sheet destination. */
        fun bottomSheet(): Map<String, Any> = mapOf(BOTTOM_SHEET_KEY to true)
    }
}
