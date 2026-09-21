/*
 * Copyright (c) 2022. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.tabs.search.custom

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.CustomSubstance
import com.isaakhanimann.journal.ui.main.navigation.routes.EditCustomSubstanceRoute
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EditCustomSubstanceViewModel.Factory::class)
class EditCustomSubstanceViewModel @AssistedInject constructor(
    val experienceRepo: ExperienceRepository,
    @Assisted val route: EditCustomSubstanceRoute
) : ViewModel() {

    var id = 0
    var name by mutableStateOf("")
    var units by mutableStateOf("")
    var description by mutableStateOf("")

    val isValid get() = name.isNotBlank() && units.isNotBlank()

    init {
        val customSubstanceId = route.customSubstanceId
        viewModelScope.launch {
            val customSubstance =
                experienceRepo.getCustomSubstanceFlow(customSubstanceId).firstOrNull()
                    ?: return@launch
            id = customSubstanceId
            name = customSubstance.name
            units = customSubstance.units
            description = customSubstance.description
        }
    }

    fun onDoneTap() {
        viewModelScope.launch {
            val customSubstance = CustomSubstance(
                id,
                name,
                units,
                description
            )
            experienceRepo.insert(customSubstance)
        }
    }

    fun deleteCustomSubstance() {
        viewModelScope.launch {
            experienceRepo.delete(CustomSubstance(id, name, units, description))
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(route: EditCustomSubstanceRoute): EditCustomSubstanceViewModel
    }
}
