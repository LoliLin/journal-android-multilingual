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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.localization.i18nOrDefault
import com.isaakhanimann.journal.ui.tabs.search.substance.SectionText
import com.isaakhanimann.journal.ui.tabs.search.substance.SectionWithTitle
import com.isaakhanimann.journal.ui.tabs.search.substance.VerticalSpace
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import com.isaakhanimann.journal.ui.utils.administrationRouteArticleKey
import com.isaakhanimann.journal.ui.utils.administrationRouteKey

@Preview
@Composable
fun RouteExplanationPreview() {
    RouteExplanationScreen(navigateToURL = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteExplanationScreen(navigateToURL: (url: String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(i18n("routes_title")) })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navigateToURL(AdministrationRoute.PSYCHONAUT_WIKI_ARTICLE_URL) },
                icon = {
                    Icon(
                        Icons.Outlined.Newspaper,
                        contentDescription = i18n("substance_open_article")
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
        ) {
            ElevatedCard(
                modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 3.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    SectionText(
                        text = i18n("routes_intro")
                    )
                    VerticalSpace()
                }
            }
            AdministrationRoute.values().filter { !it.isInjectionMethod }.forEach {
                val routeTitle = i18nOrDefault(administrationRouteKey(it), it.displayText)
                SectionWithTitle(title = routeTitle) {
                    Text(
                        text = i18nOrDefault(administrationRouteArticleKey(it), it.articleText),
                        textAlign = TextAlign.Left,
                        modifier = Modifier
                            .padding(horizontal = horizontalPadding)
                            .padding(bottom = 10.dp)
                    )
                    if (it == AdministrationRoute.RECTAL) {
                        Button(
                            onClick = {
                                navigateToURL(AdministrationRoute.SAFER_PLUGGING_ARTICLE_URL)
                            },
                            modifier = Modifier.padding(horizontal = 5.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Newspaper,
                                contentDescription = i18n("substance_open_article")
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(i18n("safer_plugging"))
                        }
                    }
                }
            }
            AdministrationRoute.values().filter { it.isInjectionMethod }.forEach {
                val routeTitle = i18nOrDefault(administrationRouteKey(it), it.displayText)
                SectionWithTitle(title = routeTitle) {
                    Text(
                        text = i18nOrDefault(administrationRouteArticleKey(it), it.articleText),
                        textAlign = TextAlign.Left,
                        modifier = Modifier
                            .padding(horizontal = horizontalPadding)
                            .padding(bottom = 10.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
