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

package com.isaakhanimann.journal.ui.tabs.journal.addingestion.dose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.classes.Substance
import com.isaakhanimann.journal.data.substances.classes.roa.DoseClass
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDose
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseDoseRoute
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.toReadableString
import com.isaakhanimann.journal.ui.utils.parseNonNegativeFiniteNumber
import com.isaakhanimann.journal.ui.utils.parsePositiveFiniteNumber
import com.isaakhanimann.journal.ui.utils.scaleDoseByUnit
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ChooseDoseViewModel.Factory::class)
class ChooseDoseViewModel @AssistedInject constructor(
    val repository: SubstanceRepository,
    private val experienceRepo: ExperienceRepository,
    @Assisted val route: ChooseDoseRoute
) : ViewModel() {
    val substance: Substance
    val administrationRoute: AdministrationRoute
    val roaDose: RoaDose?
    var isEstimate by mutableStateOf(false)
    var doseText by mutableStateOf("")
    var estimatedDoseStandardDeviationText by mutableStateOf("")
    var purityText by mutableStateOf("100")
    var units by mutableStateOf("")

    // --- Quick custom unit support ---
    private val substanceName = route.substanceName

    val customUnitsFlow = experienceRepo.getUnArchivedCustomUnitsFlow(substanceName)
        .map { customUnits ->
            customUnits.filter { it.administrationRoute == route.administrationRoute }
        }
        .stateIn(
            initialValue = emptyList(),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    var selectedCustomUnitId by mutableStateOf<Int?>(null)
        private set
    var recentlyCreatedCustomUnit by mutableStateOf<CustomUnit?>(null)
        private set
    val selectedCustomUnit: CustomUnit?
        get() = customUnitsFlow.value.firstOrNull { it.id == selectedCustomUnitId }
            ?: recentlyCreatedCustomUnit?.takeIf { it.id == selectedCustomUnitId }
    fun customUnitsForDisplay(currentUnits: List<CustomUnit>): List<CustomUnit> =
        recentlyCreatedCustomUnit?.let { created ->
            if (currentUnits.any { it.id == created.id }) currentUnits else currentUnits + created
        } ?: currentUnits
    val doseUnits: String
        get() = selectedCustomUnit?.originalUnit?.ifBlank { units }
            ?: units.ifBlank { roaDose?.units.orEmpty() }
    private val effectiveDose: Double?
        get() = if (selectedCustomUnit == null) dose else scaleDoseByUnit(dose, selectedCustomUnit?.dose)

    // Dialog inputs for creating a quick custom unit.
    var quickUnitName by mutableStateOf("")
    var quickUnitDoseText by mutableStateOf("")
    var quickUnitUnitText by mutableStateOf("")
    var isCreatingCustomUnit by mutableStateOf(false)
        private set
    val canCreateCustomUnit: Boolean
        get() = quickUnitName.isNotBlank() && parsePositiveFiniteNumber(quickUnitDoseText) != null &&
            quickUnitUnitText.isNotBlank() && !isCreatingCustomUnit

    fun selectCustomUnit(customUnitId: Int?) {
        selectedCustomUnitId = customUnitId
    }

    fun onQuickUnitDoseTextChange(value: String) {
        quickUnitDoseText = value.replace(',', '.')
    }

    fun createCustomUnit() {
        if (!canCreateCustomUnit) return
        val unitDose = checkNotNull(parsePositiveFiniteNumber(quickUnitDoseText))
        val name = quickUnitName.trim()
        val unit = quickUnitUnitText.trim()
        val newCustomUnit = CustomUnit(
            substanceName = substanceName,
            name = name,
            administrationRoute = administrationRoute,
            dose = unitDose,
            estimatedDoseStandardDeviation = null,
            isEstimate = false,
            isArchived = false,
            unit = unit,
            originalUnit = doseUnits.ifBlank { "mg" },
            note = ""
        )
        isCreatingCustomUnit = true
        viewModelScope.launch {
            try {
                val id = experienceRepo.insert(customUnit = newCustomUnit)
                recentlyCreatedCustomUnit = newCustomUnit.copy(id = id)
                selectedCustomUnitId = id
                quickUnitName = ""
                quickUnitDoseText = ""
                quickUnitUnitText = ""
            } finally {
                isCreatingCustomUnit = false
            }
        }
    }

    fun clearCustomUnit() {
        selectedCustomUnitId = null
    }

    private val purity: Double?
        get() = purityText.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 && it <= 100.0 }
    val isPurityValid: Boolean get() = purity != null
    val rawDoseWithUnit: String?
        get() {
            val currentDose = effectiveDose ?: return null
            val safePurity = purity ?: return null
            val result = currentDose.div(safePurity).times(100)
            if (!result.isFinite()) return null
            return result.toReadableString() + " $doseUnits"
        }

    val dose: Double? get() = parseNonNegativeFiniteNumber(doseText)
    val estimatedDoseStandardDeviation: Double?
        get() = parseNonNegativeFiniteNumber(estimatedDoseStandardDeviationText)
    val isValidDose: Boolean get() = dose != null
    val isValidEstimatedDoseStandardDeviation: Boolean
        get() = !isEstimate || estimatedDoseStandardDeviationText.isBlank() ||
            estimatedDoseStandardDeviation != null
    val currentDoseClass: DoseClass?
        get() = roaDose?.getDoseClass(ingestionDose = effectiveDose)


    fun onDoseTextChange(newDoseText: String) {
        doseText = newDoseText.replace(oldChar = ',', newChar = '.')
    }

    fun onEstimatedDoseStandardDeviationChange(newEstimatedStandardDeviationText: String) {
        estimatedDoseStandardDeviationText =
            newEstimatedStandardDeviationText.replace(oldChar = ',', newChar = '.')
    }

    init {
        substance = repository.getSubstance(route.substanceName)!!
        administrationRoute = route.administrationRoute
        roaDose = substance.getRoa(administrationRoute)?.roaDose
        units = roaDose?.units ?: ""
    }

    @AssistedFactory
    interface Factory {
        fun create(route: ChooseDoseRoute): ChooseDoseViewModel
    }
}
