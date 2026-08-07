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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.classes.Substance
import com.isaakhanimann.journal.data.substances.classes.roa.DoseClass
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDose
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.main.navigation.routers.ADMINISTRATION_ROUTE_KEY
import com.isaakhanimann.journal.ui.main.navigation.routers.SUBSTANCE_NAME_KEY
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.toReadableString
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ChooseDoseViewModel @Inject constructor(
    val repository: SubstanceRepository,
    private val experienceRepo: ExperienceRepository,
    state: SavedStateHandle
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
    private val substanceName = state.get<String>(SUBSTANCE_NAME_KEY)!!

    val customUnitsFlow = experienceRepo.getUnArchivedCustomUnitsFlow(substanceName).stateIn(
        initialValue = emptyList(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    var selectedCustomUnitId by mutableStateOf<Int?>(null)
        private set

    // Dialog inputs, prefilled from the current dose fields.
    var quickUnitName by mutableStateOf("")
    var quickUnitDoseText by mutableStateOf("")
    var quickUnitUnitText by mutableStateOf("")

    fun selectCustomUnit(customUnitId: Int?) {
        selectedCustomUnitId = customUnitId
    }

    fun createCustomUnit() {
        val unitDose = quickUnitDoseText.toDoubleOrNull() ?: return
        val name = quickUnitName.trim()
        if (name.isEmpty()) return
        val unit = quickUnitUnitText.trim()
        if (unit.isEmpty()) return
        val newCustomUnit = CustomUnit(
            substanceName = substanceName,
            name = name,
            administrationRoute = administrationRoute,
            dose = unitDose,
            estimatedDoseStandardDeviation = null,
            isEstimate = false,
            isArchived = false,
            unit = unit,
            originalUnit = units.ifBlank { roaDose?.units ?: "mg" },
            note = ""
        )
        viewModelScope.launch {
            val id = experienceRepo.insert(customUnit = newCustomUnit)
            selectedCustomUnitId = id
            quickUnitName = ""
            quickUnitDoseText = ""
            quickUnitUnitText = ""
        }
    }

    fun clearCustomUnit() {
        selectedCustomUnitId = null
    }

    private val purity: Double?
        get() {
            val p = purityText.toDoubleOrNull()
            return if (p != null && p > 0 && p <= 100) {
                p
            } else {
                null
            }
        }
    val isPurityValid: Boolean get() = purity != null
    val rawDoseWithUnit: String?
        get() {
            dose.let {
                if (it == null) return null
                purity.let { safePurity ->
                    if (safePurity == null) return null
                    val result = it.div(safePurity).times(100)
                    return result.toReadableString() + " ${roaDose?.units ?: ""}"
                }
            }
        }

    /** The dose value to persist: in custom-unit mode the input is a count. */
    fun doseForNext(customUnit: CustomUnit?): Double? =
        if (customUnit != null) {
            doseText.toDoubleOrNull()?.times(customUnit.dose ?: 0.0)
        } else {
            dose
        }

    val dose: Double? get() = doseText.toDoubleOrNull()
    val estimatedDoseStandardDeviation: Double? get() = estimatedDoseStandardDeviationText.toDoubleOrNull()
    val isValidDose: Boolean get() = dose != null
    val currentDoseClass: DoseClass? get() = roaDose?.getDoseClass(ingestionDose = dose)

    fun onDoseTextChange(newDoseText: String) {
        doseText = newDoseText.replace(oldChar = ',', newChar = '.')
    }

    fun onEstimatedDoseStandardDeviationChange(newEstimatedStandardDeviationText: String) {
        estimatedDoseStandardDeviationText =
            newEstimatedStandardDeviationText.replace(oldChar = ',', newChar = '.')
    }

    init {
        substance = repository.getSubstance(state.get<String>(SUBSTANCE_NAME_KEY)!!)!!
        val routeString = state.get<String>(ADMINISTRATION_ROUTE_KEY)!!
        administrationRoute = AdministrationRoute.valueOf(routeString)
        roaDose = substance.getRoa(administrationRoute)?.roaDose
        units = roaDose?.units ?: ""
    }
}
