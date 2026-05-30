package com.daycounter.presentation.stats

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.daycounter.presentation.theme.DayCounterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class StatsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `renders hero total and two secondary metrics`() {
        // counters 5/30/120 -> total 155, best 120, active 3
        compose.setContent {
            DayCounterTheme {
                StatsContent(
                    StatsUiState(isLoading = false, isEmpty = false, totalAccumulated = 155, bestStreak = 120, activeCounters = 3),
                )
            }
        }
        compose.onNodeWithTag("stats_total").assertIsDisplayed()
        compose.onNodeWithText("155").assertIsDisplayed()
        compose.onNodeWithText("120").assertIsDisplayed()
        compose.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun `shows pausas card and weekly bars`() {
        val week = com.daycounter.domain.model.WeeklyActivity(
            days = (6 downTo 0).map {
                com.daycounter.domain.model.DayBar(java.time.LocalDate.of(2026, 5, 29).minusDays(it.toLong()), fulfilled = 2)
            },
            weekTotal = 14,
            todayIndex = 6,
        )
        compose.setContent {
            DayCounterTheme {
                StatsContent(
                    StatsUiState(
                        isLoading = false,
                        isEmpty = false,
                        totalAccumulated = 155,
                        bestStreak = 120,
                        activeCounters = 2,
                        milestonesReached = 8,
                        averageStreak = 52,
                        pauseStats = com.daycounter.domain.model.PauseStats(pausedNow = 1, totalPausedDays = 12, totalPauses = 3),
                        weekly = week,
                    ),
                )
            }
        }
        compose.onNodeWithTag("stats_pauses").assertExists()
        compose.onNodeWithTag("stats_week").assertExists()
        compose.onNodeWithTag("stats_milestones").assertExists()
        compose.onNodeWithTag("stats_avg").assertExists()
    }

    @Test
    fun `shows empty state when no counters`() {
        compose.setContent {
            DayCounterTheme {
                StatsContent(StatsUiState(isLoading = false, isEmpty = true))
            }
        }
        compose.onNodeWithTag("stats_empty").assertIsDisplayed()
    }
}
