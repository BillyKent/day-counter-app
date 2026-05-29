package com.daycounter.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.daycounter.presentation.home.HomeScreen
import com.daycounter.presentation.onboarding.OnboardingScreen
import com.daycounter.presentation.settings.SettingsScreen

/**
 * The single [NavDisplay] that renders the flattened back stack produced by [TopLevelBackStack].
 *
 * Every [NavKey] is mapped to a [NavEntry][androidx.navigation3.runtime.NavEntry] here; user
 * stories US2–US8 replace the temporary placeholders below with their real screens. Bottom-sheet
 * keys carry [BottomSheetSceneStrategy.bottomSheet] metadata so the vendored strategy renders them.
 *
 * Per-entry, saveable, correctly-scoped ViewModels are provided by the
 * `rememberSavedStateNavEntryDecorator` + `rememberViewModelStoreNavEntryDecorator` decorators.
 */
@Composable
fun AppNavDisplay(
    backStack: TopLevelBackStack<NavKey>,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        backStack = backStack.backStack,
        modifier = modifier,
        onBack = { backStack.removeLast() },
        sceneStrategy = BottomSheetSceneStrategy(),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            // ---- Tabs ----
            entry<Contadores> {
                HomeScreen(
                    onCardTap = { id -> backStack.add(Detail(id)) },
                    onAddTap = { backStack.add(CreateCounter) },
                )
            }
            entry<Estadisticas> { Placeholder("stats_screen") } // US5 (T061)
            entry<Ajustes> { SettingsScreen() }

            // ---- Onboarding ----
            entry<Onboarding> {
                OnboardingScreen(onComplete = { backStack.resetTo(Contadores) })
            }

            // ---- Full-screen children ----
            entry<Detail> { Placeholder("counter_detail_screen") } // US3 (T049/T050)
            entry<History> { Placeholder("history_screen") } // US8 (T081/T082)
            entry<Celebration> { Placeholder("celebration_screen") } // US4 (T057)

            // ---- Bottom sheets ----
            entry<CreateCounter>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
                Placeholder("create_counter_sheet") // US6 (T067/T069)
            }
            entry<EditCounter>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
                Placeholder("edit_counter_sheet") // US6 (T068/T069)
            }
            entry<ResetConfirm>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
                Placeholder("reset_confirm_sheet") // US7 (T074/T075)
            }
        },
    )
}

/** Temporary placeholder for screens delivered by later user stories. */
@Composable
private fun Placeholder(tag: String) {
    Box(
        modifier = Modifier.fillMaxSize().testTag(tag),
        contentAlignment = Alignment.Center,
    ) {
        Text(tag)
    }
}
