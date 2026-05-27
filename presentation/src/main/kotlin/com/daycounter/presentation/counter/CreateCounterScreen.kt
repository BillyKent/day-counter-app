package com.daycounter.presentation.counter

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daycounter.presentation.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCounterScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: CreateCounterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val storageErrorMsg = stringResource(R.string.counter_error_storage)

    val alreadyAsked by viewModel.notificationPermissionAlreadyRequested.collectAsState(initial = true)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { viewModel.onPermissionRequestComplete() }

    LaunchedEffect(alreadyAsked) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !alreadyAsked) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                CreateCounterViewModel.UiEvent.NavigateBack -> onSaved()
                CreateCounterViewModel.UiEvent.ShowStorageError ->
                    scope.launch { snackbar.showSnackbar(storageErrorMsg) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.counter_create_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        CounterFormBody(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            goalName = state.goalName,
            startDate = state.startDate,
            nameError = state.nameError,
            dateError = state.dateError,
            canSave = state.canSave,
            isSaving = state.isSaving,
            saveLabel = stringResource(R.string.counter_save),
            onGoalNameChange = viewModel::onGoalNameChange,
            onStartDateChange = viewModel::onStartDateChange,
            onSave = viewModel::onSave,
            onCancel = onCancel,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CounterFormBody(
    modifier: Modifier = Modifier,
    goalName: String,
    startDate: LocalDate?,
    nameError: CreateCounterUiState.NameError?,
    dateError: CreateCounterUiState.DateError?,
    canSave: Boolean,
    isSaving: Boolean,
    saveLabel: String,
    onGoalNameChange: (String) -> Unit,
    onStartDateChange: (LocalDate?) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val displayDate = startDate ?: today
    val isoFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = goalName,
            onValueChange = onGoalNameChange,
            label = { Text(stringResource(R.string.counter_goal_name_label)) },
            singleLine = true,
            isError = nameError != null,
            supportingText = when (nameError) {
                CreateCounterUiState.NameError.Blank -> {
                    { Text(stringResource(R.string.counter_error_name_blank)) }
                }
                CreateCounterUiState.NameError.TooLong -> {
                    { Text(stringResource(R.string.counter_error_name_long)) }
                }
                null -> null
            },
            modifier = Modifier.fillMaxWidth().testTag("counter_goal_name"),
        )
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
        ) {
            Text(
                stringResource(R.string.counter_start_date_label) +
                    ": " + displayDate.format(isoFormatter),
            )
        }
        if (dateError == CreateCounterUiState.DateError.Future) {
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.counter_error_future),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).sizeIn(minHeight = 48.dp),
            ) {
                Text(stringResource(R.string.counter_cancel))
            }
            Button(
                onClick = onSave,
                enabled = canSave && !isSaving,
                modifier = Modifier.weight(1f).sizeIn(minHeight = 48.dp).testTag("counter_save"),
            ) {
                Text(saveLabel)
            }
        }
    }

    if (showDatePicker) {
        val initialMillis = (startDate ?: today).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val todayMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= todayMillis
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onStartDateChange(picked)
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.counter_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.counter_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
