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
    fun `shows empty state when no counters`() {
        compose.setContent {
            DayCounterTheme {
                StatsContent(StatsUiState(isLoading = false, isEmpty = true))
            }
        }
        compose.onNodeWithTag("stats_empty").assertIsDisplayed()
    }
}
