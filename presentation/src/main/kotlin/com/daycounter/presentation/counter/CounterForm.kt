package com.daycounter.presentation.counter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daycounter.domain.model.Counter
import com.daycounter.presentation.R

/**
 * Stateless Create/Edit form shared by both bottom sheets (FR-015). On Edit the start date is
 * read-only with a "use Reiniciar" hint (FR-016); on Create it opens a date picker.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun CounterFormContent(
    title: String,
    name: String,
    category: String,
    startDateText: String,
    goalTarget: Int,
    canSave: Boolean,
    isSaving: Boolean,
    nameError: CreateCounterUiState.NameError?,
    categoryError: CreateCounterUiState.CategoryError?,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onGoalTargetChange: (Int) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    onStartDateClick: (() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.counter_goal_name_label)) },
            singleLine = true,
            isError = nameError != null,
            supportingText = when (nameError) {
                CreateCounterUiState.NameError.Blank -> { { Text(stringResource(R.string.counter_error_name_blank)) } }
                CreateCounterUiState.NameError.TooLong -> { { Text(stringResource(R.string.counter_error_name_long)) } }
                null -> null
            },
            modifier = Modifier.fillMaxWidth().testTag("form_name"),
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = category,
            onValueChange = onCategoryChange,
            label = { Text(stringResource(R.string.counter_category_label)) },
            singleLine = true,
            isError = categoryError != null,
            supportingText = if (categoryError != null) {
                { Text(stringResource(R.string.counter_error_category_long)) }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth().testTag("form_category"),
        )
        Spacer(Modifier.height(12.dp))

        Text(text = stringResource(R.string.counter_start_date_label), style = MaterialTheme.typography.labelLarge)
        if (onStartDateClick != null) {
            OutlinedButton(
                onClick = onStartDateClick,
                modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp).testTag("form_date"),
            ) {
                Text(startDateText)
            }
        } else {
            Text(text = startDateText, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag("form_date"))
            Text(
                text = stringResource(R.string.counter_edit_date_readonly_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("form_date_hint"),
            )
        }
        Spacer(Modifier.height(12.dp))

        Text(text = stringResource(R.string.counter_goal_target_label), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Counter.GOAL_TARGETS.sorted().forEach { target ->
                FilterChip(
                    selected = target == goalTarget,
                    onClick = { onGoalTargetChange(target) },
                    label = { Text(stringResource(R.string.counter_goal_target_chip, target)) },
                    modifier = Modifier.testTag("form_goal_chip_$target"),
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).sizeIn(minHeight = 48.dp).testTag("form_cancel"),
            ) {
                Text(stringResource(R.string.counter_cancel))
            }
            Button(
                onClick = onSave,
                enabled = canSave && !isSaving,
                modifier = Modifier.weight(1f).sizeIn(minHeight = 48.dp).testTag("form_save"),
            ) {
                Text(stringResource(R.string.counter_save))
            }
        }
    }
}
