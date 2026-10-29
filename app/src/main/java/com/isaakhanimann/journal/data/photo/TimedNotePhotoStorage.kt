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

package com.isaakhanimann.journal.data.photo

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Owns where timed-note photos live on disk.
 *
 * Photos are copied into internal storage instead of keeping the picked content URI, because
 * those URIs are either short-lived permissions or point at files the user can move/delete.
 */
object TimedNotePhotoStorage {

    private const val DIRECTORY_NAME = "timed_note_photos"

    private fun photosDirectory(context: Context): File =
        File(context.filesDir, DIRECTORY_NAME).apply { mkdirs() }

    /**
     * Copies [sourceUri] into internal storage and returns the absolute path, or null when the
     * source could not be read (revoked permission, deleted file, decode failure).
     */
    suspend fun importPhoto(context: Context, sourceUri: Uri): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                // A timestamp alone collides when two photos are imported within the same
                // millisecond, and the second write would silently clobber the first.
                val destination = File(
                    photosDirectory(context),
                    "note_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
                )
                val copied = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    destination.outputStream().use { output -> input.copyTo(output) }
                    true
                } ?: false
                if (!copied) {
                    // Nothing was written, so do not leave the empty placeholder behind.
                    destination.delete()
                    return@runCatching null
                }
                destination.absolutePath
            }.getOrNull()
        }

    /**
     * Deletes the backing file for a stored photo path. Safe to call for paths that no longer
     * exist, so note deletion does not fail on a stale row.
     */
    suspend fun deletePhoto(filePath: String) = withContext(Dispatchers.IO) {
        deletePhotoBlocking(filePath)
    }

    /**
     * Blocking variant for places with no coroutine scope, notably [ViewModel.onCleared].
     * The files are small, so the stall is negligible and it avoids leaving orphans behind when
     * the process is on its way out.
     */
    fun deletePhotoBlocking(filePath: String) {
        runCatching { File(filePath).delete() }
    }
}
