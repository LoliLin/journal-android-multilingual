/*
 * Copyright (c) 2024. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.tabs.settings.customunits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.toReadableString
import com.isaakhanimann.journal.ui.tabs.stats.EmptyScreenDisclaimer
import com.isaakhanimann.journal.ui.theme.horizontalPadding

@Composable
fun CustomUnitsScreen(
    viewModel: CustomUnitsViewModel = hiltViewModel(),
    navigateToEditCustomUnit: (customUnitId: Int) -> Unit,
    navigateToAddCustomUnit: () -> Unit,
    navigateToCustomUnitArchive: () -> Unit
) {
    CustomUnitsScreenContent(
        filteredUnits = viewModel.filteredCustomUnitsFlow.collectAsState().value,
        navigateToEditCustomUnit = navigateToEditCustomUnit,
        navigateToAddCustomUnit = navigateToAddCustomUnit,
        navigateToCustomUnitArchive = navigateToCustomUnitArchive,
        searchText = viewModel.searchTextFlow.collectAsState().value,
        onSearch = viewModel::onSearch,
        getSubstanceDisplayName = viewModel.substanceRepository::getDisplayName
    )
}

@Preview
@Composable
fun CustomUnitsScreenPreview() {
    CustomUnitsScreenContent(
        filteredUnits = listOf(
            CustomUnit.mdmaSample,
            CustomUnit.twoCBSample
        ),
        navigateToEditCustomUnit = { _ -> },
        navigateToAddCustomUnit = {},
        navigateToCustomUnitArchive = {},
        searchText = "",
        onSearch = {},
        getSubstanceDisplayName = { it }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomUnitsScreenContent(
    filteredUnits: List<CustomUnit>,
    navigateToEditCustomUnit: (customUnitId: Int) -> Unit,
    navigateToAddCustomUnit: () -> Unit,
    navigateToCustomUnitArchive: () -> Unit,
    searchText: String,
    onSearch: (String) -> Unit,
    getSubstanceDisplayName: (String) -> String,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(i18n("settings_custom_units")) },
                actions = {
                    IconButton(onClick = navigateToCustomUnitArchive) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = i18n("custom_units_go_to_archive")
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.imePadding(),
                onClick = navigateToAddCustomUnit,
                icon = {
                    Icon(Icons.Default.Add, contentDescription = i18n("custom_units_add"))
                },
                text = {
                    Text(text = i18n("custom_units_button"))
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            val focusManager = LocalFocusManager.current
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchText,
                onValueChange = { value ->
                    onSearch(value)
                },
                placeholder = { Text(text = i18n("common_search")) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = i18n("common_search"),
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // clear search button
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = {
                                onSearch("")
                            }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = i18n("common_close"),
                                )
                            }
                        }
                    }
                },
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                keyboardOptions = KeyboardOptions.Default.copy(
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Words,
                ),
                singleLine = true
            )
            if (filteredUnits.isEmpty()) {
                if(searchText.isEmpty()) {
                    EmptyScreenDisclaimer(
                        title = i18n("custom_units_empty_title"),
                        description = i18n("custom_units_empty_desc")
                    )
                } else {
                    EmptyScreenDisclaimer(
                        title = i18n("custom_units_empty_search_title"),
                        description = i18n("custom_units_empty_search_desc")
                    )
                }
            } else {
                LazyColumn {
                    items(filteredUnits) { customUnit ->
                        CustomUnitRow(
                            customUnit = customUnit,
                            navigateToEditCustomUnit = navigateToEditCustomUnit,
                            getSubstanceDisplayName = getSubstanceDisplayName
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun CustomUnitRow(
    customUnit: CustomUnit,
    navigateToEditCustomUnit: (customUnitId: Int) -> Unit,
    getSubstanceDisplayName: (String) -> String
) {
    Column(
        modifier = Modifier
            .clickable {
                navigateToEditCustomUnit(customUnit.id)
            }
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = horizontalPadding),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "${getSubstanceDisplayName(customUnit.substanceName)}, ${customUnit.name}",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = i18n("custom_units_dose_per_unit", mapOf("dose" to customUnit.getDoseOfOneUnitDescription(), "unit" to customUnit.unit)),
            style = MaterialTheme.typography.titleSmall
        )
        if (customUnit.note.isNotBlank()) {
            Text(
                text = customUnit.note,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

