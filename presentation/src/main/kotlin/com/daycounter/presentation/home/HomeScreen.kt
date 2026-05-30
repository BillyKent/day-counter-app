package com.daycounter.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daycounter.presentation.R
import com.daycounter.presentation.components.ProgressRing
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HomeScreen(
    onCardTap: (Long) -> Unit,
    onAddTap: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }
    HomeContent(
        state = state,
        onCardTap = onCardTap,
        onAddTap = onAddTap,
        onSetFilter = viewModel::setFilter,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeContent(
    state: HomeUiState,
    onCardTap: (Long) -> Unit,
    onAddTap: () -> Unit,
    onSetFilter: (CounterFilter) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.home_title)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTap,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.home_add_counter)) },
                modifier = Modifier.testTag("home_add_counter"),
            )
        },
    ) { padding ->
        // Only a brand-new install (no counters at all) shows the full empty state without chips.
        if (state.emptyKind == EmptyKind.NO_COUNTERS) {
            EmptyState(padding, onAddTap)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 96.dp,
                    start = 16.dp,
                    end = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().testTag("home_list"),
            ) {
                state.summary?.let { summary ->
                    item(key = "summary") { SummaryCard(summary) }
                }
                item(key = "filters") {
                    FilterChips(filter = state.filter, counts = state.counts, onSetFilter = onSetFilter)
                }
                if (state.counters.isEmpty()) {
                    item(key = "filter_empty") { FilterEmptyMessage(state.emptyKind) }
                } else {
                    items(state.counters, key = { it.id }) { card ->
                        CounterCard(card = card, onClick = { onCardTap(card.id) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChips(
    filter: CounterFilter,
    counts: FilterCounts,
    onSetFilter: (CounterFilter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().testTag("home_filters"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = filter == CounterFilter.ALL,
            onClick = { onSetFilter(CounterFilter.ALL) },
            label = { Text("${stringResource(R.string.home_filter_all)} ${counts.all}") },
            modifier = Modifier.testTag("filter_all"),
        )
        FilterChip(
            selected = filter == CounterFilter.ACTIVE,
            onClick = { onSetFilter(CounterFilter.ACTIVE) },
            label = { Text("${stringResource(R.string.home_filter_active)} ${counts.active}") },
            modifier = Modifier.testTag("filter_active"),
        )
        FilterChip(
            selected = filter == CounterFilter.PAUSED,
            onClick = { onSetFilter(CounterFilter.PAUSED) },
            label = { Text("${stringResource(R.string.home_filter_paused)} ${counts.paused}") },
            modifier = Modifier.testTag("filter_paused"),
        )
    }
}

@Composable
private fun FilterEmptyMessage(emptyKind: EmptyKind) {
    val text = when (emptyKind) {
        EmptyKind.NO_PAUSED -> stringResource(R.string.home_empty_paused)
        EmptyKind.NO_ACTIVE -> stringResource(R.string.home_empty_active)
        else -> ""
    }
    Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("home_filter_empty"),
        )
    }
}

@Composable
private fun SummaryCard(summary: SummaryUi) {
    Card(modifier = Modifier.fillMaxWidth().testTag("home_summary")) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SummaryMetric(label = stringResource(R.string.home_summary_total), value = summary.totalDays)
            SummaryMetric(label = stringResource(R.string.home_summary_best_streak), value = summary.bestStreak)
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), style = MaterialTheme.typography.headlineMedium)
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun CounterCard(card: CounterCardUi, onClick: () -> Unit) {
    val description = stringResource(R.string.home_card_content_description, card.name, card.streakDays)
    val ringDescription = stringResource(
        R.string.home_ring_content_description,
        card.streakDays,
        card.goalMilestoneTarget,
    )
    val dateFormatter = rememberMediumDate()

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 88.dp)
            .semantics { contentDescription = description }
            .testTag("counter_card_${card.id}"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProgressRing(
                days = card.streakDays,
                target = card.goalMilestoneTarget,
                contentDescription = ringDescription,
                diameter = 72.dp,
                strokeWidth = 8.dp,
                paused = card.isPaused,
            ) {
                Text(card.streakDays.toString(), style = MaterialTheme.typography.titleLarge)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = card.name, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Text(
                    text = if (card.isPaused) {
                        stringResource(R.string.home_card_paused)
                    } else {
                        stringResource(R.string.home_card_start_date, card.startDate.format(dateFormatter))
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
                if (card.goalReached) {
                    Spacer(Modifier.height(8.dp))
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(stringResource(R.string.home_badge_goal_reached)) },
                        colors = AssistChipDefaults.assistChipColors(),
                        modifier = Modifier.testTag("badge_goal_reached_${card.id}"),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(padding: PaddingValues, onAddTap: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.home_empty_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_empty_body),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onAddTap, modifier = Modifier.testTag("home_empty_cta")) {
                Text(stringResource(R.string.home_empty_cta))
            }
        }
    }
}

@Composable
private fun rememberMediumDate(): DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
