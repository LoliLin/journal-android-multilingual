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

package com.isaakhanimann.journal.ui.tabs.journal.experience.timednote

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import androidx.compose.ui.platform.LocalContext

/**
 * Reads the photo paths attached to one timed note.
 *
 * This is deliberately scoped to a single note instead of being folded into the experience
 * model: notes are rendered in several places (experience screen, share card, journal preview)
 * and threading a photo map through all of them would touch far more code than it buys.
 */
@Composable
fun rememberTimedNotePhotoPaths(timedNoteId: Int): List<String> {
    val context = LocalContext.current
    val repository = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            TimedNotePhotoEntryPoint::class.java
        ).experienceRepository()
    }
    val photos by remember(repository, timedNoteId) {
        repository.getTimedNotePhotosFlow(timedNoteId)
    }.collectAsState(initial = emptyList())
    return photos.map { it.filePath }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TimedNotePhotoEntryPoint {
    fun experienceRepository(): ExperienceRepository
}
