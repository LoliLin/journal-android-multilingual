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

package com.isaakhanimann.journal.ui.tabs.journal

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.achievement.AchievementEventBus
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.substances.repositories.SearchRepository
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class JournalViewModel @Inject constructor(

    val experienceRepo: ExperienceRepository,

    val searchRepository: SearchRepository,

    val substanceRepository: SubstanceRepository,
    private val userPreferences: UserPreferences

) : ViewModel() {

    val achievementsFlow = userPreferences.achievementsFlow.stateIn(
        initialValue = emptyList(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun addAchievement(achievement: String) {
        viewModelScope.launch {
            userPreferences.addAchievement(achievement)
            AchievementEventBus.send(achievement)
        }
    }

    private val totalDoseFlowCache = mutableMapOf<String, Flow<Double>>()

    fun getTotalDoseFlow(substanceName: String): Flow<Double> =
        totalDoseFlowCache.getOrPut(substanceName) {
            experienceRepo.getSortedExperienceWithIngestionsCompanionsAndRatingsFlow()
                .map { experiences ->
                    experiences.flatMap { it.ingestionsWithCompanions }
                        .filter { it.ingestion.substanceName == substanceName }
                        .sumOf { it.ingestion.dose ?: 0.0 }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = 0.0
                )
        }

    val pregabalinTotalDoseFlow = getTotalDoseFlow("Pregabalin")

    val amantadineDoseFlow = getTotalDoseFlow("Amantadine")

    val isTimeRelativeToNow = mutableStateOf(false)

    fun onChangeRelative(isRelative: Boolean) {
        isTimeRelativeToNow.value = isRelative
    }
    val isSearchEnabled = mutableStateOf(false)

    val ownerUserNameFlow = userPreferences.ownerUserNameFlow.stateIn(
        initialValue = "You",
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun onChangeOfIsSearchEnabled(newValue: Boolean) {
        if (newValue) {
            isSearchEnabled.value = true
        } else {
            isSearchEnabled.value = false
            viewModelScope.launch {
                searchTextFlow.emit("")
            }
        }
    }

    val isFavoriteEnabledFlow = MutableStateFlow(false)

    fun onChangeFavorite(isFavorite: Boolean) {
        viewModelScope.launch {
            isFavoriteEnabledFlow.emit(isFavorite)
        }
    }

    val searchTextFlow = MutableStateFlow("")

    fun search(newSearchText: String) {
        viewModelScope.launch {
            searchTextFlow.emit(newSearchText)
        }
    }

    val experiences =
        experienceRepo.getSortedExperienceWithIngestionsCompanionsAndRatingsFlow()
            .combine(searchTextFlow) { experiencesWithIngestions, searchText ->
                Pair(first = experiencesWithIngestions, second = searchText)
            }
            .combine(isFavoriteEnabledFlow) { pair, isFavoriteEnabled ->
                val experiencesWithIngestions = pair.first
                val searchText = pair.second
                val filtered = if (searchText.isEmpty() && !isFavoriteEnabled) {
                    experiencesWithIngestions
                } else {
                    val matchingSubstances = searchRepository.getMatchingSubstances(
                        searchText = searchText,
                        filterCategories = emptyList(),
                        recentlyUsedSubstanceNamesSorted = emptyList()
                    )
                    if (isFavoriteEnabled) {
                        experiencesWithIngestions.filter {
                            it.experience.isFavorite &&
                                (
                                    it.experience.title.contains(
                                        other = searchText,
                                        ignoreCase = true
                                    ) ||
                                        it.ingestionsWithCompanions.any { ingestionWithCompanion ->
                                            matchingSubstances.any { sub ->
                                                sub.substance.name ==
                                                    ingestionWithCompanion.substanceCompanion?.substanceName
                                            }
                                        }
                                    )
                        }
                    } else {
                        experiencesWithIngestions.filter {
                            it.experience.title.contains(
                                other = searchText,
                                ignoreCase = true
                            ) ||
                                it.ingestionsWithCompanions.any { ingestionWithCompanion ->
                                    matchingSubstances.any { sub ->
                                        sub.substance.name ==
                                            ingestionWithCompanion.substanceCompanion?.substanceName
                                    }
                                }
                        }
                    }
                }
                return@combine filtered
            }
            .stateIn(
                initialValue = emptyList(),
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000)
            )
}
