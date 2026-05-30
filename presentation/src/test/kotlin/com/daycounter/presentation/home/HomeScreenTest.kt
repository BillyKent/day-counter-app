package com.daycounter.presentation.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.daycounter.presentation.theme.DayCounterTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

/**
 * US2 acceptance coverage against the stateless [HomeContent] (Robolectric Compose). Covers the
 * empty-state CTA, the summary header, per-card ring/name/badge, and card/FAB navigation callbacks.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class HomeScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun card(id: Long, streak: Int, target: Int) = CounterCardUi(
        id = id,
        name = "Counter $id",
        startDate = LocalDate.of(2026, 5, 1),
        streakDays = streak,
        goalMilestoneTarget = target,
        ringFillRatio = (streak.toFloat() / target).coerceIn(0f, 1f),
        goalReached = streak >= target,
        category = null,
        isPaused = false,
    )

    @Test
    fun `empty state shows the create CTA`() {
        compose.setContent {
            DayCounterTheme {
                HomeContent(
                    state = HomeUiState(
                        isLoading = false,
                        counters = emptyList(),
                        summary = null,
                        emptyKind = EmptyKind.NO_COUNTERS,
                    ),
                    onCardTap = {},
                    onAddTap = {},
                    onSetFilter = {},
                )
            }
        }
        compose.onNodeWithTag("home_empty_cta").assertIsDisplayed()
    }

    @Test
    fun `summary and cards render with the over-target badge`() {
        compose.setContent {
            DayCounterTheme {
                HomeContent(
                    state = HomeUiState(
                        isLoading = false,
                        counters = listOf(card(1, 3, 30), card(2, 15, 30), card(3, 102, 100)),
                        summary = SummaryUi(totalDays = 120, bestStreak = 102),
                    ),
                    onCardTap = {},
                    onAddTap = {},
                    onSetFilter = {},
                )
            }
        }
        compose.onNodeWithTag("home_summary").assertIsDisplayed()
        // Counter 1 (3 < 30): scroll it into view and confirm it has no badge.
        compose.onNodeWithTag("home_list").performScrollToNode(hasTestTag("counter_card_1"))
        compose.onNodeWithTag("badge_goal_reached_1").assertDoesNotExist()
        // Counter 3 (102 >= 100): scroll it into view and confirm the badge is present.
        compose.onNodeWithTag("home_list").performScrollToNode(hasTestTag("counter_card_3"))
        compose.onNodeWithTag("badge_goal_reached_3").assertIsDisplayed()
    }

    @Test
    fun `card tap and add tap invoke their callbacks`() {
        var tapped: Long? = null
        var added = false
        compose.setContent {
            DayCounterTheme {
                HomeContent(
                    state = HomeUiState(
                        isLoading = false,
                        counters = listOf(card(7, 5, 30)),
                        summary = SummaryUi(totalDays = 5, bestStreak = 5),
                    ),
                    onCardTap = { tapped = it },
                    onAddTap = { added = true },
                    onSetFilter = {},
                )
            }
        }
        compose.onNodeWithTag("counter_card_7").performClick()
        compose.onNodeWithTag("home_add_counter").performClick()
        assertEquals(7L, tapped)
        assertTrue(added)
    }
}
