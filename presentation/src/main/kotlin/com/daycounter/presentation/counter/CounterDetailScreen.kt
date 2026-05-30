package com.daycounter.presentation.counter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.daycounter.presentation.components.ProgressRing

/** Navigation callbacks for the Detail screen (each maps to a TopLevelBackStack operation). */
data class CounterDetailActions(
    val onBack: () -> Unit,
    val onEdit: (Long) -> Unit,
    val onReset: (Long) -> Unit,
    val onHistory: (Long) -> Unit,
    val onCelebration: (counterId: Long, milestone: Int) -> Unit,
    val onExitToContadores: () -> Unit,
)

@Composable
fun CounterDetailScreen(
    counterId: Long,
    actions: CounterDetailActions,
    viewModel: CounterDetailViewModel = hiltViewModel<CounterDetailViewModel, CounterDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(counterId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CounterDetailViewModel.UiEvent.CounterDeleted -> actions.onExitToContadores()
                is CounterDetailViewModel.UiEvent.AutoLaunchCelebration ->
                    actions.onCelebration(counterId, event.milestone)
            }
        }
    }
    LaunchedEffect(state.missing) {
        if (state.missing) actions.onExitToContadores()
    }

    CounterDetailContent(
        state = state,
        counterId = counterId,
        actions = actions,
        onTogglePause = viewModel::togglePause,
        onRequestDelete = viewModel::requestDelete,
        onConfirmDelete = viewModel::confirmDelete,
        onDismissDelete = viewModel::dismissDelete,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun CounterDetailContent(
    state: CounterDetailUiState,
    counterId: Long,
    actions: CounterDetailActions,
    onTogglePause: () -> Unit,
    onRequestDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.testTag("counter_detail_screen"),
        topBar = {
            TopAppBar(
                title = { Text(state.name) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading || state.missing) return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProgressRing(
                days = state.streakDays,
                target = state.goalMilestoneTarget,
                contentDescription = stringResource(
                    R.string.home_ring_content_description,
                    state.streakDays,
                    state.goalMilestoneTarget,
                ),
                diameter = 160.dp,
                strokeWidth = 14.dp,
                paused = state.isPaused,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (state.isPaused) {
                        Text(
                            text = stringResource(R.string.counter_detail_paused_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = state.streakDays.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.testTag("detail_hero_streak"),
                    )
                }
            }

            if (!state.isPaused) {
                Text(
                    text = if (state.nextMilestone != null) {
                        stringResource(R.string.counter_detail_next_milestone, state.nextMilestone - state.streakDays)
                    } else {
                        stringResource(R.string.counter_detail_all_milestones)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("detail_next_milestone"),
                )
            }

            if (state.isPaused) {
                PausedBanner(pausedDays = state.pausedDays)
            }

            Button(
                onClick = onTogglePause,
                modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp).testTag("detail_toggle_pause"),
            ) {
                Text(
                    stringResource(
                        if (state.isPaused) R.string.counter_detail_resume else R.string.counter_detail_pause,
                    ),
                )
            }

            if (state.achievedMilestones.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.counter_detail_achieved_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.achievedMilestones.forEach { milestone ->
                        AchievedChip(milestone)
                    }
                }
            }

            DetailActions(state = state, counterId = counterId, actions = actions, onRequestDelete = onRequestDelete)
        }

        if (state.deleteConfirmVisible) {
            AlertDialog(
                onDismissRequest = onDismissDelete,
                title = { Text(stringResource(R.string.counter_delete_title)) },
                text = { Text(stringResource(R.string.counter_delete_message)) },
                confirmButton = {
                    TextButton(onClick = onConfirmDelete, modifier = Modifier.testTag("detail_delete_confirm")) {
                        Text(stringResource(R.string.counter_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDelete) { Text(stringResource(R.string.counter_cancel)) }
                },
            )
        }
    }
}

@Composable
private fun PausedBanner(pausedDays: Int) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().testTag("detail_paused_banner"),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.counter_detail_paused_banner_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.counter_detail_paused_banner_subtitle, pausedDays),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun AchievedChip(milestone: Int) {
    // Informational and intentionally non-interactive (FR-022): no click action.
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.testTag("achieved_chip_$milestone"),
    ) {
        Text(
            text = stringResource(R.string.counter_detail_milestone_chip, milestone),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun DetailActions(
    state: CounterDetailUiState,
    counterId: Long,
    actions: CounterDetailActions,
    onRequestDelete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionButton("detail_action_edit", stringResource(R.string.counter_detail_action_edit)) { actions.onEdit(counterId) }
        ActionButton("detail_action_reset", stringResource(R.string.counter_detail_action_reset)) { actions.onReset(counterId) }
        ActionButton("detail_action_history", stringResource(R.string.counter_detail_action_history)) { actions.onHistory(counterId) }
        if (state.canRevive && state.mostRecentMilestone != null) {
            ActionButton("detail_action_revive", stringResource(R.string.counter_detail_action_revive)) {
                actions.onCelebration(counterId, state.mostRecentMilestone)
            }
        }
        ActionButton("detail_action_delete", stringResource(R.string.counter_detail_action_delete), onRequestDelete)
    }
}

@Composable
private fun ActionButton(tag: String, label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp).testTag(tag),
    ) {
        Text(label)
    }
}
