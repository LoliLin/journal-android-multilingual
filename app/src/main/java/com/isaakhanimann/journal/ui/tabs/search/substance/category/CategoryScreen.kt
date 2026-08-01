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

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.isaakhanimann.journal.data.substances.classes.Category
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.tabs.stats.EmptyScreenDisclaimer
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import com.isaakhanimann.journal.ui.tabs.search.SubstanceModel
import com.isaakhanimann.journal.ui.tabs.search.substancerow.SubstanceRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun CategoryScreen(
    navigateToURL: (url: String) -> Unit,
    onSubstanceTap: (substanceModel: SubstanceModel) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    CategoryScreen(
        category = viewModel.category, 
        navigateToURL = navigateToURL,
        onSubstanceTap = onSubstanceTap,
        substanceModels = viewModel.substanceModels
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    category: Category?, 
    navigateToURL: (url: String) -> Unit,
    substanceModels: List<SubstanceModel>,
    onSubstanceTap: (substanceModel: SubstanceModel) -> Unit
) {
    if (category == null) {
        EmptyScreenDisclaimer(
            title = i18n("category_not_found"),
            description = i18n("category_error")
        )
    } else {
        Scaffold(
            topBar = {
                val context = androidx.compose.ui.platform.LocalContext.current

                val displayName = category.getLocalizedName(context)

                TopAppBar(title = { Text(displayName) })
            },
            floatingActionButton = {
                if (category.url != null) {
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
            LazyColumn(modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = horizontalPadding, vertical = 10.dp)
            ){
                item{
                    Text(
                        text = category.getLocalizedDescription(
                            androidx.compose.ui.platform.LocalContext.current
                        ),
                        textAlign = TextAlign.Left,
                    )
                }
                item{
                    HorizontalDivider()
                }
                items(substanceModels) { substance ->
                    SubstanceRow(substanceModel = substance, onTap = {
                        onSubstanceTap(substance)
                    })
                    HorizontalDivider()
                }
            }
        }
    }
}
