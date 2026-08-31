/*
 * Copyright (c) 2023. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.tabs.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.localization.i18nOrDefault
import com.isaakhanimann.journal.ui.main.bottomBarNestedScroll
import com.isaakhanimann.journal.ui.main.bottomBarOverlayDp
import com.isaakhanimann.journal.ui.main.bottomBarOverlayPadding
import com.isaakhanimann.journal.ui.tabs.search.substancerow.SubstanceRow
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import com.isaakhanimann.journal.ui.utils.categoryNameKey

@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = hiltViewModel(),
    onSubstanceTap: (substanceModel: SubstanceModel) -> Unit,
    onCustomSubstanceTap: (customSubstanceId: Int) -> Unit,
    navigateToAddCustomSubstanceScreen: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.bottomBarNestedScroll(),
        contentWindowInsets = WindowInsets.statusBars,
        floatingActionButton = {
            if (!isFocused) {
                FloatingActionButton(
                    modifier = Modifier.padding(bottom = bottomBarOverlayDp()),
                    onClick = { focusRequester.requestFocus() }
                ) {
                    Icon(Icons.Default.Keyboard, contentDescription = i18n("search_keyboard"))
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SearchField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState -> isFocused = focusState.isFocused },
                searchText = searchViewModel.searchTextFlow.collectAsState().value,
                onChange = { searchViewModel.filterSubstances(searchText = it) },
                categories = searchViewModel.chipCategoriesFlow.collectAsState().value,
                onFilterTapped = searchViewModel::onFilterTapped,
                isShowingFilter = true
            )

            val activeFilters = searchViewModel.chipCategoriesFlow.collectAsState().value.filter {
                it.isActive
            }
            val onFilterTapped = searchViewModel::onFilterTapped
            val filteredSubstances = searchViewModel.filteredSubstancesFlow.collectAsState().value
            val filteredCustomSubstances = searchViewModel.filteredCustomSubstancesFlow.collectAsState().value
            val customColor = searchViewModel.customColor


            if (activeFilters.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    item { Spacer(modifier = Modifier.width(4.dp)) }
                    items(activeFilters.size) { index ->
                        val categoryChipModel = activeFilters[index]
                        CategoryChipDelete(categoryChipModel = categoryChipModel) {
                            onFilterTapped(categoryChipModel.rawName)
                        }
                    }
                    item { Spacer(modifier = Modifier.width(4.dp)) }
                }
            }

            if (filteredSubstances.isEmpty() && filteredCustomSubstances.isEmpty()) {
                EmptySearchState(
                    activeFilters = activeFilters,
                    navigateToAddCustomSubstanceScreen = navigateToAddCustomSubstanceScreen
                )
            } else {
                LazyColumn(contentPadding = bottomBarOverlayPadding()) {
                    items(filteredCustomSubstances) { customSubstance ->
                        SubstanceRow(
                            substanceModel = SubstanceModel(
                                name = customSubstance.name,
                                displayName = customSubstance.name,
                                commonNames = emptyList(),
                                categories = listOf(
                                    CategoryModel(rawName = "custom", color = customColor)
                                ),
                                hasSaferUse = false,
                                hasInteractions = false
                            ),
                            onTap = { onCustomSubstanceTap(customSubstance.id) }
                        )
                        HorizontalDivider()
                    }

                    items(filteredSubstances) { substance ->
                        SubstanceRow(substanceModel = substance, onTap = {
                            onSubstanceTap(substance)
                        })
                        HorizontalDivider()
                    }

                    item { AddCustomSubstanceButton(navigateToAddCustomSubstanceScreen) }
                }
            }
        }
    }
}

@Composable
private fun EmptySearchState(
    activeFilters: List<CategoryChipModel>,
    navigateToAddCustomSubstanceScreen: () -> Unit
) {
    Column {
        val activeCategoryNames = activeFilters.filter { it.isActive }.map { chip ->
            if (chip.rawName == "custom") {
                i18n("search_custom")
            } else {
                i18nOrDefault(categoryNameKey(chip.rawName), chip.rawName)
            }
        }
        when (activeCategoryNames.size) {
            0 -> Text(i18n("search_no_match"), modifier = Modifier.padding(10.dp))
            1 -> Text(
                i18n(
                    key = "search_no_match_single_tag",
                    replacements = mapOf("tag" to activeCategoryNames[0])
                ),
                modifier = Modifier.padding(10.dp)
            )

            else -> {
                val names = activeCategoryNames.joinToString(separator = "', '")
                Text(
                    i18n(
                        key = "search_no_match_multiple_tags",
                        replacements = mapOf("tags" to names)
                    ),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        AddCustomSubstanceButton(navigateToAddCustomSubstanceScreen)
    }
}

@Composable
private fun AddCustomSubstanceButton(navigateToAddCustomSubstanceScreen: () -> Unit) {
    TextButton(
        onClick = navigateToAddCustomSubstanceScreen,
        modifier = Modifier.padding(horizontal = horizontalPadding)
    ) {
        Icon(
            Icons.Outlined.Add,
            contentDescription = i18n("common_add")
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = i18n("search_add_custom_substance"))
    }
}
