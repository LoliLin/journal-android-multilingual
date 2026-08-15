/*
 * Copyright (c) 2022-2026. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.tabs.journal.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class NotesViewModel @Inject constructor(
    experienceRepo: ExperienceRepository,
    val substanceRepo: SubstanceRepository
) : ViewModel() {

    val searchTextFlow = MutableStateFlow("")

    fun onSearchChange(newText: String) {
        searchTextFlow.value = newText
    }

    val notesFlow: StateFlow<List<NoteRowData>> =
        experienceRepo.getSortedIngestionsFlow()
            .combine(searchTextFlow) { ingestions, searchText ->
                ingestions.mapNotNull { ingestion ->
                    val note = ingestion.notes?.trim()
                    if (note.isNullOrEmpty()) {
                        return@mapNotNull null
                    }
                    if (searchText.isNotBlank() && !note.contains(searchText, ignoreCase = true)) {
                        return@mapNotNull null
                    }
                    NoteRowData(
                        ingestionId = ingestion.id,
                        note = note,
                        substanceName = ingestion.substanceName,
                        time = ingestion.time,
                        experienceId = ingestion.experienceId
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}

data class NoteRowData(
    val ingestionId: Int,
    val note: String,
    val substanceName: String,
    val time: Instant,
    val experienceId: Int
)
