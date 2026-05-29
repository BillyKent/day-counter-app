package com.daycounter.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** The four mutually-exclusive states a calendar day can be in (FR-028). */
enum class CalendarCellState { InStreak, Today, PreStreak, Future }

/**
 * One rendered calendar day. [contentDescription] is built by the caller (localized) so the grid
 * stays free of string resources.
 */
data class CalendarDay(
    val dayOfMonth: Int,
    val state: CalendarCellState,
    val contentDescription: String,
)

/**
 * A current-month calendar grid (no month navigation — out of scope). Renders [leadingBlanks]
 * empty cells for the weekday offset of day 1, then one cell per [days] entry. Cell state is
 * conveyed by shape + weight + a per-cell content description, never color alone (Principle I).
 * Cells are non-interactive.
 */
@Composable
fun MonthCalendarGrid(
    days: List<CalendarDay>,
    leadingBlanks: Int,
    modifier: Modifier = Modifier,
) {
    val cells: List<CalendarDay?> = List(leadingBlanks.coerceAtLeast(0)) { null } + days

    LazyVerticalGrid(
        columns = GridCells.Fixed(DAYS_PER_WEEK),
        modifier = modifier.fillMaxWidth(),
    ) {
        items(cells) { day ->
            if (day == null) {
                Box(Modifier.aspectRatio(1f))
            } else {
                DayCell(day)
            }
        }
    }
}

@Composable
private fun DayCell(day: CalendarDay) {
    val container: Color
    val content: Color
    when (day.state) {
        CalendarCellState.Today -> {
            container = MaterialTheme.colorScheme.primary
            content = MaterialTheme.colorScheme.onPrimary
        }
        CalendarCellState.InStreak -> {
            container = MaterialTheme.colorScheme.primaryContainer
            content = MaterialTheme.colorScheme.onPrimaryContainer
        }
        CalendarCellState.PreStreak -> {
            container = Color.Transparent
            content = MaterialTheme.colorScheme.onSurfaceVariant
        }
        CalendarCellState.Future -> {
            container = Color.Transparent
            content = MaterialTheme.colorScheme.onSurface
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clearAndSetSemantics { contentDescription = day.contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = container,
            contentColor = content,
            modifier = Modifier.padding(2.dp),
        ) {
            Box(
                modifier = Modifier.padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (day.state == CalendarCellState.Today) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

private const val DAYS_PER_WEEK = 7
