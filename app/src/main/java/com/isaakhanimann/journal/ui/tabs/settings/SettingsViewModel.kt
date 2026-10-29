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

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.localization.I18n
import com.isaakhanimann.journal.ui.notifications.Notifications
import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferences
import com.isaakhanimann.journal.ui.utils.DateLocaleOption
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@HiltViewModel
class SettingsViewModel @Inject constructor(

    @ApplicationContext private val context: Context,
    private val experienceRepository: ExperienceRepository,
    private val fileSystemConnection: FileSystemConnection,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private companion object {
        const val MAX_BACKUP_PHOTO_PIXELS = 100_000_000L
    }

    val isRootTabTransitionsEnabledFlow = userPreferences.isRootTabTransitionsEnabledFlow.stateIn(
        initialValue = true,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun saveRootTabTransitionsEnabled(value: Boolean) = viewModelScope.launch {
        userPreferences.saveRootTabTransitionsEnabled(value)
    }

    fun saveDosageDotsAreHidden(value: Boolean) = viewModelScope.launch {
        userPreferences.saveDosageDotsAreHidden(value)
    }

    fun saveAreSubstanceHeightsIndependent(value: Boolean) = viewModelScope.launch {
        userPreferences.saveAreSubstanceHeightsIndependent(value)
    }

    fun saveIsTimelineHidden(value: Boolean) = viewModelScope.launch {
        userPreferences.saveIsTimelineHidden(value)
    }

    val isTimelineHiddenFlow = userPreferences.isTimelineHiddenFlow.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun saveIsBottomBarPinned(value: Boolean) = viewModelScope.launch {
        userPreferences.saveIsBottomBarPinned(value)
    }

    val isBottomBarPinnedFlow = userPreferences.isBottomBarPinnedFlow.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val areSubstanceHeightsIndependentFlow = userPreferences.areSubstanceHeightsIndependentFlow.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val areDosageDotsHiddenFlow = userPreferences.areDosageDotsHiddenFlow.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun saveUse24HourClock(value: Boolean) = viewModelScope.launch {
        userPreferences.saveUse24HourClock(value)
    }

    val use24HourClockFlow = userPreferences.use24HourClockFlow.stateIn(
        initialValue = null,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun saveOpenLinkInBrowser(value: Boolean) {
        viewModelScope.launch {
            userPreferences.saveOpenLinkInBrowser(value)
        }
    }

    val isAppLockEnabledFlow = userPreferences.isAppLockEnabledFlow.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun saveAppLockEnabled(value: Boolean) {
        viewModelScope.launch {
            userPreferences.saveAppLockEnabled(value)
        }
    }

    val isEffectNotificationEnabledFlow = userPreferences.isEffectNotificationEnabledFlow.stateIn(
        initialValue = true,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun saveEffectNotificationEnabled(value: Boolean) {
        viewModelScope.launch {
            userPreferences.saveEffectNotificationEnabled(value)
            if (!value) {
                // Disabling the toggle also dismisses any effect notification
                // that is still showing.
                Notifications.cancelAllEffectNotifications(context)
            }
        }
    }

    val isOpenLinkInBrowserFlow = userPreferences.isOpenLinkInBrowserFlow.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val isMidnightCutoffEnabledFlow = userPreferences.isMidnightCutoffEnabledFlow.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun saveMidnightCutoffEnabled(value: Boolean) {
        viewModelScope.launch {
            userPreferences.saveMidnightCutoffEnabled(value)
        }
    }

    val isStatsByIngestionTimeFlow = userPreferences.isStatsByIngestionTimeFlow.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun saveStatsByIngestionTime(value: Boolean) {
        viewModelScope.launch {
            userPreferences.saveStatsByIngestionTime(value)
        }
    }

    val selectedLanguageFlow = userPreferences.selectedLanguageFlow.stateIn(
        initialValue = null,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val themePaletteFlow = userPreferences.themePaletteFlow.stateIn(
        initialValue = null,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun saveThemePalette(value: String?) {
        viewModelScope.launch { userPreferences.saveThemePalette(value) }
    }

    val dateLocaleOptionFlow = userPreferences.dateLocaleOptionFlow.stateIn(
        initialValue = DateLocaleOption.FOLLOW_LANGUAGE,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun saveDateLocaleOption(value: DateLocaleOption) {
        viewModelScope.launch {
            userPreferences.saveDateLocaleOption(value)
        }
    }

    val achievementsFlow = userPreferences.achievementsFlow.stateIn(
        initialValue = emptyList(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val ownerUserNameFlow = userPreferences.ownerUserNameFlow.stateIn(
        initialValue = "You",
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    data class BackupProgress(val completed: Int, val total: Int)
    val backupProgress = kotlinx.coroutines.flow.MutableStateFlow<BackupProgress?>(null)

    val snackbarHostState = SnackbarHostState()

    fun saveSelectedLanguage(languageKey: String?) {
        viewModelScope.launch {
            userPreferences.saveSelectedLanguage(languageKey)
        }
    }

    fun addAchievement(achievement: String) {
        viewModelScope.launch {
            userPreferences.addAchievement(achievement)
        }
    }

    fun saveOwnerUserName(userName: String?) {
        viewModelScope.launch {
            userPreferences.saveOwnerUserName(userName)
        }
    }

    private fun validateBackupPhoto(bytes: ByteArray) {
        require(bytes.isNotEmpty()) { "Photo data is empty" }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val pixelCount = bounds.outWidth.toLong() * bounds.outHeight.toLong()
        require(
            bounds.outWidth > 0 && bounds.outHeight > 0 &&
                pixelCount <= MAX_BACKUP_PHOTO_PIXELS
        ) {
            "Photo data is not a supported image or exceeds the dimension limit"
        }

        var sampleSize = 1
        while (
            (bounds.outWidth / sampleSize.toLong()) *
                (bounds.outHeight / sampleSize.toLong()) > 4_000_000L
        ) {
            sampleSize *= 2
        }
        val decoded = BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
            BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
            }
        ) ?: throw IllegalArgumentException("Photo data is not a decodable image")
        decoded.recycle()
    }

    /** Returns true when the picked file starts with the encrypted-export magic (reads only the prefix). */
    fun isImportEncrypted(uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val prefix = ByteArray(5)
                var read = 0
                while (read < prefix.size) {
                    val n = input.read(prefix, read, prefix.size - read)
                    if (n == -1) break
                    read += n
                }
                ExportEncryption.isEncryptedExport(prefix)
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun importFile(uri: Uri, password: String? = null) {
        viewModelScope.launch {
            backupProgress.value = BackupProgress(0, 0)
            val stagedAvatars = mutableListOf<Pair<File, File>>()
            val stagedPhotos = mutableListOf<String>()
            var didReplaceJournal = false
            try {
                val bytes = withContext(Dispatchers.IO) {
                    fileSystemConnection.getBytesFromUri(uri)
                }
                if (bytes == null) {
                    snackbarHostState.showSnackbar(
                        message = "File not found",
                        duration = SnackbarDuration.Short
                    )
                    return@launch
                }
                val encrypted = ExportEncryption.isEncryptedExport(bytes)
                if (encrypted && password == null) {
                    snackbarHostState.showSnackbar(
                        message = "This backup is encrypted",
                        duration = SnackbarDuration.Short
                    )
                    return@launch
                }
                val text = try {
                    withContext(Dispatchers.Default) {
                        if (encrypted) {
                            ExportEncryption.decryptExport(bytes, password!!)
                        } else {
                            bytes.toString(Charsets.UTF_8)
                        }
                    }
                } catch (_: javax.crypto.AEADBadTagException) {
                    snackbarHostState.showSnackbar(
                        message = "Wrong password",
                        duration = SnackbarDuration.Short
                    )
                    return@launch
                }
                val journalExport = withContext(Dispatchers.Default) {
                    decodeCompatibleJournalImport(text)
                }
                journalExport.preferences?.let { UserPreferences.validateBackup(it) }
                val notesWithPhotos = journalExport.experiences.flatMap { it.timedNotes }
                val photoCount = notesWithPhotos.sumOf { it.photos.size }
                var restoredPhotoCount = 0
                backupProgress.value = BackupProgress(0, photoCount)
                notesWithPhotos.forEach { timedNote ->
                    timedNote.restoredPhotoPaths = timedNote.photos.map { photo ->
                        val image = withContext(Dispatchers.Default) {
                            java.util.Base64.getDecoder().decode(photo.imageBase64).also(::validateBackupPhoto)
                        }
                        val path = withContext(Dispatchers.IO) {
                            TimedNotePhotoStorage.restorePhoto(context, image)
                        }
                        stagedPhotos.add(path)
                        restoredPhotoCount++
                        backupProgress.value = BackupProgress(restoredPhotoCount, photoCount)
                        path
                    }
                }
                val decodedAvatars = withContext(Dispatchers.Default) {
                    journalExport.avatars.map { (userName, base64) ->
                        userName to java.util.Base64.getDecoder().decode(base64)
                    }
                }
                withContext(Dispatchers.IO) {
                    decodedAvatars.forEach { (userName, decoded) ->
                        val targetFile = AvatarUtil.getAvatarFile(context, userName)
                        val directory = targetFile.parentFile
                            ?: throw IOException("Avatar destination has no parent directory")
                        if (!directory.isDirectory && !directory.mkdirs()) {
                            throw IOException("Could not create avatar directory")
                        }
                        val stagedFile = File.createTempFile("${targetFile.name}.", ".restore", directory)
                        stagedAvatars.add(stagedFile to targetFile)
                        FileOutputStream(stagedFile).use { it.write(decoded) }
                    }
                }
                val previousPhotoPaths = withContext(Dispatchers.IO) {
                    experienceRepository
                        .getAllExperiencesWithIngestionsTimedNotesAndRatingsSorted()
                        .flatMap { experience -> experience.timedNotes }
                        .flatMap { note -> experienceRepository.getTimedNotePhotos(note.id) }
                        .map { photo -> photo.filePath }
                }
                currentCoroutineContext().ensureActive()
                withContext(kotlinx.coroutines.NonCancellable + Dispatchers.IO) {
                    experienceRepository.replaceEverything(journalExport)
                    didReplaceJournal = true
                    previousPhotoPaths.forEach(TimedNotePhotoStorage::deletePhotoBlocking)
                }
                withContext(kotlinx.coroutines.NonCancellable) {
                    journalExport.preferences?.let {
                        userPreferences.restoreValidatedBackup(it)
                    }
                    withContext(Dispatchers.IO) {
                        stagedAvatars.forEach { (stagedFile, targetFile) ->
                            try {
                                Files.move(
                                    stagedFile.toPath(),
                                    targetFile.toPath(),
                                    StandardCopyOption.REPLACE_EXISTING,
                                    StandardCopyOption.ATOMIC_MOVE
                                )
                            } catch (_: AtomicMoveNotSupportedException) {
                                Files.move(
                                    stagedFile.toPath(),
                                    targetFile.toPath(),
                                    StandardCopyOption.REPLACE_EXISTING
                                )
                            }
                        }
                    }
                }
                snackbarHostState.showSnackbar(
                    message = "Import successful",
                    duration = SnackbarDuration.Short
                )
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                snackbarHostState.showSnackbar(
                    message = if (didReplaceJournal) {
                        I18n.translate(context, "settings_import_partial_success")
                    } else {
                        "Import failed"
                    },
                    duration = SnackbarDuration.Short
                )
            } finally {
                withContext(kotlinx.coroutines.NonCancellable + Dispatchers.IO) {
                    stagedAvatars.forEach { (stagedFile, _) -> stagedFile.delete() }
                    if (!didReplaceJournal) {
                        stagedPhotos.forEach { TimedNotePhotoStorage.deletePhoto(it) }
                    }
                }
                backupProgress.value = null
            }
        }
    }

    fun exportFile(uri: Uri, password: String? = null) {
        viewModelScope.launch {
            backupProgress.value = BackupProgress(0, 0)
            try {
                val ownerUserName = userPreferences.ownerUserNameFlow.firstOrNull() ?: "You"
                val userPreferencesBackup = userPreferences.exportForBackup()
                val journalExport = withContext(Dispatchers.IO) {
                    val experiencesWithIngestionsAndRatings =
                        experienceRepository.getAllExperiencesWithIngestionsTimedNotesAndRatingsSorted()
                    val photosByTimedNoteId = mutableMapOf<Int, List<com.isaakhanimann.journal.data.room.experiences.entities.TimedNotePhoto>>()
                    experiencesWithIngestionsAndRatings
                        .asSequence()
                        .flatMap { it.timedNotes.asSequence() }
                        .forEach { timedNote ->
                            photosByTimedNoteId[timedNote.id] = experienceRepository.getTimedNotePhotos(timedNote.id)
                        }
                    val photoCount = photosByTimedNoteId.values.sumOf { it.size }
                    var exportedPhotoCount = 0
                    backupProgress.value = BackupProgress(0, photoCount)

                    val experiencesSerializable = experiencesWithIngestionsAndRatings.map { entry ->
                        val location = entry.experience.location
                        ExperienceSerializable(
                            title = entry.experience.title,
                            text = entry.experience.text,
                            creationDate = entry.experience.creationDate,
                            sortDate = entry.experience.sortDate,
                            isFavorite = entry.experience.isFavorite,
                            ingestions = entry.ingestions.map { it.toIngestionSerializable() },
                            location = location?.let {
                                LocationSerializable(
                                    name = it.name,
                                    latitude = it.latitude,
                                    longitude = it.longitude
                                )
                            },
                            ratings = entry.ratings.map { rating ->
                                RatingSerializable(
                                    option = rating.option,
                                    time = rating.time,
                                    creationDate = rating.creationDate
                                )
                            },
                            timedNotes = entry.timedNotes.map { timedNote ->
                                TimedNoteSerializable(
                                    creationDate = timedNote.creationDate,
                                    time = timedNote.time,
                                    note = timedNote.note,
                                    color = timedNote.color,
                                    isPartOfTimeline = timedNote.isPartOfTimeline,
                                    photos = photosByTimedNoteId[timedNote.id].orEmpty().map { photo ->
                                        val encoded = java.util.Base64.getEncoder()
                                            .encodeToString(File(photo.filePath).readBytes())
                                        exportedPhotoCount++
                                        backupProgress.value = BackupProgress(exportedPhotoCount, photoCount)
                                        TimedNotePhotoSerializable(
                                            imageBase64 = encoded,
                                            creationDate = photo.creationDate,
                                            caption = photo.caption
                                        )
                                    }
                                )
                            }
                        )
                    }
                    val customUnitsSerializable = experienceRepository.getAllCustomUnitsSorted().map { unit ->
                        CustomUnitSerializable(
                            id = unit.id,
                            substanceName = unit.substanceName,
                            name = unit.name,
                            creationDate = unit.creationDate,
                            administrationRoute = unit.administrationRoute,
                            dose = unit.dose,
                            estimatedDoseStandardDeviation = unit.estimatedDoseStandardDeviation,
                            isEstimate = unit.isEstimate,
                            isArchived = unit.isArchived,
                            unit = unit.unit,
                            unitPlural = unit.unitPlural,
                            originalUnit = unit.originalUnit,
                            note = unit.note
                        )
                    }
                    val avatarFile = AvatarUtil.getUserAvatar(context, ownerUserName)
                    val avatars = if (avatarFile != null && avatarFile.exists()) {
                        mapOf(
                            ownerUserName to java.util.Base64.getEncoder()
                                .encodeToString(avatarFile.readBytes())
                        )
                    } else {
                        emptyMap()
                    }
                    JournalExport(
                        experiences = experiencesSerializable,
                        substanceCompanions = experienceRepository.getAllSubstanceCompanions(),
                        customSubstances = experienceRepository.getAllCustomSubstances(),
                        customUnits = customUnitsSerializable,
                        recipes = experienceRepository.getAllRecipes().map { row ->
                            RecipeSerializable(
                                id = row.recipe.id,
                                name = row.recipe.name,
                                isArchived = row.recipe.isArchived,
                                items = row.items.map { item ->
                                    RecipeItemSerializable(
                                        position = item.position,
                                        substanceName = item.substanceName,
                                        administrationRoute = item.administrationRoute,
                                        dose = item.dose,
                                        isDoseAnEstimate = item.isDoseAnEstimate,
                                        estimatedDoseStandardDeviation = item.estimatedDoseStandardDeviation,
                                        units = item.units,
                                        customUnitId = item.customUnitId
                                    )
                                }
                            )
                        },
                        preferences = userPreferencesBackup,
                        avatars = avatars
                    )
                }
                val json = withContext(Dispatchers.Default) {
                    journalExportJson.encodeToString(journalExport)
                }
                if (password == null) {
                    withContext(Dispatchers.IO) { fileSystemConnection.saveTextInUri(uri, text = json) }
                } else {
                    val encrypted = withContext(Dispatchers.Default) {
                        ExportEncryption.encryptExport(json, password)
                    }
                    withContext(Dispatchers.IO) { fileSystemConnection.saveBytesInUri(uri, encrypted) }
                }
                snackbarHostState.showSnackbar(
                    message = "Export successful",
                    duration = SnackbarDuration.Short
                )
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                snackbarHostState.showSnackbar(
                    message = "Export failed",
                    duration = SnackbarDuration.Short
                )
            } finally {
                backupProgress.value = null
            }
        }
    }

    fun deleteEverything() {
        viewModelScope.launch {
            experienceRepository.deleteEverything()
        }
    }
}
