/*
 * Copyright (c) 2024. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.tabs.settings.customunits.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.substances.classes.Substance
import com.isaakhanimann.journal.data.substances.classes.roa.DoseClass
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.main.navigation.routes.FinishAddCustomUnitRoute
import com.isaakhanimann.journal.ui.utils.parseNonNegativeFiniteNumber
import com.isaakhanimann.journal.ui.utils.parsePositiveFiniteNumber
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel(assistedFactory = FinishAddCustomUnitViewModel.Factory::class)
class FinishAddCustomUnitViewModel @AssistedInject constructor(
    private val experienceRepo: ExperienceRepository,
    val substanceRepository: SubstanceRepository,
    @Assisted val route: FinishAddCustomUnitRoute
) : ViewModel() {
    var substanceName by mutableStateOf(route.substanceName)
    val administrationRoute = route.administrationRoute

    var isLoading by mutableStateOf(true)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var substance by mutableStateOf<Substance?>(null)
    val roaDose get() = substance?.getRoa(administrationRoute)?.roaDose

    var name by mutableStateOf("")
    var isUnitsFieldShown by mutableStateOf(false)

    val currentDoseClass: DoseClass? get() = roaDose?.getDoseClass(ingestionDose = dose)

    fun onChangeOfName(newName: String) {
        name = newName
    }

    var unit by mutableStateOf("")

    fun onChangeOfUnit(newUnit: String) {
        unit = newUnit
        unitPlural = if (newUnit != "mg" && newUnit != "g" && newUnit.lowercase() != "ml" && newUnit.lastOrNull() != 's') {
            newUnit + "s"
        } else {
            newUnit
        }
    }

    var unitPlural by mutableStateOf("")

    fun onChangeOfUnitPlural(newUnit: String) {
        unitPlural = newUnit
    }

    var originalUnit by mutableStateOf("mg")

    fun onChangeOfOriginalUnit(newUnit: String) {
        originalUnit = newUnit
    }

    var doseText by mutableStateOf("")
    fun onChangeOfDose(newDose: String) {
        doseText = newDose.replace(',', '.')
    }

    val dose: Double? get() = parsePositiveFiniteNumber(doseText)
    private val estimatedDoseDeviation: Double?
        get() = parseNonNegativeFiniteNumber(estimatedDoseDeviationText)
    private val isDoseInputValid: Boolean get() = doseText.isBlank() || dose != null
    private val isEstimatedDeviationInputValid: Boolean
        get() = !isEstimate || estimatedDoseDeviationText.isBlank() || estimatedDoseDeviation != null
    val canSave: Boolean
        get() = !isLoading && !isSaving && substanceName.isNotBlank() && name.isNotBlank() &&
            unit.isNotBlank() && isDoseInputValid && isEstimatedDeviationInputValid &&
            (dose == null || originalUnit.isNotBlank())

    var estimatedDoseDeviationText by mutableStateOf("")
    fun onChangeOfEstimatedDoseDeviation(newEstimatedDoseDeviation: String) {
        estimatedDoseDeviationText = newEstimatedDoseDeviation.replace(',', '.')
    }
    var isEstimate by mutableStateOf(false)
    fun onChangeOfIsEstimate(newIsEstimate: Boolean) {
        isEstimate = newIsEstimate
    }

    var isArchived by mutableStateOf(false)
    fun onChangeOfIsArchived(newIsArchived: Boolean) {
        isArchived = newIsArchived
    }

    var note by mutableStateOf("")

    fun onChangeOfNote(newNote: String) {
        note = newNote
    }

    init {
        originalUnit = roaDose?.units ?: ""
        viewModelScope.launch {
            try {
                substance = substanceRepository.getSubstance(substanceName)
                originalUnit = roaDose?.units ?: "mg"
                isUnitsFieldShown = roaDose?.units?.isBlank() ?: true
                if (substance == null || roaDose?.units == null) {
                    val customSubstance = experienceRepo.getCustomSubstance(substanceName)
                    if (customSubstance != null) {
                        originalUnit = customSubstance.units
                        isUnitsFieldShown = customSubstance.units.isBlank()
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun createSaveAndDismissAfter(dismiss: (customUnitId: Int) -> Unit) {
        if (!canSave) return
        val savedDose = dose
        val savedIsEstimate = isEstimate && savedDose != null
        val customUnit = CustomUnit(
            substanceName = substanceName.trim(),
            name = name.trim(),
            administrationRoute = administrationRoute,
            dose = savedDose,
            isEstimate = savedIsEstimate,
            estimatedDoseStandardDeviation = if (savedIsEstimate) estimatedDoseDeviation else null,
            isArchived = isArchived,
            unit = unit.trim(),
            unitPlural = unitPlural.trim().takeIf { it.isNotEmpty() },
            originalUnit = originalUnit.trim(),
            note = note
        )
        isSaving = true
        viewModelScope.launch {
            try {
                val customUnitId = experienceRepo.insert(customUnit)
                withContext(Dispatchers.Main) {
                    dismiss(customUnitId)
                }
            } finally {
                isSaving = false
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(route: FinishAddCustomUnitRoute): FinishAddCustomUnitViewModel
    }
}
