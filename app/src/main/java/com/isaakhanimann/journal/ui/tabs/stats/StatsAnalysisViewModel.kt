/*
 * Copyright (c) 2026. Journal Multilingual fork.
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

package com.isaakhanimann.journal.ui.tabs.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithCompanionAndCustomUnit
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TotalDoseLine(
    val substanceName: String,
    val units: String,
    val totalDose: Double,
    val color: AdaptiveColor?
)

data class StatsAnalysisModel(
    val selectedSubstances: Set<String>,
    val selectedConsumerName: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val ingestionCount: Int,
    val totalDoseBySubstance: List<TotalDoseLine>,
    val doseFrequency: List<Pair<Double, Int>>,
    val perDayCounts: List<Pair<LocalDate, Int>>,
    val primaryColor: AdaptiveColor?,
    val ingestions: List<IngestionWithCompanionAndCustomUnit>
)

@HiltViewModel
class StatsAnalysisViewModel @Inject constructor(
    experienceRepository: ExperienceRepository,
    val substanceRepo: SubstanceRepository
) : ViewModel() {

    private val ingestionsFlow =
        experienceRepository.getSortedIngestionsWithSubstanceCompanionsFlow(limit = 100000)

    val usedSubstancesFlow: StateFlow<List<String>> = ingestionsFlow.map { list ->
        list.map { it.ingestion.substanceName }.distinct().sorted()
    }.stateIn(
        initialValue = emptyList(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val consumerNamesFlow: StateFlow<List<String>> = ingestionsFlow.map { list ->
        list.mapNotNull { it.ingestion.consumerName }.distinct().sorted()
    }.stateIn(
        initialValue = emptyList(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    private val _selectedConsumerName = MutableStateFlow<String?>(null)
    val selectedConsumerNameFlow: StateFlow<String?> = _selectedConsumerName.asStateFlow()

    fun setSelectedConsumerName(consumerName: String?) {
        _selectedConsumerName.value = consumerName
    }

    private val _selectedSubstances = MutableStateFlow<Set<String>>(emptySet())
    val selectedSubstancesFlow: StateFlow<Set<String>> = _selectedSubstances.asStateFlow()

    fun toggleSubstance(substanceName: String) {
        _selectedSubstances.value = _selectedSubstances.value.let { current ->
            if (substanceName in current) {
                current - substanceName
            } else {
                current + substanceName
            }
        }
    }

    fun clearSubstances() {
        _selectedSubstances.value = emptySet()
    }

    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDateFlow: StateFlow<LocalDate?> = _startDate.asStateFlow()

    fun setStartDate(date: LocalDate?) {
        _startDate.value = date
    }

    private val _endDate = MutableStateFlow<LocalDate?>(null)
    val endDateFlow: StateFlow<LocalDate?> = _endDate.asStateFlow()

    fun setEndDate(date: LocalDate?) {
        _endDate.value = date
    }

    fun clearTimeRange() {
        _startDate.value = null
        _endDate.value = null
    }

    val modelFlow: StateFlow<StatsAnalysisModel> = combine(
        ingestionsFlow,
        _selectedSubstances,
        _selectedConsumerName,
        _startDate,
        _endDate
    ) { ingestions, selected, consumerName, start, end ->
        val zone = ZoneId.systemDefault()
        val filtered = ingestions.filter { ingestionWith ->
            if (ingestionWith.ingestion.substanceName !in selected) {
                return@filter false
            }
            if (consumerName != null && ingestionWith.ingestion.consumerName != consumerName) {
                return@filter false
            }
            val date = ingestionWith.ingestion.time.atZone(zone).toLocalDate()
            val isAfterStart = start == null || !date.isBefore(start)
            val isBeforeEnd = end == null || !date.isAfter(end)
            isAfterStart && isBeforeEnd
        }
        val totalDoseBySubstance = filtered
            .groupBy { it.ingestion.substanceName to (it.ingestion.units ?: "") }
            .map { (key, list) ->
                TotalDoseLine(
                    substanceName = key.first,
                    units = key.second,
                    totalDose = list.sumOf { it.ingestion.dose ?: 0.0 },
                    color = list.firstOrNull()?.substanceCompanion?.color
                )
            }
            .sortedBy { it.substanceName }
        val doseFrequency = filtered
            .mapNotNull { it.ingestion.dose }
            .groupBy { it }
            .map { (dose, list) -> dose to list.size }
            .sortedBy { it.first }
        val perDayCounts = filtered
            .groupBy { it.ingestion.time.atZone(zone).toLocalDate() }
            .map { (date, list) -> date to list.size }
            .sortedBy { it.first }
        StatsAnalysisModel(
            selectedSubstances = selected,
            selectedConsumerName = consumerName,
            startDate = start,
            endDate = end,
            ingestionCount = filtered.size,
            totalDoseBySubstance = totalDoseBySubstance,
            doseFrequency = doseFrequency,
            perDayCounts = perDayCounts,
            primaryColor = filtered.firstOrNull()?.substanceCompanion?.color,
            ingestions = filtered
        )
    }.stateIn(
        initialValue = StatsAnalysisModel(
            selectedSubstances = emptySet(),
            selectedConsumerName = null,
            startDate = null,
            endDate = null,
            ingestionCount = 0,
            totalDoseBySubstance = emptyList(),
            doseFrequency = emptyList(),
            perDayCounts = emptyList(),
            primaryColor = null,
            ingestions = emptyList()
        ),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )
}
