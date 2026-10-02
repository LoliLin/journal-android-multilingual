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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.data.substances.classes.Category
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.tabs.search.SubstanceModel
import com.isaakhanimann.journal.ui.tabs.search.substancerow.SubstanceRow
import com.isaakhanimann.journal.ui.tabs.stats.EmptyScreenDisclaimer
import com.isaakhanimann.journal.ui.theme.horizontalPadding

@Composable
fun CategoryScreen(
    navigateToURL: (url: String) -> Unit,
    onSubstanceTap: (substanceModel: SubstanceModel) -> Unit,
    viewModel: CategoryViewModel
) {
    val substanceModels by viewModel.substanceModelsFlow.collectAsState()
    val isSearchEnabled by viewModel.isSearchEnabledFlow.collectAsState()
    val searchText by viewModel.searchTextFlow.collectAsState()

    CategoryScreen(
        category = viewModel.category,
        navigateToURL = navigateToURL,
        onSubstanceTap = onSubstanceTap,
        substanceModels = substanceModels,
        isSearchEnabled = isSearchEnabled,
        onChangeIsSearchEnabled = { viewModel.toggleIsSearchEnabled() },
        searchText = searchText,
        onChangeSearchText = viewModel::search
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    category: Category?,
    navigateToURL: (url: String) -> Unit,
    substanceModels: List<SubstanceModel>,
    onSubstanceTap: (substanceModel: SubstanceModel) -> Unit,
    isSearchEnabled: Boolean = false,
    onChangeIsSearchEnabled: (Boolean) -> Unit = {},
    searchText: String = "",
    onChangeSearchText: (String) -> Unit = {}
) {
    if (category == null) {
        EmptyScreenDisclaimer(
            title = i18n("category_not_found"),
            description = i18n("category_error")
        )
    } else {
        val context = LocalContext.current

        Scaffold(
            topBar = {
                val displayName = category.getLocalizedName(context)

                TopAppBar(
                    title = { Text(displayName) },
                    actions = {
                        IconToggleButton(
                            checked = isSearchEnabled,
                            onCheckedChange = onChangeIsSearchEnabled
                        ) {
                            if (isSearchEnabled) {
                                Icon(
                                    Icons.Outlined.SearchOff,
                                    contentDescription = i18n("journal_search_off")
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = i18n("common_search")
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                if (!isSearchEnabled && category.url != null) {
                    ExtendedFloatingActionButton(
                        onClick = { navigateToURL(category.url) },
                        icon = {
                            Icon(
                                Icons.Outlined.Newspaper,
                                contentDescription = i18n("category_open_link")
                            )
                        },
                        text = { Text(i18n("category_more_info")) }
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Top
            ) {
                AnimatedVisibility(visible = isSearchEnabled) {
                    Column {
                        val focusManager = LocalFocusManager.current
                        TextField(
                            value = searchText,
                            onValueChange = onChangeSearchText,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = i18n("common_search")
                                )
                            },
                            trailingIcon = {
                                if (searchText.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            onChangeSearchText("")
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = i18n("common_close")
                                        )
                                    }
                                }
                            },
                            placeholder = { Text(text = i18n("search_substances_placeholder")) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                            }),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.None
                            ),
                            singleLine = true
                        )
                        if (substanceModels.isEmpty() && searchText.trim().isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding, vertical = 16.dp)
                            ) {
                                Text(
                                    text = i18n("journal_no_results"),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 10.dp)
                ) {
                    if (!isSearchEnabled || searchText.trim().isEmpty()) {
                        item(key = "category_description") {
                            Text(
                                text = category.getLocalizedDescription(context),
                                textAlign = TextAlign.Left
                            )
                        }
                        item(key = "description_divider") {
                            HorizontalDivider()
                        }
                    }
                    items(substanceModels, key = { it.name }) { substance ->
                        SubstanceRow(substanceModel = substance, onTap = {
                            onSubstanceTap(substance)
                        })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
