package com.daycounter.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey

/**
 * Holds one back stack per top-level (tab) key and exposes a single flattened [backStack] to the
 * `NavDisplay`. Adapted from the official Navigation 3 multi-back-stack "common UI" recipe.
 *
 * - [addTopLevel] switches tab, preserving that tab's history (or seeding it).
 * - [add] pushes a child destination onto the current tab.
 * - [removeLast] pops; when a tab's base key is popped, falls back to the previous top-level key.
 * - [resetTo] wipes everything to a single tab (onboarding → Contadores handoff, FR-004).
 * - [popToBase] trims the current tab to its base key (delete-from-Detail → Contadores, FR-012).
 */
class TopLevelBackStack<T : NavKey>(startKey: T) {

    private val topLevelStacks: LinkedHashMap<T, MutableList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey),
    )

    var topLevelKey: T by mutableStateOf(startKey)
        private set

    val backStack: MutableList<T> = mutableStateListOf(startKey)

    private fun updateBackStack() {
        backStack.clear()
        backStack.addAll(topLevelStacks.flatMap { it.value })
    }

    fun addTopLevel(key: T) {
        if (topLevelStacks[key] == null) {
            topLevelStacks[key] = mutableStateListOf(key)
        } else {
            // Move the existing stack to the end so its history is preserved and surfaced.
            topLevelStacks.remove(key)?.let { topLevelStacks[key] = it }
        }
        topLevelKey = key
        updateBackStack()
    }

    fun add(key: T) {
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    fun removeLast() {
        val removedKey = topLevelStacks[topLevelKey]?.removeLastOrNull()
        // If we popped a tab's base key, drop that whole stack and fall back to the previous tab.
        if (topLevelStacks[topLevelKey]?.isEmpty() == true) {
            topLevelStacks.remove(removedKey)
            topLevelStacks.keys.lastOrNull()?.let { topLevelKey = it }
        }
        updateBackStack()
    }

    /** Replaces all tab stacks with a single [key] tab (used for the onboarding → tabs handoff). */
    fun resetTo(key: T) {
        topLevelStacks.clear()
        topLevelStacks[key] = mutableStateListOf(key)
        topLevelKey = key
        updateBackStack()
    }

    /** Trims the current tab back to its base key, discarding any pushed children. */
    fun popToBase() {
        val stack = topLevelStacks[topLevelKey] ?: return
        val base = stack.firstOrNull() ?: return
        stack.clear()
        stack.add(base)
        updateBackStack()
    }
}
