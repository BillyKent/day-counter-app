package com.daycounter.presentation.counter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daycounter.presentation.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCounterScreen(
    counterId: Long,
    onDone: () -> Unit,
    viewModel: EditCounterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val storageErrorMsg = stringResource(R.string.counter_error_storage)

    LaunchedEffect(counterId) {
        viewModel.load(counterId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                EditCounterViewModel.UiEvent.Done -> onDone()
                EditCounterViewModel.UiEvent.ShowStorageError ->
                    scope.launch { snackbar.showSnackbar(storageErrorMsg) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.counter_edit_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onDone,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::requestReset,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.counter_reset),
                        )
                    }
                    IconButton(
                        onClick = viewModel::requestDelete,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.counter_delete),
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
            onCancel = onDone,
        )
    }

    if (state.showResetDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = { Text(stringResource(R.string.counter_reset_title)) },
            text = { Text(stringResource(R.string.counter_reset_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmReset() }) {
                    Text(stringResource(R.string.counter_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDialogs() }) {
                    Text(stringResource(R.string.counter_cancel))
                }
            },
        )
    }
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = { Text(stringResource(R.string.counter_delete_title)) },
            text = { Text(stringResource(R.string.counter_delete_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text(stringResource(R.string.counter_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDialogs() }) {
                    Text(stringResource(R.string.counter_cancel))
                }
            },
        )
    }
}
