/*
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

package com.isaakhanimann.journal.ui.utils

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.isaakhanimann.journal.localization.i18n
import java.io.File

/**
 * Lets the user attach a photo from the gallery or shoot a new one.
 *
 * The camera writes to a FileProvider URI we own, so the result is full resolution and can be
 * read back without any extra storage permission. Both paths hand a content [Uri] to the
 * caller, which is expected to copy it into internal storage.
 */
@Composable
fun PhotoPickerDialog(
    onDismiss: () -> Unit,
    onPhotoSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    // The camera target must survive recomposition, otherwise the URI we hand to the camera
    // intent no longer points at the file we later read back.
    val cameraTarget = remember { createCameraOutputUri(context) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) onPhotoSelected(uri)
        onDismiss()
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val target = cameraTarget
        if (success && target != null) onPhotoSelected(target)
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(i18n("timed_note_add_photo")) },
        text = {
            Column {
                ListItem(
                    headlineContent = { Text(i18n("timed_note_photo_from_gallery")) },
                    leadingContent = {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    },
                    modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
                )
                ListItem(
                    headlineContent = { Text(i18n("timed_note_photo_from_camera")) },
                    leadingContent = {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        val target = cameraTarget
                        if (target != null) cameraLauncher.launch(target)
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(i18n("common_cancel")) }
        }
    )
}

/**
 * Creates an empty cache file the camera can write into and returns its FileProvider URI.
 * Returns null when the provider is not configured, in which case only the gallery is usable.
 */
private fun createCameraOutputUri(context: Context): Uri? = runCatching {
    val directory = File(context.cacheDir, "note_photos").apply { mkdirs() }
    val file = File(directory, "camera_${System.currentTimeMillis()}.jpg")
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}.getOrNull()
