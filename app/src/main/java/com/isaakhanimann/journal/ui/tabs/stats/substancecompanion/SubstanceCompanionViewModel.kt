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

package com.isaakhanimann.journal.ui.tabs.stats.substancecompanion

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.room.experiences.entities.Experience
import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceCompanionRoute
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.search.suggestion.models.CustomUnitDose
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.toReadableString
import com.isaakhanimann.journal.ui.utils.getTimeDifferenceText
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel(assistedFactory = SubstanceCompanionViewModel.Factory::class)
class SubstanceCompanionViewModel @AssistedInject constructor(
    experienceRepo: ExperienceRepository,
    val substanceRepo: SubstanceRepository,
    @Assisted val route: SubstanceCompanionRoute
) : ViewModel() {

    private val currentTimeFlow: Flow<Instant> = flow {
        while (true) {
            emit(Instant.now())
            delay(timeMillis = 1000 * 10)
        }
    }

    private data class IngestionBurstSource(
        val experience: Experience,
        val ingestions: List<IngestionsBurst.IngestionAndCustomUnit>,
        val start: Instant,
        val end: Instant
    )

    private val substanceName = route.substanceName
    val consumerName = route.consumerName

    private val substance = substanceRepo.getSubstance(substanceName)
    val tolerance = substance?.tolerance
    val crossTolerances = substance?.crossTolerances ?: emptyList()

    val thisCompanionFlow: StateFlow<SubstanceCompanion?> =
        experienceRepo.getSubstanceCompanionFlow(substanceName).stateIn(
            initialValue = null,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    private val ingestionBurstSourceFlow: Flow<List<IngestionBurstSource>> =
        experienceRepo.getSortedIngestionsWithExperienceAndCustomUnitFlow(substanceName)
            .map { list ->
                list.filter { it.ingestion.consumerName == consumerName }
                    .groupBy { it.ingestion.experienceId }
                    .mapNotNull { (_, oneExperience) ->
                        val experience = oneExperience.firstOrNull()?.experience ?: return@mapNotNull null
                        val ingestionsSorted = oneExperience.map {
                            IngestionsBurst.IngestionAndCustomUnit(
                                ingestion = it.ingestion,
                                customUnit = it.customUnit
                            )
                        }.sortedBy { it.ingestion.time }
                        IngestionBurstSource(
                            experience = experience,
                            ingestions = ingestionsSorted,
                            start = ingestionsSorted.first().ingestion.time,
                            end = ingestionsSorted.last().ingestion.time
                        )
                    }
            }

    val ingestionBurstsFlow: StateFlow<List<IngestionsBurst>> =
        ingestionBurstSourceFlow
            .combine(currentTimeFlow) { experienceGroups, currentTime ->
                var lastDate = currentTime
                experienceGroups.map { group ->
                    val diffText = getTimeDifferenceText(
                        fromInstant = group.end,
                        toInstant = lastDate
                    )
                    lastDate = group.start
                    IngestionsBurst(
                        timeUntil = diffText,
                        experience = group.experience,
                        ingestions = group.ingestions
                    )
                }
            }.stateIn(
                initialValue = emptyList(),
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000)
            )

    @AssistedFactory
    interface Factory {
        fun create(route: SubstanceCompanionRoute): SubstanceCompanionViewModel
    }
}

data class IngestionsBurst(
    val timeUntil: String,
    val experience: Experience,
    val ingestions: List<IngestionAndCustomUnit>
) {
    data class IngestionAndCustomUnit(val ingestion: Ingestion, val customUnit: CustomUnit?) {
        val customUnitDose: CustomUnitDose?
            get() = ingestion.dose?.let { doseUnwrapped ->
                customUnit?.let { customUnitUnwrapped ->
                    CustomUnitDose(
                        dose = doseUnwrapped,
                        isEstimate = ingestion.isDoseAnEstimate,
                        estimatedDoseStandardDeviation = ingestion.estimatedDoseStandardDeviation,
                        customUnit = customUnitUnwrapped
                    )
                }
            }

        fun getDoseDescription(context: Context): String =
            customUnitDose?.doseDescription ?: getIngestionDoseDescription(context)

        private fun getIngestionDoseDescription(context: Context): String =
            ingestion.dose?.let { dose ->
                ingestion.estimatedDoseStandardDeviation?.let { estimatedDoseStandardDeviation ->
                    "${dose.toReadableString()}±${estimatedDoseStandardDeviation.toReadableString()} ${ingestion.units}"
                } ?: run {
                    val description = "${dose.toReadableString()} ${ingestion.units}"
                    if (ingestion.isDoseAnEstimate) {
                        "~$description"
                    } else {
                        description
                    }
                }
            } ?: com.isaakhanimann.journal.localization.I18n.translate(context, "dose_unknown")
    }
}
