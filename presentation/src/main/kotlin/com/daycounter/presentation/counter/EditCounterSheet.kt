package com.daycounter.presentation.counter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daycounter.presentation.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EditCounterSheet(
    counterId: Long,
    onDismiss: () -> Unit,
    viewModel: EditCounterViewModel = hiltViewModel<EditCounterViewModel, EditCounterViewModel.Factory>(
        creationCallback = { factory -> factory.create(counterId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            if (event is EditCounterViewModel.UiEvent.Done) onDismiss()
        }
    }

    CounterFormContent(
        title = stringResource(R.string.counter_edit_title),
        name = state.goalName,
        category = state.category,
        startDateText = (state.startDate ?: LocalDate.now()).format(formatter),
        goalTarget = state.goalMilestoneTarget,
        canSave = state.canSave,
        isSaving = state.isSaving,
        nameError = state.nameError,
        categoryError = state.categoryError,
        onNameChange = viewModel::onGoalNameChange,
        onCategoryChange = viewModel::onCategoryChange,
        onGoalTargetChange = viewModel::onGoalTargetChange,
        onSave = viewModel::onSave,
        onCancel = onDismiss,
        // No onStartDateClick → the date renders read-only with the "use Reiniciar" hint (FR-016).
        onStartDateClick = null,
    )
}
