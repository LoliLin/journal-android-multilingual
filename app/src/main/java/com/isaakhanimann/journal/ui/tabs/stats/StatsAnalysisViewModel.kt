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
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.ReleaseForm
import com.isaakhanimann.journal.data.substances.classes.roa.DoseClass
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDose
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

enum class AnalysisPeriodPreset { ALL_TIME, LAST_30_DAYS, LAST_90_DAYS, THIS_YEAR, LAST_YEAR, CUSTOM }

internal fun trailingCalendarDateRange(today: LocalDate, dayCount: Long): Pair<LocalDate, LocalDate> {
    require(dayCount > 0) { "dayCount must be positive" }
    return today.minusDays(dayCount - 1) to today
}

// null-consumer ingestions belong to the owner; All aggregates everyone
sealed interface ConsumerSelection {
    data object All : ConsumerSelection
    data object Owner : ConsumerSelection
    data class Specific(val name: String) : ConsumerSelection
}

data class TotalDoseLine(
    val substanceName: String,
    val units: String,
    val color: AdaptiveColor?,
    val absoluteTotal: Double,
    val relativeTotal: Double?,
    val customColorArgb: Int? = null
)

data class SubstanceChartData(
    val substanceName: String,
    val color: AdaptiveColor?,
    // dose distribution: values are relative doses (x common dose) when relativeDoseData exists,
    // otherwise raw doses in the substance's unit; labels are derived via bucketLabel
    val doseBuckets: List<Pair<Double, Int>>,
    val bucketLabel: (Double) -> String,
    val doseClassCounts: List<Pair<DoseClass, Int>>,
    val perDayCumulativeRelative: List<Pair<LocalDate, Double>>,
    val absoluteTotal: Double,
    val relativeTotal: Double?,
    val units: String?,
    val unknownDoseCount: Int,
    val customColorArgb: Int? = null
)

data class StatsAnalysisModel(
    val selectedSubstances: Set<String>,
    val selectedConsumer: ConsumerSelection,
    val selectedPreset: AnalysisPeriodPreset,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val effectiveStart: LocalDate?,
    val effectiveEnd: LocalDate?,
    val ingestionCount: Int,
    val totalDoseBySubstance: List<TotalDoseLine>,
    val perSubstanceCharts: List<SubstanceChartData>,
    val ingestions: List<IngestionWithCompanionAndCustomUnit>
)

/**
 * The reference dose for relative measurements: the midpoint of the common dose range
 * [commonMin, strongMin), falling back to commonMin when the range has no upper bound.
 */
internal fun commonDoseReference(commonMin: Double?, strongMin: Double?): Double? =
    when {
        commonMin == null -> null
        strongMin != null -> (commonMin + strongMin) / 2
        else -> commonMin
    }

/**
 * Relative dose of one ingestion: the pure dose divided by the common dose reference of the
 * substance's ROA, defined only when the entry is expressed in the ROA's units.
 */
internal fun relativeDoseOfIngestion(
    substanceRepo: SubstanceRepository,
    ingestionWith: IngestionWithCompanionAndCustomUnit
): Double? {
    val ingestion = ingestionWith.ingestion
    val roaDose = substanceRepo.getSubstance(ingestion.substanceName)
        ?.getRoa(ingestion.administrationRoute, ingestion.releaseForm)?.roaDose
    return relativeDoseOfIngestion(ingestionWith, roaDose)
}

private fun relativeDoseOfIngestion(
    ingestionWith: IngestionWithCompanionAndCustomUnit,
    roaDose: RoaDose?
): Double? {
    val ingestion = ingestionWith.ingestion
    roaDose ?: return null
    val reference = commonDoseReference(roaDose.commonMin, roaDose.strongMin) ?: return null
    if (reference <= 0) return null
    // relative dose is only defined when the entry is expressed in the ROA's units
    val unit = ingestionWith.originalUnit
    if (unit != null && roaDose.units != unit) return null
    val pureDose = ingestionWith.pureDose ?: return null
    return pureDose / reference
}

@HiltViewModel
class StatsAnalysisViewModel @Inject constructor(
    experienceRepository: ExperienceRepository,
    val substanceRepo: SubstanceRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val ingestionInvalidationsFlow =
        experienceRepository.getSortedIngestionsWithSubstanceCompanionsFlow(limit = 1)
            .map { Unit }
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                replay = 1
            )

    val ownerUserNameFlow: StateFlow<String?> = userPreferences.ownerUserNameFlow.stateIn(
        initialValue = "You",
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    private val _selectedConsumer = MutableStateFlow<ConsumerSelection>(ConsumerSelection.All)
    val selectedConsumerFlow: StateFlow<ConsumerSelection> = _selectedConsumer.asStateFlow()

    fun selectAllConsumers() {
        _selectedConsumer.value = ConsumerSelection.All
    }

    fun selectOwner() {
        _selectedConsumer.value = ConsumerSelection.Owner
    }

    fun selectConsumer(name: String) {
        _selectedConsumer.value = ConsumerSelection.Specific(name)
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

    fun setSelectedSubstances(substances: Set<String>) {
        _selectedSubstances.value = substances
    }

    fun syncConsumer(consumerName: String?) {
        _selectedConsumer.value = if (consumerName == null) {
            ConsumerSelection.Owner
        } else {
            ConsumerSelection.Specific(consumerName)
        }
    }

    fun syncDateRange(start: LocalDate?, end: LocalDate?) {
        _startDate.value = start
        _endDate.value = end
        _selectedPreset.value = AnalysisPeriodPreset.CUSTOM
    }

    private val _selectedPreset = MutableStateFlow(AnalysisPeriodPreset.ALL_TIME)
    val selectedPresetFlow: StateFlow<AnalysisPeriodPreset> = _selectedPreset.asStateFlow()

    fun setPreset(preset: AnalysisPeriodPreset) {
        _selectedPreset.value = preset
        if (preset != AnalysisPeriodPreset.CUSTOM) {
            _startDate.value = null
            _endDate.value = null
        }
    }

    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDateFlow: StateFlow<LocalDate?> = _startDate.asStateFlow()

    fun setStartDate(date: LocalDate?) {
        _startDate.value = date
        if (date != null) {
            _selectedPreset.value = AnalysisPeriodPreset.CUSTOM
        }
    }

    private val _endDate = MutableStateFlow<LocalDate?>(null)
    val endDateFlow: StateFlow<LocalDate?> = _endDate.asStateFlow()

    fun setEndDate(date: LocalDate?) {
        _endDate.value = date
        if (date != null) {
            _selectedPreset.value = AnalysisPeriodPreset.CUSTOM
        }
    }

    fun clearTimeRange() {
        _startDate.value = null
        _endDate.value = null
        _selectedPreset.value = AnalysisPeriodPreset.ALL_TIME
    }

    private data class RoaLookupKey(
        val substanceName: String,
        val route: AdministrationRoute,
        val releaseForm: ReleaseForm?
    )

    private data class AnalysisFilters(
        val selectedSubstances: Set<String>,
        val consumer: ConsumerSelection,
        val preset: AnalysisPeriodPreset,
        val start: LocalDate?,
        val end: LocalDate?
    )

    private val filtersFlow: Flow<AnalysisFilters> = combine(
        _selectedSubstances,
        _selectedConsumer,
        _selectedPreset,
        _startDate,
        _endDate
    ) { selected, consumer, preset, start, end ->
        AnalysisFilters(selected, consumer, preset, start, end)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val modelFlow: StateFlow<StatsAnalysisModel> = combine(
        ingestionInvalidationsFlow,
        filtersFlow
    ) { _, filters -> filters }
        .mapLatest { filters ->
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now()
            val (effectiveStart, effectiveEnd) = when (filters.preset) {
                AnalysisPeriodPreset.ALL_TIME -> null to null
                AnalysisPeriodPreset.LAST_30_DAYS -> trailingCalendarDateRange(today, 30)
                AnalysisPeriodPreset.LAST_90_DAYS -> trailingCalendarDateRange(today, 90)
                AnalysisPeriodPreset.THIS_YEAR -> LocalDate.of(today.year, 1, 1) to today
                AnalysisPeriodPreset.LAST_YEAR ->
                    LocalDate.of(today.year - 1, 1, 1) to LocalDate.of(today.year - 1, 12, 31)
                AnalysisPeriodPreset.CUSTOM -> filters.start to filters.end
            }
            val roaDoseCache = mutableMapOf<RoaLookupKey, RoaDose?>()
            fun cachedRoaDose(ingestionWith: IngestionWithCompanionAndCustomUnit): RoaDose? {
                val ingestion = ingestionWith.ingestion
                val key = RoaLookupKey(
                    ingestion.substanceName,
                    ingestion.administrationRoute,
                    ingestion.releaseForm
                )
                if (roaDoseCache.containsKey(key)) return roaDoseCache[key]
                val roaDose = substanceRepo.getSubstance(ingestion.substanceName)
                    ?.getRoa(ingestion.administrationRoute, ingestion.releaseForm)?.roaDose
                roaDoseCache[key] = roaDose
                return roaDose
            }

            val accumulator = StatsAnalysisAccumulator(
                selectedSubstances = filters.selectedSubstances,
                selectedConsumer = filters.consumer,
                selectedPreset = filters.preset,
                startDate = filters.start,
                endDate = filters.end,
                effectiveStart = effectiveStart,
                effectiveEnd = effectiveEnd,
                zone = zone,
                relativeDoseOf = { item ->
                    relativeDoseOfIngestion(item, cachedRoaDose(item))
                },
                doseClassOf = { item ->
                    cachedRoaDose(item)?.getDoseClass(item.pureDose, item.originalUnit)
                }
            )
            experienceRepository.forEachIngestionAnalysisPage(action = accumulator::addPage)
            accumulator.build()
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            initialValue = StatsAnalysisModel(
                selectedSubstances = emptySet(),
                selectedConsumer = ConsumerSelection.All,
                selectedPreset = AnalysisPeriodPreset.ALL_TIME,
                startDate = null,
                endDate = null,
                effectiveStart = null,
                effectiveEnd = null,
                ingestionCount = 0,
                totalDoseBySubstance = emptyList(),
                perSubstanceCharts = emptyList(),
                ingestions = emptyList()
            ),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
}
