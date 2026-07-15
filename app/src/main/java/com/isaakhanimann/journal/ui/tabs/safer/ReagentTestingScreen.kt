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

package com.isaakhanimann.journal.ui.tabs.safer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.tabs.search.substance.SectionText
import com.isaakhanimann.journal.ui.tabs.search.substance.VerticalSpace
import com.isaakhanimann.journal.ui.theme.horizontalPadding

@Preview
@Composable
fun ReagentTestingPreview() {
    ReagentTestingScreen(navigateToReagentTestingArticle = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReagentTestingScreen(navigateToReagentTestingArticle: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(i18n("safer_reagent_testing")) })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = navigateToReagentTestingArticle,
                icon = {
                    Icon(
                        Icons.Outlined.Newspaper,
                        contentDescription = i18n("common_open_link")
                    )
                },
                text = { Text(i18n("common_article")) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = horizontalPadding)
        ) {
            ElevatedCard(modifier = Modifier.padding(vertical = 3.dp)) {
                Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    SectionText(
                        text = i18n("reagent_testing_body")
                    )
                    VerticalSpace()
                }
            }
            ElevatedCard(modifier = Modifier.padding(vertical = 5.dp)) {
                Column(
                    Modifier.padding(vertical = 5.dp)
                ) {
                    Text(
                        text = i18n("kit_sellers"),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    HorizontalDivider()
                    val uriHandler = LocalUriHandler.current
                    TextButton(onClick = {
                        uriHandler.openUri("https://dancesafe.org/testing-kit-instructions/")
                    }) {
                        Text(text = "DanceSafe")
                    }
                    HorizontalDivider()
                    TextButton(onClick = { uriHandler.openUri("https://bunkpolice.com") }) {
                        Text(text = "Bunk Police")
                    }
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}
