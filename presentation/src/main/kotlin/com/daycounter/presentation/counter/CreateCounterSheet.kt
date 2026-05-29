package com.daycounter.presentation.counter

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daycounter.presentation.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCounterSheet(
    onDismiss: () -> Unit,
    viewModel: CreateCounterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    val alreadyAsked by viewModel.notificationPermissionAlreadyRequested.collectAsState(initial = true)
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.onPermissionRequestComplete()
    }
    LaunchedEffect(alreadyAsked) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !alreadyAsked) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            if (event is CreateCounterViewModel.UiEvent.NavigateBack) onDismiss()
        }
    }

    CounterFormContent(
        title = stringResource(R.string.counter_create_title),
        name = state.goalName,
        category = state.category,
        startDateText = (state.startDate ?: today).format(formatter),
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
        onStartDateClick = { showDatePicker = true },
    )

    if (showDatePicker) {
        val todayMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (state.startDate ?: today).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayMillis
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.onStartDateChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.counter_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.counter_cancel)) }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
