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

package com.isaakhanimann.journal.ui.tabs.search.substance.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.substances.repositories.SearchRepository
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.main.navigation.routes.CategoryRoute
import com.isaakhanimann.journal.ui.tabs.search.SubstanceModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CategoryViewModel.Factory::class)
class CategoryViewModel @AssistedInject constructor(
    substanceRepo: SubstanceRepository,
    @Assisted val route: CategoryRoute,
    private val searchRepository: SearchRepository
) : ViewModel() {
    private val categoryName = route.categoryName
    val category = substanceRepo.getCategory(categoryName)

    val isSearchEnabledFlow = MutableStateFlow(false)

    fun toggleIsSearchEnabled() {
        viewModelScope.launch {
            val isEnabled = isSearchEnabledFlow.value.not()
            isSearchEnabledFlow.emit(isEnabled)
            if (!isEnabled) {
                searchTextFlow.emit("")
            }
        }
    }

    val searchTextFlow = MutableStateFlow("")

    fun search(newSearchText: String) {
        viewModelScope.launch {
            searchTextFlow.emit(newSearchText)
        }
    }

    val substanceModelsFlow: StateFlow<List<SubstanceModel>> = searchTextFlow
        .map { query ->
            val trimmed = query.trim()
            val matching = if (trimmed.isEmpty()) {
                searchRepository.getSubstancesMatchingCategories(listOf(categoryName))
            } else {
                searchRepository.getMatchingSubstances(
                    searchText = trimmed,
                    filterCategories = listOf(categoryName),
                    recentlyUsedSubstanceNamesSorted = emptyList()
                )
            }
            matching.map { it.toSubstanceModel() }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = searchRepository.getSubstancesMatchingCategories(listOf(categoryName))
                .map { it.toSubstanceModel() }
        )

    val substanceModels: List<SubstanceModel>
        get() = substanceModelsFlow.value

    @AssistedFactory
    interface Factory {
        fun create(route: CategoryRoute): CategoryViewModel
    }
}
