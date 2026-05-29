package com.daycounter.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import com.daycounter.presentation.R

/**
 * Hosts [AppNavDisplay] inside a [Scaffold] whose [NavigationBar] is shown **only when the
 * currently visible entry is one of the three tab keys** (FR-001/FR-002). Detail, History,
 * Celebration and the bottom sheets therefore hide the bar. The selected item tracks
 * [TopLevelBackStack.topLevelKey].
 */
@Composable
fun MainScaffold(backStack: TopLevelBackStack<NavKey>) {
    val showBar = backStack.backStack.lastOrNull() in TOP_LEVEL_KEYS

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar(modifier = Modifier.testTag("bottom_navigation_bar")) {
                    TOP_LEVEL_KEYS.forEach { key ->
                        NavigationBarItem(
                            selected = key == backStack.topLevelKey,
                            onClick = { backStack.addTopLevel(key) },
                            icon = { Icon(iconFor(key), contentDescription = null) },
                            label = { Text(stringResource(labelResFor(key))) },
                            modifier = Modifier.testTag("tab_${tagFor(key)}"),
                        )
                    }
                }
            }
        },
    ) { padding ->
        AppNavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(padding),
        )
    }
}

private fun iconFor(key: NavKey): ImageVector = when (key) {
    Contadores -> Icons.AutoMirrored.Filled.List
    Estadisticas -> Icons.Filled.Insights
    else -> Icons.Filled.Settings
}

private fun labelResFor(key: NavKey): Int = when (key) {
    Contadores -> R.string.tab_counters
    Estadisticas -> R.string.tab_stats
    else -> R.string.tab_settings
}

private fun tagFor(key: NavKey): String = when (key) {
    Contadores -> "counters"
    Estadisticas -> "stats"
    else -> "settings"
}
