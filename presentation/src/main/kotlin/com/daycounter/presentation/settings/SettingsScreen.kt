package com.daycounter.presentation.settings

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import com.daycounter.domain.model.AppLanguage
import com.daycounter.domain.model.AppearanceMode
import com.daycounter.presentation.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val enabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val appearance by viewModel.appearance.collectAsStateWithLifecycle()
    val counterCount by viewModel.counterCount.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Recreate the activity once a language change is persisted so the new locale takes effect (US3).
    LaunchedEffect(Unit) {
        viewModel.languageChanged.collect { (context as? Activity)?.recreate() }
    }

    var languageSheetOpen by remember { mutableStateOf(false) }
    var eraseSheetOpen by remember { mutableStateOf(false) }

    val snackbarHost = remember { SnackbarHostState() }
    val erasedMessage = stringResource(R.string.settings_erased_toast)
    val undoLabel = stringResource(R.string.settings_undo)
    // Show the undo snackbar once an erase completes (FR-031).
    LaunchedEffect(Unit) {
        viewModel.eraseUndoAvailable.collect {
            val result = snackbarHost.showSnackbar(
                message = erasedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoErase()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) })
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_notifications),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.settings_notifications_description),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .semantics { contentDescription = "Toggle milestone notifications" }
                        .testTag("settings_notifications_switch"),
                )
            }

            // Appearance section (US7).
            Text(
                text = stringResource(R.string.settings_appearance_header),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
            Text(
                text = stringResource(R.string.settings_dark_mode),
                style = MaterialTheme.typography.titleLarge,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("settings_appearance"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppearanceMode.entries.forEach { mode ->
                    FilterChip(
                        selected = mode == appearance,
                        onClick = { viewModel.setAppearance(mode) },
                        label = { Text(stringResource(mode.labelRes())) },
                        modifier = Modifier.testTag("appearance_${mode.name.lowercase()}"),
                    )
                }
            }

            // Language row (US3).
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 48.dp)
                    .clickable { languageSheetOpen = true }
                    .padding(vertical = 8.dp)
                    .testTag("settings_language_row"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_language),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.settings_language_description),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Text(
                    text = stringResource(language.nativeNameRes()),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Data section (US6).
            Text(
                text = stringResource(R.string.settings_data_header),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 48.dp)
                    .clickable { eraseSheetOpen = true }
                    .padding(vertical = 8.dp)
                    .testTag("settings_erase_row"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_erase_all),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = stringResource(R.string.settings_erase_all_description),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        if (languageSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { languageSheetOpen = false },
                sheetState = rememberModalBottomSheetState(),
                modifier = Modifier.testTag("language_sheet"),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.settings_language_sheet_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    AppLanguage.entries.forEach { lang ->
                        LanguageRow(
                            language = lang,
                            selected = lang == language,
                            onClick = {
                                viewModel.setLanguage(lang)
                                languageSheetOpen = false
                            },
                        )
                    }
                }
            }
        }

        if (eraseSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { eraseSheetOpen = false },
                sheetState = rememberModalBottomSheetState(),
                modifier = Modifier.testTag("erase_sheet"),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_erase_confirm_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.settings_erase_confirm_message, counterCount),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { eraseSheetOpen = false },
                            modifier = Modifier.weight(1f),
                        ) { Text(stringResource(R.string.settings_erase_cancel)) }
                        Button(
                            onClick = {
                                eraseSheetOpen = false
                                viewModel.eraseAll()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                            modifier = Modifier.weight(1f).testTag("erase_confirm"),
                        ) { Text(stringResource(R.string.settings_erase_confirm_cta)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageRow(language: AppLanguage, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 48.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .testTag("language_option_${language.tag}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(language.nativeNameRes()), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(language.labelRes()), style = MaterialTheme.typography.bodyMedium)
        }
        if (selected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun AppLanguage.nativeNameRes(): Int = when (this) {
    AppLanguage.ENGLISH -> R.string.language_name_en
    AppLanguage.SPANISH -> R.string.language_name_es
}

private fun AppLanguage.labelRes(): Int = when (this) {
    AppLanguage.ENGLISH -> R.string.language_label_en
    AppLanguage.SPANISH -> R.string.language_label_es
}

private fun AppearanceMode.labelRes(): Int = when (this) {
    AppearanceMode.SYSTEM -> R.string.settings_appearance_system
    AppearanceMode.LIGHT -> R.string.settings_appearance_light
    AppearanceMode.DARK -> R.string.settings_appearance_dark
}
