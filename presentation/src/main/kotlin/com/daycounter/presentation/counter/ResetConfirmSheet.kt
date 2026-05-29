package com.daycounter.presentation.counter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.daycounter.presentation.R
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ResetConfirmSheet(
    counterId: Long,
    onDismiss: () -> Unit,
    viewModel: ResetConfirmViewModel = hiltViewModel<ResetConfirmViewModel, ResetConfirmViewModel.Factory>(
        creationCallback = { factory -> factory.create(counterId) },
    ),
) {
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            if (event is ResetConfirmViewModel.UiEvent.Done) onDismiss()
        }
    }
    ResetConfirmContent(onConfirm = viewModel::confirm, onCancel = onDismiss)
}

@Composable
internal fun ResetConfirmContent(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).testTag("reset_confirm_sheet")) {
        Text(text = stringResource(R.string.reset_confirm_title), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text(text = stringResource(R.string.reset_confirm_message), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).sizeIn(minHeight = 48.dp).testTag("reset_cancel"),
            ) {
                Text(stringResource(R.string.reset_confirm_cancel))
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f).sizeIn(minHeight = 48.dp).testTag("reset_confirm"),
            ) {
                Text(stringResource(R.string.reset_confirm_confirm))
            }
        }
    }
}
