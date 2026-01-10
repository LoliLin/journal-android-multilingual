/*
 * Copyright (c) 2022-2023. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * PsychonautWiki Journal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PsychonautWiki Journal.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.ui.tabs.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ContactSupport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.localization.I18n
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.VERSION_NAME
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.CardWithTitle
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import com.isaakhanimann.journal.ui.utils.getStringOfPattern
import kotlinx.coroutines.launch
import java.time.Instant

@Preview
@Composable
fun SettingsPreview() {
    SettingsScreen(
        deleteEverything = {},
        navigateToFAQ = {},
        navigateToComboSettings = {},
        navigateToSubstanceColors = {},
        navigateToCustomUnits = {},
        navigateToDonate = {},
        importFile = {},
        exportFile = {},
        snackbarHostState = remember { SnackbarHostState() },
        areDosageDotsHidden = false,
        saveDosageDotsAreHidden = {},
        supportedLanguages = mapOf("en_us" to "English (US)", "zh_cn" to "中文（中国）"),
        selectedLanguageKey = null,
        saveSelectedLanguage = {}
    )
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navigateToFAQ: () -> Unit,
    navigateToComboSettings: () -> Unit,
    navigateToSubstanceColors: () -> Unit,
    navigateToCustomUnits: () -> Unit,
    navigateToDonate: () -> Unit,
) {
    val context = LocalContext.current
    val selectedLanguageKey = viewModel.selectedLanguageFlow.collectAsState().value
    val supportedLanguages = remember(context) { I18n.getSupportedLanguages(context) }
    SettingsScreen(
        navigateToFAQ = navigateToFAQ,
        navigateToComboSettings = navigateToComboSettings,
        navigateToSubstanceColors = navigateToSubstanceColors,
        navigateToCustomUnits = navigateToCustomUnits,
        navigateToDonate = navigateToDonate,
        deleteEverything = viewModel::deleteEverything,
        importFile = viewModel::importFile,
        exportFile = viewModel::exportFile,
        snackbarHostState = viewModel.snackbarHostState,
        areDosageDotsHidden = viewModel.areDosageDotsHiddenFlow.collectAsState().value,
        saveDosageDotsAreHidden = viewModel::saveDosageDotsAreHidden,
        supportedLanguages = supportedLanguages,
        selectedLanguageKey = selectedLanguageKey,
        saveSelectedLanguage = viewModel::saveSelectedLanguage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateToFAQ: () -> Unit,
    navigateToComboSettings: () -> Unit,
    navigateToSubstanceColors: () -> Unit,
    navigateToCustomUnits: () -> Unit,
    navigateToDonate: () -> Unit,
    deleteEverything: () -> Unit,
    importFile: (uri: Uri) -> Unit,
    exportFile: (uri: Uri) -> Unit,
    snackbarHostState: SnackbarHostState,
    areDosageDotsHidden: Boolean,
    saveDosageDotsAreHidden: (Boolean) -> Unit,
    supportedLanguages: Map<String, String>,
    selectedLanguageKey: String?,
    saveSelectedLanguage: (String?) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(i18n("settings")) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = horizontalPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            CardWithTitle(title = i18n("settings_ui"), innerPaddingHorizontal = 0.dp) {
                SettingsButton(
                    imageVector = Icons.Outlined.Medication,
                    text = i18n("settings_custom_units")
                ) {
                    navigateToCustomUnits()
                }
                HorizontalDivider()
                SettingsButton(
                    imageVector = Icons.Outlined.Palette,
                    text = i18n("settings_substance_colors")
                ) {
                    navigateToSubstanceColors()
                }
                HorizontalDivider()
                SettingsButton(
                    imageVector = Icons.Outlined.WarningAmber,
                    text = i18n("settings_interaction_settings")
                ) {
                    navigateToComboSettings()
                }
                HorizontalDivider()
                var isLanguageDialogVisible by remember { mutableStateOf(false) }
                val languageName =
                    supportedLanguages[selectedLanguageKey] ?: i18n("settings_language_system")
                SettingsButton(
                    imageVector = Icons.Outlined.Language,
                    text = i18n(
                        "settings_language_with_value",
                        mapOf("language" to languageName)
                    )
                ) {
                    isLanguageDialogVisible = true
                }
                if (isLanguageDialogVisible) {
                    LanguageSelectionDialog(
                        supportedLanguages = supportedLanguages,
                        selectedLanguageKey = selectedLanguageKey,
                        onSelectLanguage = {
                            saveSelectedLanguage(it)
                            I18n.setPreferredLanguageKey(it)
                            isLanguageDialogVisible = false
                        },
                        onDismiss = { isLanguageDialogVisible = false }
                    )
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = i18n("settings_hide_dosage_dots"))
                    Switch(
                        checked = areDosageDotsHidden,
                        onCheckedChange = saveDosageDotsAreHidden)
                }
            }
            CardWithTitle(title = i18n("settings_app_data"), innerPaddingHorizontal = 0.dp) {
                var isShowingExportDialog by remember { mutableStateOf(false) }
                SettingsButton(
                    imageVector = Icons.Outlined.FileUpload,
                    text = i18n("settings_export_file")
                ) {
                    isShowingExportDialog = true
                }
                val jsonMIMEType = "application/json"
                val launcherExport =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.CreateDocument(
                            mimeType = jsonMIMEType
                        )
                    ) { uri ->
                        if (uri != null) {
                            exportFile(uri)
                        }
                    }
                AnimatedVisibility(visible = isShowingExportDialog) {
                    AlertDialog(
                        onDismissRequest = { isShowingExportDialog = false },
                        title = {
                            Text(text = i18n("settings_export_title"))
                        },
                        text = {
                            Text(i18n("settings_export_description"))
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    isShowingExportDialog = false
                                    launcherExport.launch("Journal ${Instant.now().getStringOfPattern("dd MMM yyyy")}.json")
                                }
                            ) {
                                Text(i18n("common_export"))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { isShowingExportDialog = false }
                            ) {
                                Text(i18n("common_cancel"))
                            }
                        }
                    )
                }
                HorizontalDivider()
                var isShowingImportDialog by remember { mutableStateOf(false) }
                SettingsButton(
                    imageVector = Icons.Outlined.FileDownload,
                    text = i18n("settings_import_file")
                ) {
                    isShowingImportDialog = true
                }
                val launcherImport =
                    rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
                        if (uri != null) {
                            importFile(uri)
                        }
                    }
                AnimatedVisibility(visible = isShowingImportDialog) {
                    AlertDialog(
                        onDismissRequest = { isShowingImportDialog = false },
                        title = {
                            Text(text = i18n("settings_import_title"))
                        },
                        text = {
                            Text(i18n("settings_import_description"))
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    isShowingImportDialog = false
                                    launcherImport.launch(jsonMIMEType)
                                }
                            ) {
                                Text(i18n("common_import"))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { isShowingImportDialog = false }
                            ) {
                                Text(i18n("common_cancel"))
                            }
                        }
                    )
                }
                HorizontalDivider()
                var isShowingDeleteDialog by remember { mutableStateOf(false) }
                SettingsButton(
                    imageVector = Icons.Outlined.DeleteForever,
                    text = i18n("settings_delete_everything")
                ) {
                    isShowingDeleteDialog = true
                }
                val scope = rememberCoroutineScope()
                val deletedSnackbarMessage = i18n("settings_deleted_snackbar")
                AnimatedVisibility(visible = isShowingDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { isShowingDeleteDialog = false },
                        title = {
                            Text(text = i18n("settings_delete_title"))
                        },
                        text = {
                            Text(i18n("settings_delete_description"))
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    isShowingDeleteDialog = false
                                    deleteEverything()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = deletedSnackbarMessage,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            ) {
                                Text(i18n("common_delete"))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { isShowingDeleteDialog = false }
                            ) {
                                Text(i18n("common_cancel"))
                            }
                        }
                    )
                }
            }
            val uriHandler = LocalUriHandler.current
            CardWithTitle(title = i18n("settings_feedback"), innerPaddingHorizontal = 0.dp) {
                SettingsButton(imageVector = Icons.Outlined.QuestionAnswer, text = i18n("settings_faq")) {
                    navigateToFAQ()
                }
                HorizontalDivider()
                SettingsButton(
                    imageVector = Icons.AutoMirrored.Outlined.ContactSupport,
                    text = i18n("settings_feedback_button")
                ) {
                    uriHandler.openUri("https://t.me/+ss8uZhBF6g00MTY8")
                }
                HorizontalDivider()
                SettingsButton(imageVector = Icons.Outlined.VolunteerActivism, text = i18n("settings_donate")) {
                    navigateToDonate()
                }
            }
            CardWithTitle(title = i18n("settings_app"), innerPaddingHorizontal = 0.dp) {
                SettingsButton(imageVector = Icons.Outlined.Code, text = i18n("settings_source_code")) {
                    uriHandler.openUri("https://github.com/isaakhanimann/psychonautwiki-journal-android")
                }
                HorizontalDivider()
                val context = LocalContext.current
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, SHARE_APP_URL)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                SettingsButton(imageVector = Icons.Outlined.Share, text = i18n("settings_share")) {
                    context.startActivity(shareIntent)
                }
                HorizontalDivider()
                Text(
                    text = i18n("settings_version_with_value", mapOf("version" to VERSION_NAME)),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                        .padding(vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun LanguageSelectionDialog(
    supportedLanguages: Map<String, String>,
    selectedLanguageKey: String?,
    onSelectLanguage: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sortedLanguages = supportedLanguages.entries.sortedBy { it.value }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(i18n("settings_language_title")) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LanguageOptionRow(
                    label = i18n("settings_language_system"),
                    isSelected = selectedLanguageKey == null,
                    onClick = { onSelectLanguage(null) }
                )
                sortedLanguages.forEach { (key, label) ->
                    LanguageOptionRow(
                        label = label,
                        isSelected = selectedLanguageKey == key,
                        onClick = { onSelectLanguage(key) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(i18n("common_close"))
            }
        }
    )
}

@Composable
private fun LanguageOptionRow(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .padding(horizontal = horizontalPadding)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(label)
    }
}

const val SHARE_APP_URL = "https://psychonautwiki.org/wiki/PsychonautWiki_Journal"


@Composable
fun SettingsButton(imageVector: ImageVector, text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Icon(
            imageVector,
            contentDescription = imageVector.name,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text)
        Spacer(modifier = Modifier.weight(1f))
    }
}
