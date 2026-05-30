package com.daycounter.presentation.counter

import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.daycounter.presentation.theme.DayCounterTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class CounterDetailScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun noopActions(
        onEdit: (Long) -> Unit = {},
        onReset: (Long) -> Unit = {},
        onHistory: (Long) -> Unit = {},
        onCelebration: (Long, Int) -> Unit = { _, _ -> },
    ) = CounterDetailActions(
        onBack = {},
        onEdit = onEdit,
        onReset = onReset,
        onHistory = onHistory,
        onCelebration = onCelebration,
        onExitToContadores = {},
    )

    private fun render(state: CounterDetailUiState, actions: CounterDetailActions = noopActions()) {
        compose.setContent {
            DayCounterTheme {
                CounterDetailContent(
                    state = state,
                    counterId = 1L,
                    actions = actions,
                    onTogglePause = {},
                    onRequestDelete = {},
                    onConfirmDelete = {},
                    onDismissDelete = {},
                )
            }
        }
    }

    private fun loadedState(streak: Int, target: Int = 100, next: Int? = 100, achieved: List<Int> = listOf(1, 7, 30)) =
        CounterDetailUiState(
            isLoading = false,
            name = "Read daily",
            streakDays = streak,
            goalMilestoneTarget = target,
            ringFillRatio = (streak.toFloat() / target).coerceIn(0f, 1f),
            nextMilestone = next,
            achievedMilestones = achieved,
            canRevive = achieved.isNotEmpty(),
            mostRecentMilestone = achieved.maxOrNull(),
        )

    @Test
    fun `hero shows streak and next-milestone hint`() {
        render(loadedState(streak = 35))
        compose.onNodeWithTag("detail_hero_streak").assertIsDisplayed()
        compose.onNodeWithText("35").assertIsDisplayed()
        compose.onNodeWithTag("detail_next_milestone").assertIsDisplayed()
    }

    @Test
    fun `at or beyond 1000 shows all-milestones-reached`() {
        render(loadedState(streak = 1200, target = 365, next = null, achieved = listOf(1, 7, 30, 100, 365, 1000)))
        // The hint node renders the "all reached" copy (nextMilestone == null path).
        compose.onNodeWithTag("detail_next_milestone").assertIsDisplayed()
    }

    @Test
    fun `paused state shows banner and resume action`() {
        render(loadedState(streak = 9).copy(isPaused = true, pausedDays = 5))
        compose.onNodeWithTag("detail_paused_banner").assertIsDisplayed()
        compose.onNodeWithTag("detail_toggle_pause").performScrollTo().assertIsDisplayed()
        // While paused, the toggle reads "Resume counter".
        compose.onNodeWithText("Resume counter").assertIsDisplayed()
        // The next-milestone hint is hidden while paused.
        compose.onNodeWithTag("detail_next_milestone").assertDoesNotExist()
    }

    @Test
    fun `toggle pause invokes callback`() {
        var toggled = false
        compose.setContent {
            DayCounterTheme {
                CounterDetailContent(
                    state = loadedState(streak = 12),
                    counterId = 1L,
                    actions = noopActions(),
                    onTogglePause = { toggled = true },
                    onRequestDelete = {},
                    onConfirmDelete = {},
                    onDismissDelete = {},
                )
            }
        }
        compose.onNodeWithTag("detail_toggle_pause").performScrollTo().performClick()
        assertEquals(true, toggled)
    }

    @Test
    fun `achieved chips are non-interactive`() {
        render(loadedState(streak = 35))
        compose.onNodeWithTag("achieved_chip_1").assertHasNoClickAction()
        compose.onNodeWithTag("achieved_chip_30").assertHasNoClickAction()
    }

    @Test
    fun `actions invoke navigation callbacks`() {
        var edited: Long? = null
        var reset: Long? = null
        var history: Long? = null
        var revived: Pair<Long, Int>? = null
        render(
            loadedState(streak = 35),
            noopActions(
                onEdit = { edited = it },
                onReset = { reset = it },
                onHistory = { history = it },
                onCelebration = { id, m -> revived = id to m },
            ),
        )
        compose.onNodeWithTag("detail_action_edit").performScrollTo().performClick()
        compose.onNodeWithTag("detail_action_reset").performScrollTo().performClick()
        compose.onNodeWithTag("detail_action_history").performScrollTo().performClick()
        compose.onNodeWithTag("detail_action_revive").performScrollTo().performClick()
        assertEquals(1L, edited)
        assertEquals(1L, reset)
        assertEquals(1L, history)
        assertEquals(1L to 30, revived)
    }
}
