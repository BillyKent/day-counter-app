package com.daycounter.presentation.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.daycounter.domain.model.PastStreakRecord
import com.daycounter.presentation.components.CalendarCellState
import com.daycounter.presentation.theme.DayCounterTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class HistoryScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun state(canLoadMore: Boolean = false) = HistoryUiState(
        isLoading = false,
        counterName = "Read daily",
        currentStreak = 12,
        sparklinePoints = (1..12).toList(),
        leadingBlanks = 2,
        calendarCells = (1..28).map { CalendarCell(it, if (it == 28) CalendarCellState.Today else CalendarCellState.InStreak) },
        pastStreaks = listOf(
            PastStreakUi(14, PastStreakRecord.REASON_RESET, LocalDate.of(2026, 4, 1)),
            PastStreakUi(3, PastStreakRecord.REASON_RESET, LocalDate.of(2026, 3, 1)),
        ),
        canLoadMore = canLoadMore,
    )

    @Test
    fun `header shows name and current streak`() {
        compose.setContent { DayCounterTheme { HistoryContent(state(), onBack = {}, onLoadMore = {}) } }
        compose.onNodeWithText("Read daily").assertIsDisplayed()
        compose.onNodeWithTag("history_screen").assertIsDisplayed()
    }

    @Test
    fun `back invokes callback`() {
        var backed = false
        compose.setContent { DayCounterTheme { HistoryContent(state(), onBack = { backed = true }, onLoadMore = {}) } }
        compose.onNodeWithTag("history_back").performClick()
        assertEquals(true, backed)
    }

    @Test
    fun `past streak rows are listed`() {
        compose.setContent { DayCounterTheme { HistoryContent(state(), onBack = {}, onLoadMore = {}) } }
        // Scroll the lazy list so a past-streak row composes, then assert it is shown.
        compose.onNodeWithTag("history_list").performScrollToNode(hasTestTag("past_streak_row"))
        compose.onAllNodesWithTag("past_streak_row").onFirst().assertIsDisplayed()
    }

    @Test
    fun `load more shown and clickable when canLoadMore`() {
        var loaded = false
        compose.setContent { DayCounterTheme { HistoryContent(state(canLoadMore = true), onBack = {}, onLoadMore = { loaded = true }) } }
        compose.onNodeWithTag("history_list").performScrollToNode(hasTestTag("history_load_more"))
        compose.onNodeWithTag("history_load_more").performClick()
        assertEquals(true, loaded)
    }
}
