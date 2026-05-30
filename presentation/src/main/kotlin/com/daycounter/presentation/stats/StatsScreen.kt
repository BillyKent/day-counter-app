package com.daycounter.presentation.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daycounter.presentation.R
import com.daycounter.presentation.components.WeeklyBars

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }
    StatsContent(state)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StatsContent(state: StatsUiState) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.stats_title)) }) },
    ) { padding ->
        if (state.isEmpty) {
            EmptyStats(Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeroMetric(
                label = stringResource(R.string.stats_total_accumulated),
                value = state.totalAccumulated,
                tag = "stats_total",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                SecondaryMetric(
                    label = stringResource(R.string.stats_best_streak),
                    value = state.bestStreak,
                    tag = "stats_best",
                    modifier = Modifier.weight(1f),
                )
                SecondaryMetric(
                    label = stringResource(R.string.stats_milestones),
                    value = state.milestonesReached,
                    tag = "stats_milestones",
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                SecondaryMetric(
                    label = stringResource(R.string.stats_active_counters),
                    value = state.activeCounters,
                    tag = "stats_active",
                    modifier = Modifier.weight(1f),
                )
                SecondaryMetric(
                    label = stringResource(R.string.stats_avg_streak),
                    value = state.averageStreak,
                    tag = "stats_avg",
                    modifier = Modifier.weight(1f),
                )
            }

            // Pausas card (FR-027).
            Card(modifier = Modifier.fillMaxWidth().testTag("stats_pauses")) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.stats_pauses), style = MaterialTheme.typography.titleLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        PauseMetric(state.pauseStats.pausedNow, stringResource(R.string.stats_paused_now), Modifier.weight(1f))
                        PauseMetric(state.pauseStats.totalPausedDays, stringResource(R.string.stats_paused_days), Modifier.weight(1f))
                        PauseMetric(state.pauseStats.totalPauses, stringResource(R.string.stats_total_pauses), Modifier.weight(1f))
                    }
                    Text(
                        stringResource(R.string.stats_pauses_note),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Weekly activity (FR-028).
            Card(modifier = Modifier.fillMaxWidth().testTag("stats_week")) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.stats_this_week), style = MaterialTheme.typography.titleLarge)
                        Text(
                            stringResource(R.string.stats_week_completed, state.weekly.weekTotal),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    WeeklyBars(activity = state.weekly)
                }
            }
        }
    }
}

@Composable
private fun PauseMetric(value: Int, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(12.dp),
    ) {
        Text(value.toString(), style = MaterialTheme.typography.headlineMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HeroMetric(label: String, value: Int, tag: String) {
    Card(modifier = Modifier.fillMaxWidth().testTag(tag)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = value.toString(), style = MaterialTheme.typography.displayLarge)
            Text(text = label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun SecondaryMetric(label: String, value: Int, tag: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.testTag(tag)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = value.toString(), style = MaterialTheme.typography.headlineMedium)
            Text(text = label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun EmptyStats(modifier: Modifier) {
    Box(modifier = modifier.padding(32.dp).testTag("stats_empty"), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.stats_empty_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.stats_empty_body),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}
