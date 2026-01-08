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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.data.substances.classes.Category
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.localization.i18nOrDefault
import com.isaakhanimann.journal.ui.tabs.stats.EmptyScreenDisclaimer
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import com.isaakhanimann.journal.ui.utils.categoryNameKey

@Composable
fun CategoryScreen(
    navigateToURL: (url: String) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    CategoryScreen(category = viewModel.category, navigateToURL = navigateToURL)
}

@Preview
@Composable
fun CategoryPreview() {
    CategoryScreen(
        category = Category(
            name = "psychedelic",
            description = "Psychedelics are drugs which alter the perception, causing a number of mental effects which manifest in many forms including altered states of consciousness, visual or tactile effects.",
            url = "https://psychonautwiki.org/wiki/Psychedelics",
            color = Color.Red
        ),
        navigateToURL = {}
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(category: Category?, navigateToURL: (url: String) -> Unit) {
    if (category == null) {
        EmptyScreenDisclaimer(
            title = i18n("category_not_found"),
            description = i18n("category_error")
        )
    } else {
        Scaffold(
            topBar = {
                val displayName = i18nOrDefault(
                    key = categoryNameKey(category.name),
                    fallback = category.name.replaceFirstChar { it.uppercase() }
                )
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
                        text = { Text(i18n("category_more_info")) },
                    )
                }
            }
        ) { padding ->
            Text(
                text = category.description,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = horizontalPadding, vertical = 10.dp)
            )
        }
    }
}
