package com.daycounter.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.daycounter.presentation.celebration.MilestoneCelebrationScreen
import com.daycounter.presentation.counter.CounterDetailActions
import com.daycounter.presentation.counter.CounterDetailScreen
import com.daycounter.presentation.counter.CreateCounterSheet
import com.daycounter.presentation.counter.EditCounterSheet
import com.daycounter.presentation.counter.ResetConfirmSheet
import com.daycounter.presentation.history.HistoryScreen
import com.daycounter.presentation.home.HomeScreen
import com.daycounter.presentation.onboarding.OnboardingScreen
import com.daycounter.presentation.settings.SettingsScreen
import com.daycounter.presentation.stats.StatsScreen

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
            entry<Estadisticas> { StatsScreen() }
            entry<Ajustes> { SettingsScreen() }

            // ---- Onboarding ----
            entry<Onboarding> {
                OnboardingScreen(onComplete = { backStack.resetTo(Contadores) })
            }

            // ---- Full-screen children ----
            entry<Detail> { key ->
                CounterDetailScreen(
                    counterId = key.counterId,
                    actions = CounterDetailActions(
                        onBack = { backStack.removeLast() },
                        onEdit = { backStack.add(EditCounter(it)) },
                        onReset = { backStack.add(ResetConfirm(it)) },
                        onHistory = { backStack.add(History(it)) },
                        onCelebration = { id, milestone -> backStack.add(Celebration(id, milestone)) },
                        onExitToContadores = { backStack.popToBase() },
                    ),
                )
            }
            entry<History> { key ->
                HistoryScreen(counterId = key.counterId, onBack = { backStack.removeLast() })
            }
            entry<Celebration> { key ->
                MilestoneCelebrationScreen(
                    counterId = key.counterId,
                    milestone = key.milestone,
                    onClose = { backStack.removeLast() },
                )
            }

            // ---- Bottom sheets ----
            entry<CreateCounter>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
                CreateCounterSheet(onDismiss = { backStack.removeLast() })
            }
            entry<EditCounter>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
                EditCounterSheet(counterId = key.counterId, onDismiss = { backStack.removeLast() })
            }
            entry<ResetConfirm>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
                ResetConfirmSheet(counterId = key.counterId, onDismiss = { backStack.removeLast() })
            }
        },
    )
}
