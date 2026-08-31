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

package com.isaakhanimann.journal.ui.tabs.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsAndCompanions
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithCompanionAndCustomUnit
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class StatsViewModel @Inject constructor(
    experienceRepo: ExperienceRepository,
    val substanceRepo: SubstanceRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _optionFlow = MutableStateFlow(TimePickerOption.WEEKS_26)
    private val optionFlow = _optionFlow.asStateFlow()

    private val consumerFlow = MutableStateFlow<String?>(null)

    fun onTapOption(timePickerOption: TimePickerOption) {
        viewModelScope.launch {
            _optionFlow.emit(timePickerOption)
        }
    }

    fun onChangeConsumer(consumerName: String?) {
        viewModelScope.launch {
            consumerFlow.emit(consumerName)
        }
    }

    private val startDateFlow = _optionFlow.map {
        return@map Instant.now().getEndOfDay().minus(it.allBucketSizes)
    }

    private val startDateTextFlow = startDateFlow.map {
        val dateTime = LocalDateTime.ofInstant(it, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")
        return@map dateTime.format(formatter)
    }

    private val allExperiencesSortedFlow: Flow<List<ExperienceWithIngestionsAndCompanions>> =
        experienceRepo.getSortedExperiencesWithIngestionsAndCustomUnitsFlow()

    private val areThereAnyIngestionsFlow =
        allExperiencesSortedFlow.combine(consumerFlow) { experiences, consumerName ->
            experiences.any { experience ->
                experience.ingestionsWithCompanionAndCustomUnit.any {
                    it.ingestion.consumerName ==
                        consumerName
                }
            }
        }


    private val companionFlow = experienceRepo.getAllSubstanceCompanionsFlow()

    private val isStatsByIngestionTimeFlow = userPreferences.isStatsByIngestionTimeFlow

    fun onChangeStatsByIngestionTime(value: Boolean) {
        viewModelScope.launch {
            userPreferences.saveStatsByIngestionTime(value)
        }
    }

    private val experienceChartBucketsFlow: Flow<List<List<ColorCount>>> =
        combine(
            combine(allExperiencesSortedFlow, optionFlow, startDateFlow) { experiences, option, startDate ->
                Triple(experiences, option, startDate)
            },
            combine(isStatsByIngestionTimeFlow, consumerFlow, companionFlow) { byIngestionTime, consumerName, companions ->
                Triple(byIngestionTime, consumerName, companions)
            }
        ) { chartInputs, filters ->
            val (experiences, option, startDate) = chartInputs
            val (byIngestionTime, consumerName, companions) = filters
            val nowEndOfDay = Instant.now().getEndOfDay()
            if (byIngestionTime) {
                val ingestionsNewestFirst = experiences
                    .asSequence()
                    .flatMap { it.ingestionsWithCompanionAndCustomUnit }
                    .filter { it.ingestion.consumerName == consumerName }
                    .sortedByDescending { it.ingestion.time }
                    .toList()
                val inWindow = takeItemsInStatsWindow(
                    itemsNewestFirst = ingestionsNewestFirst,
                    instantOf = { it.ingestion.time },
                    nowEndOfDay = nowEndOfDay,
                    startExclusive = startDate
                )
                bucketNewestFirst(
                    itemsNewestFirst = inWindow,
                    instantOf = { it.ingestion.time },
                    option = option,
                    nowEndOfDay = nowEndOfDay
                ).map { ingestionsInBucket ->
                    getColorCountsForIngestions(ingestionsInBucket, companions)
                }
            } else {
                val inWindow = takeItemsInStatsWindow(
                    itemsNewestFirst = experiences,
                    instantOf = { it.sortInstant },
                    nowEndOfDay = nowEndOfDay,
                    startExclusive = startDate
                )
                bucketNewestFirst(
                    itemsNewestFirst = inWindow,
                    instantOf = { it.sortInstant },
                    option = option,
                    nowEndOfDay = nowEndOfDay
                ).map { experiencesInBucket ->
                    getColorCountsForExperiences(experiencesInBucket, companions, consumerName)
                }
            }
        }

    private fun getColorCountsForExperiences(
        experiences: List<ExperienceWithIngestionsAndCompanions>,
        companions: List<SubstanceCompanion>,
        consumerName: String?
    ): List<ColorCount> = getColorCountsForIngestions(
        ingestions = experiences.flatMap { experience ->
            experience.ingestionsWithCompanionAndCustomUnit.filter {
                it.ingestion.consumerName == consumerName
            }
        },
        companions = companions
    )

    private fun getColorCountsForIngestions(
        ingestions: List<IngestionWithCompanionAndCustomUnit>,
        companions: List<SubstanceCompanion>
    ): List<ColorCount> {
        val companionBySubstance = companions.associateBy { it.substanceName }
        return ingestions.mapNotNull { ingestionWith ->
            val oneCompanion =
                companionBySubstance[ingestionWith.ingestion.substanceName]
                    ?: return@mapNotNull null
            oneCompanion.color to (
                relativeDoseOfIngestion(substanceRepo, ingestionWith) ?: 0.0
                )
        }.groupBy { it.first }
            .map { (color, dosePairs) ->
                ColorCount(
                    color = color,
                    count = dosePairs.sumOf { it.second }
                )
            }
            .sortedByDescending { it.count }
    }

    private val statsFlowItem: Flow<List<StatItem>> = combine(
        combine(allExperiencesSortedFlow, startDateFlow, isStatsByIngestionTimeFlow) { experiences, startDate, byIngestionTime ->
            Triple(experiences, startDate, byIngestionTime)
        },
        consumerFlow,
        companionFlow
    ) { window, consumerName, companions ->
        val (experiences, startDate, byIngestionTime) = window
        val nowEndOfDay = Instant.now().getEndOfDay()
        val relevantIngestions = if (byIngestionTime) {
            val ingestionsNewestFirst = experiences
                .asSequence()
                .flatMap { it.ingestionsWithCompanionAndCustomUnit }
                .filter { it.ingestion.consumerName == consumerName }
                .sortedByDescending { it.ingestion.time }
                .toList()
            takeItemsInStatsWindow(
                itemsNewestFirst = ingestionsNewestFirst,
                instantOf = { it.ingestion.time },
                nowEndOfDay = nowEndOfDay,
                startExclusive = startDate
            )
        } else {
            takeItemsInStatsWindow(
                itemsNewestFirst = experiences,
                instantOf = { it.sortInstant },
                nowEndOfDay = nowEndOfDay,
                startExclusive = startDate
            ).flatMap { experience ->
                experience.ingestionsWithCompanionAndCustomUnit.filter {
                    it.ingestion.consumerName == consumerName
                }
            }
        }
        val experienceCountsBySubstance = if (byIngestionTime) {
            relevantIngestions.groupBy { it.ingestion.substanceName }
                .mapValues { (_, list) -> list.map { it.ingestion.experienceId }.distinct().size }
        } else {
            relevantIngestions
                .groupBy { it.ingestion.experienceId }
                .values
                .flatMap { ingestionsInExperience ->
                    ingestionsInExperience.map { it.ingestion.substanceName }.toSet()
                }
                .groupingBy { it }
                .eachCount()
        }
        val map = relevantIngestions.groupBy { it.ingestion.substanceName }
        map.values.mapNotNull { groupedIngestions ->
            val name =
                groupedIngestions.firstOrNull()?.ingestion?.substanceName ?: return@mapNotNull null
            val oneCompanion =
                companions.firstOrNull { it.substanceName == name } ?: return@mapNotNull null
            val relativeValues = groupedIngestions.mapNotNull {
                relativeDoseOfIngestion(substanceRepo, it)
            }
            StatItem(
                substanceName = name,
                substanceRepo = substanceRepo,
                color = oneCompanion.color,
                experienceCount = experienceCountsBySubstance[name] ?: 0,
                ingestionCount = groupedIngestions.size,
                routeCounts = getRouteCounts(groupedIngestions.map { it.ingestion }),
                totalDose = getTotalDose(groupedIngestions),
                relativeTotalDose = relativeValues.sum().takeIf { relativeValues.isNotEmpty() }
            )
        }.sortedByDescending { it.experienceCount }
    }

    private fun getRouteCounts(groupedIngestions: List<Ingestion>): List<RouteCount> {
        val routeMap = groupedIngestions.groupBy { it.administrationRoute }
        return routeMap.values.mapNotNull {
            val route = it.firstOrNull()?.administrationRoute ?: return@mapNotNull null
            RouteCount(administrationRoute = route, count = it.size)
        }
    }

    private fun getTotalDose(
        groupedIngestions: List<IngestionWithCompanionAndCustomUnit>
    ): TotalDose? {
        val units = groupedIngestions.firstOrNull()?.originalUnit ?: return null
        if (groupedIngestions.any { it.originalUnit != units || it.pureDose == null }) return null
        val sumDose = groupedIngestions.sumOf { it.pureDose ?: 0.0 }
        val sumStandardDeviations = groupedIngestions.sumOf { it.pureDoseStandardDeviation ?: 0.0 }
        val isEstimate = groupedIngestions.any { it.isEstimate }
        return TotalDose(
            dose = sumDose,
            units = units,
            isEstimate = isEstimate,
            estimatedDoseStandardDeviation = sumStandardDeviations.takeIf { it > 0 }
        )
    }

    val statsModelFlow: StateFlow<StatsModel> =
        combine(
            combine(optionFlow, startDateTextFlow, statsFlowItem) { option, startDateText, substanceStats ->
                Triple(option, startDateText, substanceStats)
            },
            combine(areThereAnyIngestionsFlow, consumerFlow, experienceChartBucketsFlow) { areThere, consumerName, chartBuckets ->
                Triple(areThere, consumerName, chartBuckets)
            },
            isStatsByIngestionTimeFlow
        ) { summary, extras, byIngestionTime ->
            val (option, startDateText, substanceStats) = summary
            val (areThere, consumerName, chartBuckets) = extras
            StatsModel(
                areThereAnyIngestions = areThere,
                selectedOption = option,
                startDateText = startDateText,
                statItems = substanceStats,
                chartBuckets = chartBuckets,
                consumerName = consumerName,
                isByIngestionTime = byIngestionTime
            )
        }.stateIn(
            initialValue = StatsModel(
                selectedOption = TimePickerOption.WEEKS_26,
                areThereAnyIngestions = false,
                startDateText = "",
                statItems = emptyList(),
                chartBuckets = emptyList(),
                consumerName = null,
                isByIngestionTime = false
            ),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    val sortedConsumerNamesFlow =
        experienceRepo.getSortedIngestions(limit = 200).map { ingestions ->
            return@map ingestions.mapNotNull { it.consumerName }.distinct()
        }.stateIn(
            initialValue = emptyList(),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    val ownerUserNameFlow = userPreferences.ownerUserNameFlow.stateIn(
        initialValue = "You",
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )
}

data class StatsModel(
    val selectedOption: TimePickerOption,
    val areThereAnyIngestions: Boolean,
    val startDateText: String,
    val statItems: List<StatItem>,
    val chartBuckets: List<List<ColorCount>>,
    val consumerName: String?,
    val isByIngestionTime: Boolean = false
)

data class ColorCount(val color: AdaptiveColor, val count: Double)

data class StatItem(
    val substanceName: String,
    val substanceRepo: SubstanceRepository? = null,
    val color: AdaptiveColor,
    val experienceCount: Int,
    val ingestionCount: Int,
    val routeCounts: List<RouteCount>,
    val totalDose: TotalDose?,
    val relativeTotalDose: Double?
)

data class RouteCount(val administrationRoute: AdministrationRoute, val count: Int)

data class TotalDose(
    val dose: Double,
    val units: String,
    val isEstimate: Boolean,
    val estimatedDoseStandardDeviation: Double?
)

enum class TimePickerOption {
    DAYS_7 {
        override val displayText = "7D"
        override val longDisplayText = "Last week"
        override val tabIndex = 0
        override val bucketCount = 7
        override val oneBucketSize: Period = Period.ofDays(1)
        override val allBucketSizes: Period = Period.ofDays(7)
    },
    DAYS_30 {
        override val displayText = "30D"
        override val longDisplayText = "Last month"
        override val tabIndex = 1
        override val bucketCount = 30
        override val oneBucketSize: Period = Period.ofDays(1)
        override val allBucketSizes: Period = Period.ofDays(30)
    },
    WEEKS_26 {
        override val displayText = "26W"
        override val longDisplayText = "Half year"
        override val tabIndex = 2
        override val bucketCount = 26
        override val oneBucketSize: Period =
            Period.ofDays(7) // the max time unit that can be used for subtraction is days
        override val allBucketSizes: Period = Period.ofDays(7 * bucketCount)
    },
    MONTHS_12 {
        override val displayText = "12M"
        override val longDisplayText = "Last year"
        override val tabIndex = 3
        override val bucketCount = 12
        override val oneBucketSize: Period = Period.ofDays(30)
        override val allBucketSizes: Period = Period.ofDays(30 * bucketCount)
    },
    YEARS_10 {
        override val displayText = "10Y"
        override val longDisplayText = "10 years"
        override val tabIndex = 4
        override val bucketCount = 10
        override val oneBucketSize: Period = Period.ofDays(365)
        override val allBucketSizes: Period = Period.ofDays(365 * bucketCount)
    };

    abstract val displayText: String
    abstract val longDisplayText: String
    abstract val tabIndex: Int
    abstract val bucketCount: Int
    abstract val oneBucketSize: Period
    abstract val allBucketSizes: Period
}

fun Instant.getEndOfDay(): Instant = this.atOffset(ZoneOffset.UTC)
    .with(LocalTime.of(23, 59, 59, this.nano))
    .toInstant()
