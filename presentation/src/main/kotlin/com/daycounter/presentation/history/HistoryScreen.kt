package com.daycounter.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daycounter.domain.model.PastStreakRecord
import com.daycounter.presentation.R
import com.daycounter.presentation.components.CalendarCellState
import com.daycounter.presentation.components.CalendarDay
import com.daycounter.presentation.components.MonthCalendarGrid
import com.daycounter.presentation.components.Sparkline
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HistoryScreen(
    counterId: Long,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel<HistoryViewModel, HistoryViewModel.Factory>(
        creationCallback = { factory -> factory.create(counterId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }
    HistoryContent(state = state, onBack = onBack, onLoadMore = viewModel::loadNextPastStreaksPage)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryContent(
    state: HistoryUiState,
    onBack: () -> Unit,
    onLoadMore: () -> Unit,
) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val calendarDays = state.calendarCells.map { cell ->
        CalendarDay(dayOfMonth = cell.dayOfMonth, state = cell.state, contentDescription = cellDescription(cell.state, cell.dayOfMonth))
    }
    val rows = ((state.leadingBlanks + state.calendarCells.size) + 6) / 7

    Scaffold(
        modifier = Modifier.testTag("history_screen"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).testTag("history_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("history_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "header") {
                Column {
                    Text(text = state.counterName, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = stringResource(R.string.history_current_streak, state.currentStreak),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (state.sparklinePoints.isNotEmpty()) {
                        Sparkline(
                            values = state.sparklinePoints,
                            contentDescription = stringResource(R.string.history_sparkline_content_description, state.currentStreak),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                    }
                }
            }
            item(key = "calendar") {
                Column {
                    Text(text = stringResource(R.string.history_calendar_title), style = MaterialTheme.typography.titleMedium)
                    MonthCalendarGrid(
                        days = calendarDays,
                        leadingBlanks = state.leadingBlanks,
                        modifier = Modifier.height((rows * 48).dp),
                    )
                }
            }
            item(key = "past_title") {
                Text(
                    text = stringResource(R.string.history_past_streaks_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            items(state.pastStreaks, key = { "${it.endDate}_${it.streakDays}_${it.hashCode()}" }) { row ->
                Text(
                    text = stringResource(
                        R.string.history_past_streak_row,
                        row.streakDays,
                        reasonLabel(row.reason),
                        row.endDate.format(dateFormatter),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().testTag("past_streak_row"),
                )
            }
            if (state.canLoadMore) {
                item(key = "load_more") {
                    OutlinedButton(onClick = onLoadMore, modifier = Modifier.testTag("history_load_more")) {
                        Text(stringResource(R.string.history_load_more))
                    }
                }
            }
        }
    }
}

@Composable
private fun cellDescription(state: CalendarCellState, day: Int): String = when (state) {
    CalendarCellState.InStreak -> stringResource(R.string.history_calendar_cell_in_streak, day)
    CalendarCellState.Today -> stringResource(R.string.history_calendar_cell_today, day)
    CalendarCellState.PreStreak -> stringResource(R.string.history_calendar_cell_pre_streak, day)
    CalendarCellState.Future -> stringResource(R.string.history_calendar_cell_future, day)
}

@Composable
private fun reasonLabel(reason: String): String =
    if (reason == PastStreakRecord.REASON_RESET) stringResource(R.string.history_reason_reset) else reason
