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
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ContactSupport
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.isaakhanimann.journal.data.achievement.AchievementLogoButton
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.CardWithTitle
import com.isaakhanimann.journal.ui.main.bottomBarNestedScroll
import com.isaakhanimann.journal.ui.main.bottomBarOverlayDp
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import com.isaakhanimann.journal.ui.utils.rememberOpenLink
import com.isaakhanimann.journal.ui.utils.getMediumDateText
import java.time.Instant
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.height




@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navigateToFAQ: () -> Unit,
    navigateToComboSettings: () -> Unit,
    navigateToSubstanceColors: () -> Unit,
    navigateToCustomUnits: () -> Unit,
    navigateToDonate: () -> Unit,
    navigateToIconPicker: () -> Unit = {},
    navigateToExtensionPack: () -> Unit = {},
    navigateToPreferences: () -> Unit = {}
) {
    val ownerUserName = viewModel.ownerUserNameFlow.collectAsState(initial = "You").value ?: "You"
    SettingsScreen(
        navigateToFAQ = navigateToFAQ,
        navigateToComboSettings = navigateToComboSettings,
        navigateToSubstanceColors = navigateToSubstanceColors,
        navigateToCustomUnits = navigateToCustomUnits,
        navigateToDonate = navigateToDonate,
        navigateToIconPicker = navigateToIconPicker,
        navigateToExtensionPack = navigateToExtensionPack,
        navigateToPreferences = navigateToPreferences,
        deleteEverything = viewModel::deleteEverything,
        importFile = viewModel::importFile,
        isImportEncrypted = viewModel::isImportEncrypted,
        exportFile = viewModel::exportFile,
        snackbarHostState = viewModel.snackbarHostState,
        ownerUserName = ownerUserName,
        saveOwnerUserName = viewModel::saveOwnerUserName,
        achievements = viewModel.achievementsFlow.collectAsState().value,
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
    navigateToIconPicker: () -> Unit = {},
    navigateToExtensionPack: () -> Unit = {},
    navigateToPreferences: () -> Unit = {},
    deleteEverything: () -> Unit,
    importFile: (uri: Uri, password: String?) -> Unit,
    isImportEncrypted: (Uri) -> Boolean,
    exportFile: (uri: Uri, password: String?) -> Unit,
    snackbarHostState: SnackbarHostState,
    ownerUserName: String = "You",
    achievements: List<String> = emptyList(),
    saveOwnerUserName: (String?) -> Unit,
) {
    Scaffold(
        modifier = Modifier.bottomBarNestedScroll(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(i18n("settings")) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = horizontalPadding)
                .padding(bottom = bottomBarOverlayDp())
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            OwnerProfileCard(
                ownerUserName = ownerUserName,
                onUserNameChanged = saveOwnerUserName,
                achievements = achievements
            )

            CardWithTitle(title = i18n("settings_ui"), innerPaddingHorizontal = 0.dp) {
                SettingsButton(
                    imageVector = Icons.Outlined.Tune,
                    text = i18n("settings_preferences")
                ) {
                    navigateToPreferences()
                }
                HorizontalDivider()
                SettingsButton(
                    imageVector = Icons.Outlined.Medication,
                    text = i18n("settings_custom_units")
                ) {
                    navigateToCustomUnits()
                }
                HorizontalDivider()
                SettingsButton(
                    imageVector = Icons.Outlined.StarBorder,
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
                var exportEncrypt by remember { mutableStateOf(false) }
                var exportPassword by remember { mutableStateOf("") }
                val launcherExport =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.CreateDocument(
                            mimeType = jsonMIMEType
                        )
                    ) { uri ->
                        if (uri != null) {
                            exportFile(uri, if (exportEncrypt) exportPassword else null)
                        }
                    }
                AnimatedVisibility(visible = isShowingExportDialog) {
                    AlertDialog(
                        onDismissRequest = { isShowingExportDialog = false },
                        title = {
                            Text(text = i18n("settings_export_title"))
                        },
                        text = {
                            Column {
                                Text(i18n("settings_export_description"))
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = i18n("settings_export_encrypt"))
                                    Switch(
                                        checked = exportEncrypt,
                                        onCheckedChange = {
                                            exportEncrypt = it
                                            if (!it) exportPassword = ""
                                        }
                                    )
                                }
                                if (exportEncrypt) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = exportPassword,
                                        onValueChange = { exportPassword = it },
                                        label = { Text(i18n("settings_export_password")) },
                                        visualTransformation = PasswordVisualTransformation(),
                                        singleLine = true
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                enabled = !exportEncrypt || exportPassword.isNotBlank(),
                                onClick = {
                                    isShowingExportDialog = false
                                    launcherExport.launch(
                                        "Journal ${Instant.now().getMediumDateText()}.${if (exportEncrypt) "jenc" else "json"}"
                                    )
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
                var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
                var importPassword by remember { mutableStateOf("") }
                val scopeImport = rememberCoroutineScope()
                val launcherImport =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                        if (uri != null) {
                            scopeImport.launch {
                                if (isImportEncrypted(uri)) {
                                    importPassword = ""
                                    pendingImportUri = uri
                                } else {
                                    importFile(uri, null)
                                }
                            }
                        }
                    }
                AnimatedVisibility(visible = pendingImportUri != null) {
                    AlertDialog(
                        onDismissRequest = { pendingImportUri = null },
                        title = {
                            Text(text = i18n("settings_import_encrypted_title"))
                        },
                        text = {
                            Column {
                                Text(i18n("settings_import_encrypted_description"))
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = importPassword,
                                    onValueChange = { importPassword = it },
                                    label = { Text(i18n("settings_import_password")) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                enabled = importPassword.isNotBlank(),
                                onClick = {
                                    val uri = pendingImportUri
                                    pendingImportUri = null
                                    if (uri != null) {
                                        importFile(uri, importPassword)
                                    }
                                }
                            ) {
                                Text(i18n("common_import"))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { pendingImportUri = null }
                            ) {
                                Text(i18n("common_cancel"))
                            }
                        }
                    )
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
                                    launcherImport.launch("*/*") // .jenc has no registered MIME; the magic-byte sniffer decides
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
                var deleteConfirmText by remember { mutableStateOf("") }
                
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
                        onDismissRequest = {
                            isShowingDeleteDialog = false
                            deleteConfirmText = ""
                        },
                        title = {
                            Text(text = i18n("settings_delete_title"))
                        },
                        text = {
                            Column {
                                Text(i18n("settings_delete_description"))

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = deleteConfirmText,
                                    onValueChange = { deleteConfirmText = it },
                                    label = { Text(i18n("settings_delete_type_confirm")) },
                                    singleLine = true,
                                    isError = deleteConfirmText.isNotBlank() && deleteConfirmText.trim() != "Delete",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    isShowingDeleteDialog = false
                                    deleteConfirmText = ""
                                    deleteEverything()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = deletedSnackbarMessage,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                },
                                enabled = deleteConfirmText.trim() == "Delete",
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    disabledContainerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(i18n("common_confirm"))
                            }
                        }
                    )
                }
            }

            CardWithTitle(title = i18n("settings_extension_pack"), innerPaddingHorizontal = 0.dp) {
                SettingsButton(

                    imageVector = Icons.Outlined.Extension,

                    text = i18n("settings_extension_import")

                ) {
                    navigateToExtensionPack()
                }
            }

            val openLink = rememberOpenLink()

            CardWithTitle(title = i18n("settings_feedback"), innerPaddingHorizontal = 0.dp) {
                SettingsButton(
                    imageVector = Icons.Outlined.QuestionAnswer,
                    text = i18n("settings_faq")
                ) {
                    navigateToFAQ()
                }
                HorizontalDivider()
                SettingsButton(
                    imageVector = Icons.AutoMirrored.Outlined.ContactSupport,
                    text = i18n("settings_feedback_button")
                ) {
                    openLink(
                        "https://github.com/LoliLin/journal-android-multilingual/issues"
                    )
                }
                HorizontalDivider()
                SettingsButton(
                    imageVector = Icons.Outlined.VolunteerActivism,
                    text = i18n("settings_donate")
                ) {
                    navigateToDonate()
                }
            }
            CardWithTitle(title = i18n("settings_app"), innerPaddingHorizontal = 0.dp) {
                SettingsButton(
                    imageVector = Icons.Outlined.Code,
                    text = i18n("settings_source_code")
                ) {
                    openLink("https://github.com/LoliLin/journal-android-multilingual")
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
                    text = i18n(
                        "settings_version_with_value",
                        mapOf(
                            "version" to com.isaakhanimann.journal.ui.VERSION_NAME
                        )
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                        .padding(vertical = 10.dp)
                )
            }
        }
    }
}


const val SHARE_APP_URL = "https://github.com/LoliLin/journal-android-multilingual"

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

@Composable
fun OwnerProfileCard(
    ownerUserName: String = "You",
    achievements: List<String> = emptyList(),
    onUserNameChanged: (String) -> Unit, // 调用 ViewModel/DataStore 更新用户名
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var avatarRefresh by remember { mutableStateOf(0) }

    // 当前头像文件

    val avatarFile = remember(ownerUserName, avatarRefresh) {
        AvatarUtil.getUserAvatar(context, ownerUserName)
    }

    // 控制改名对话框
    var showEditDialog by remember { mutableStateOf(false) }
    var newName by remember(ownerUserName) { mutableStateOf(ownerUserName) }

    // 头像选择触发器
    val pickAvatar = AvatarUtil.acquireUserAvatar(

        context = context,

        userName = ownerUserName,

        onAvatarSaved = {
            avatarRefresh++
        }

    )

    // 改名对话框
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(i18n("change_name")) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    label = { Text(i18n("name")) }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = newName.trim()
                    if (trimmed.isNotEmpty() && trimmed != ownerUserName) {
                        // 更新存储的用户名
                        onUserNameChanged(trimmed)
                        // 同步头像文件：如果旧头像存在，重命名为新用户名
                        val oldFile = AvatarUtil.getUserAvatar(context, ownerUserName)
                        if (oldFile?.exists() == true) {
                            val newFile = AvatarUtil.getAvatarFile(context, trimmed)
                            oldFile.renameTo(newFile)
                        }
                        // 刷新界面（ownerUserName 变化后 avatarFile 会自动重新计算）
                    }
                    showEditDialog = false
                }) {
                    Text(i18n("common_done"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(i18n("common_cancel"))
                }
            }
        )
    }

    // 卡片布局：头像居中 + 名字下方居中
    ElevatedCard(modifier = modifier.padding(vertical = 5.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像区域（可点击更换）
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .clickable { pickAvatar() },
                contentAlignment = Alignment.Center
            ) {
                if (avatarFile != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(avatarFile)
                            .setParameter("version", avatarRefresh) // ✨ 强行改变请求特征，让 Coil 缓存失效并重新加载
                            .build(),
                        contentDescription = "头像",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "默认头像",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 用户名（可点击改名）
            Text(
                text = ownerUserName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .clickable { showEditDialog = true }
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )
            if (achievements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    achievements.forEach { achievementName ->
                        AchievementLogoButton(registerName = achievementName)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}
