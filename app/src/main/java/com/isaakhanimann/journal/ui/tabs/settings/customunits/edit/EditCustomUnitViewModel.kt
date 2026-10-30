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

package com.isaakhanimann.journal.ui.tabs.settings.customunits.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.classes.roa.DoseClass
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDose
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.utils.parseNonNegativeFiniteNumber
import com.isaakhanimann.journal.ui.utils.parsePositiveFiniteNumber
import com.isaakhanimann.journal.ui.main.navigation.routes.EditCustomUnitRoute
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.toReadableString
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel(assistedFactory = EditCustomUnitViewModel.Factory::class)
class EditCustomUnitViewModel @AssistedInject constructor(
    private val experienceRepo: ExperienceRepository,
    val substanceRepository: SubstanceRepository,
    @Assisted val route: EditCustomUnitRoute
) : ViewModel() {

    private var customUnit by mutableStateOf<CustomUnit?>(null)
    var isLoading by mutableStateOf(true)
        private set
    var isSaving by mutableStateOf(false)
        private set
    val isMissing: Boolean get() = !isLoading && customUnit == null

    init {
        val customUnitId = route.customUnitId
        viewModelScope.launch {
            try {
                val customUnitWithIngestions = experienceRepo.getCustomUnitWithIngestions(customUnitId)
                val loadedCustomUnit = customUnitWithIngestions?.customUnit
                customUnit = loadedCustomUnit
                if (loadedCustomUnit != null) {
                    substanceName = loadedCustomUnit.substanceName
                    administrationRoute = loadedCustomUnit.administrationRoute
                    val substance = substanceRepository.getSubstance(loadedCustomUnit.substanceName)
                    roaDose = substance?.getRoa(loadedCustomUnit.administrationRoute)?.roaDose
                    originalUnit = loadedCustomUnit.originalUnit
                    name = loadedCustomUnit.name
                    val pluralizableUnit = loadedCustomUnit.getPluralizableUnit()
                    unit = loadedCustomUnit.unit
                    unitPlural = pluralizableUnit.plural
                    doseText = loadedCustomUnit.dose?.toReadableString() ?: ""
                    estimatedDoseDeviationText =
                        loadedCustomUnit.estimatedDoseStandardDeviation?.toReadableString() ?: ""
                    isEstimate = loadedCustomUnit.isEstimate
                    isArchived = loadedCustomUnit.isArchived
                    note = loadedCustomUnit.note
                }
                numberOfIngestionsWithThisCustomUnit = customUnitWithIngestions?.ingestions?.size
            } finally {
                isLoading = false
            }
        }
    }

    var roaDose: RoaDose? = null

    var name by mutableStateOf("")

    var numberOfIngestionsWithThisCustomUnit: Int? by mutableStateOf(null)

    var substanceName by mutableStateOf("")
    var administrationRoute by mutableStateOf(AdministrationRoute.ORAL)

    val currentDoseClass: DoseClass? get() = roaDose?.getDoseClass(ingestionDose = dose)

    fun onChangeOfName(newName: String) {
        name = newName
    }

    var unit by mutableStateOf("")

    fun onChangeOfUnit(newUnit: String) {
        unit = newUnit
    }

    var unitPlural by mutableStateOf("")

    fun onChangeOfUnitPlural(newUnit: String) {
        unitPlural = newUnit
    }

    var originalUnit by mutableStateOf("")

    fun onChangeOfOriginalUnit(newUnit: String) {
        originalUnit = newUnit
    }

    var doseText by mutableStateOf("")
    fun onChangeOfDose(newDose: String) {
        doseText = newDose.replace(',', '.')
    }
    val dose: Double? get() = parsePositiveFiniteNumber(doseText)

    var estimatedDoseDeviationText by mutableStateOf("")
    fun onChangeOfEstimatedDoseDeviation(newEstimatedDoseDeviation: String) {
        estimatedDoseDeviationText = newEstimatedDoseDeviation.replace(',', '.')
    }
    val estimatedDoseStandardDeviation: Double?
        get() = parseNonNegativeFiniteNumber(estimatedDoseDeviationText)

    var isEstimate by mutableStateOf(false)
    val isDoseInputValid: Boolean get() = doseText.isBlank() || dose != null
    val isEstimatedDeviationInputValid: Boolean
        get() = !isEstimate || estimatedDoseDeviationText.isBlank() || estimatedDoseStandardDeviation != null
    val canSave: Boolean
        get() = !isLoading && !isSaving && customUnit != null && name.isNotBlank() && unit.isNotBlank() &&
            isDoseInputValid && isEstimatedDeviationInputValid &&
            (dose == null || originalUnit.isNotBlank())

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

    fun updateAndDismissAfter(dismiss: () -> Unit) {
        if (!canSave) return
        val currentCustomUnit = customUnit ?: return
        val savedDose = dose
        val savedIsEstimate = isEstimate && savedDose != null
        val updatedCustomUnit = currentCustomUnit.copy(
            dose = savedDose,
            name = name.trim(),
            isEstimate = savedIsEstimate,
            originalUnit = originalUnit.trim(),
            unitPlural = unitPlural.trim().takeIf { it.isNotEmpty() },
            estimatedDoseStandardDeviation =
                if (savedIsEstimate) estimatedDoseStandardDeviation else null,
            isArchived = isArchived,
            unit = unit.trim(),
            note = note
        )
        isSaving = true
        viewModelScope.launch {
            try {
                if (experienceRepo.update(updatedCustomUnit) > 0) {
                    customUnit = updatedCustomUnit
                    withContext(Dispatchers.Main) {
                        dismiss()
                    }
                } else {
                    customUnit = null
                }
            } finally {
                isSaving = false
            }
        }
    }

    fun deleteCustomUnit(dismiss: () -> Unit) {
        if (isLoading || isSaving) return
        val currentCustomUnit = customUnit ?: return
        isSaving = true
        viewModelScope.launch {
            try {
                experienceRepo.delete(customUnit = currentCustomUnit)
                customUnit = null
                withContext(Dispatchers.Main) {
                    dismiss()
                }
            } finally {
                isSaving = false
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(route: EditCustomUnitRoute): EditCustomUnitViewModel
    }
}
