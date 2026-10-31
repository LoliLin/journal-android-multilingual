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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.achievement.AchievementDefinition
import com.isaakhanimann.journal.data.achievement.AchievementDefinitionsLoader
import com.isaakhanimann.journal.data.achievement.AchievementEventBus
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsCompanionsAndRatings
import com.isaakhanimann.journal.data.substances.repositories.SearchRepository
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val IS_MIGRATED_0 = booleanPreferencesKey("is_migrated_0")
val IS_MIGRATED_1 = booleanPreferencesKey("is_migrated_1")

@HiltViewModel
class JournalViewModel @Inject constructor(
    val experienceRepo: ExperienceRepository,
    val searchRepository: SearchRepository,
    val substanceRepository: SubstanceRepository,
    private val dataStore: DataStore<Preferences>,
    private val userPreferences: UserPreferences,
    achievementDefinitionsLoader: AchievementDefinitionsLoader
) : ViewModel() {

    val achievementDefinitions: List<AchievementDefinition> =
        achievementDefinitionsLoader.definitions

    val achievementsFlow: StateFlow<List<String>?> = userPreferences.achievementsFlow.stateIn(
        initialValue = null,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun addAchievement(achievement: String) {
        viewModelScope.launch {
            val added = userPreferences.addAchievement(achievement)
            if (added) {
                AchievementEventBus.send(achievement)
            }
        }
    }

    val ingestionsFlow = experienceRepo.getSortedIngestionsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val isJournalActionsInTopBarFlow = userPreferences.isJournalActionsInTopBarFlow.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val isTimeRelativeToNow = mutableStateOf(false)

    fun onChangeRelative(isRelative: Boolean) {
        isTimeRelativeToNow.value = isRelative
    }

    val isSearchEnabled = mutableStateOf(false)

    val ownerUserNameFlow: StateFlow<String?> = userPreferences.ownerUserNameFlow.stateIn(
        initialValue = null,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun resetAddIngestionTimes() = viewModelScope.launch {
        userPreferences.saveLastIngestionTimeOfExperience(null)
        userPreferences.saveClonedIngestionTime(null)
    }

    fun maybeMigrate() {
        viewModelScope.launch {
            dataStore.edit { settings ->
                if (settings[IS_MIGRATED_0] == null) {
                    experienceRepo.migrateBenzydamine()
                    settings[IS_MIGRATED_0] = true
                }
                if (settings[IS_MIGRATED_1] == null) {
                    experienceRepo.migrateCannabisAndMushroomUnits()
                    settings[IS_MIGRATED_1] = true
                }
            }
        }
    }

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
            .combine(experienceRepo.getCustomSubstancesFlow()) { pair, customSubstances ->
                Pair(first = pair, second = customSubstances)
            }
            .combine(isFavoriteEnabledFlow) { pair, isFavoriteEnabled ->
                var experiencesWithIngestions = pair.first.first
                val searchText = pair.first.second
                val customSubstances = pair.second

                if (isFavoriteEnabled) {
                    // only favorites
                    experiencesWithIngestions =
                        experiencesWithIngestions.filter { it.experience.isFavorite }
                }
                if (searchText.isNotEmpty()) {
                    // Split into whitespace-separated terms so several substances can be
                    // combined in one query. Each term is matched independently and an
                    // experience has to satisfy ALL of them, so "LSD MDMA" means
                    // "experiences that contain both", not "either".
                    val searchTerms = splitSearchTerms(searchText)
                    // Resolve the substantances that each term refers to once, so the
                    // per-experience filter below stays a cheap set lookup.
                    val substanceNamesByTerm: List<Set<String>> = searchTerms.map { term ->
                        buildSet {
                            val matches = searchRepository.getMatchingSubstances(
                                searchText = term,
                                filterCategories = emptyList(),
                                recentlyUsedSubstanceNamesSorted = emptyList()
                            )
                            for (match in matches) {
                                add(match.substance.name)
                            }
                            for (custom in customSubstances) {
                                if (custom.name.contains(term, ignoreCase = true)) {
                                    add(custom.name)
                                }
                            }
                        }
                    }

                    experiencesWithIngestions = experiencesWithIngestions.filter { it.matchesAllTerms(
                        searchTerms = searchTerms,
                        substanceNamesByTerm = substanceNamesByTerm
                    ) }
                }
                return@combine experiencesWithIngestions
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                initialValue = emptyList(),
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000)
            )
}

private val WHITESPACE_REGEX = Regex("\\s+")

/**
 * Splits a search query into individual terms so multiple substances can be searched at once
 * (e.g. "LSD MDMA" matches experiences containing both substances).
 */
internal fun splitSearchTerms(searchText: String): List<String> =
    searchText.trim().split(WHITESPACE_REGEX).filter { it.isNotBlank() }

/**
 * True when this experience satisfies every search term.
 *
 * A term is satisfied by a substance the experience consumed (resolved through
 * [substanceNamesByTerm] so fuzzy/pinyin matching still applies), or by free text: the
 * experience title/text, the consumer name, an ingestion note or a timed note.
 *
 * Terms are combined with AND on purpose — searching "LSD MDMA" asks for the experiences
 * where both were involved.
 */
internal fun ExperienceWithIngestionsCompanionsAndRatings.matchesAllTerms(
    searchTerms: List<String>,
    substanceNamesByTerm: List<Set<String>>
): Boolean {
    if (searchTerms.isEmpty()) return true
    // Defensive: a caller that builds the two lists out of step would otherwise index past the
    // end. Treat a missing entry as "no substance resolved for this term" instead of crashing.
    val resolvedNames = searchTerms.indices.map { index ->
        substanceNamesByTerm.getOrElse(index) { emptySet() }
    }
    val consumedSubstanceNames = ingestionsWithCompanions
        .map { it.ingestion.substanceName }
        .toSet()
    return searchTerms.withIndex().all { (index, term) ->
        val termSubstanceNames = resolvedNames[index]
        consumedSubstanceNames.any { it in termSubstanceNames } ||
            experience.title.contains(term, ignoreCase = true) ||
            experience.text.contains(term, ignoreCase = true) ||
            ingestionsWithCompanions.any { ingestionWithCompanion ->
                ingestionWithCompanion.ingestion.consumerName
                    ?.contains(term, ignoreCase = true) == true ||
                    ingestionWithCompanion.ingestion.notes
                        ?.contains(term, ignoreCase = true) == true
            } ||
            timedNotes.any { timedNote -> timedNote.note.contains(term, ignoreCase = true) }
    }
}
