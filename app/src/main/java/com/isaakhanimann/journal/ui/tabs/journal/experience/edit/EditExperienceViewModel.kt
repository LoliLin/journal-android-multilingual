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

package com.isaakhanimann.journal.ui.tabs.journal.experience.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.Experience
import com.isaakhanimann.journal.data.room.experiences.entities.Location
import com.isaakhanimann.journal.ui.main.navigation.routes.EditExperienceRoute
import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EditExperienceViewModel.Factory::class)
class EditExperienceViewModel @AssistedInject constructor(
    private val repository: ExperienceRepository,
    @Assisted val route: EditExperienceRoute,
    private val userPreferences: UserPreferences
) : ViewModel() {

    var experience by mutableStateOf<Experience?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var enteredTitle by mutableStateOf("")
    val isEnteredTitleOk get() = enteredTitle.isNotBlank()
    val canSave get() = experience != null && isEnteredTitleOk && !isSaving
    var enteredText by mutableStateOf("")
    var enteredLocation by mutableStateOf("")
    private var oldLongitude: Double? = null
    private var oldLatitude: Double? = null

    val ownerUserNameFlow = userPreferences.ownerUserNameFlow.stateIn(
        initialValue = "You",
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    init {
        val id = route.experienceId
        viewModelScope.launch {
            try {
                val loadedExperience = repository.getExperience(id = id) ?: return@launch
                experience = loadedExperience
                enteredTitle = loadedExperience.title
                enteredText = loadedExperience.text
                enteredLocation = loadedExperience.location?.name ?: ""
                oldLongitude = loadedExperience.location?.longitude
                oldLatitude = loadedExperience.location?.latitude
            } finally {
                isLoading = false
            }
        }
    }

    fun onDoneTap(onSaved: () -> Unit = {}) {
        val currentExperience = experience ?: return
        if (!canSave) return

        val location = if (enteredLocation.isNotBlank()) {
            Location(
                name = enteredLocation,
                longitude = oldLongitude,
                latitude = oldLatitude
            )
        } else {
            null
        }
        val updatedExperience = currentExperience.copy(
            title = enteredTitle.trim(),
            text = enteredText,
            location = location
        )

        isSaving = true
        viewModelScope.launch {
            try {
                if (!repository.updateExperienceIfExists(updatedExperience)) {
                    experience = null
                    return@launch
                }
                experience = updatedExperience
                onSaved()
            } finally {
                isSaving = false
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(route: EditExperienceRoute): EditExperienceViewModel
    }
}
