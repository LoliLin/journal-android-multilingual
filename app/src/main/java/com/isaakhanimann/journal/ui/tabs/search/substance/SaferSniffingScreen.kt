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

package com.isaakhanimann.journal.ui.tabs.search.substance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.theme.horizontalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SaferSniffingScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(i18n("safer_sniffing_short")) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            ElevatedCard(modifier = Modifier.padding(vertical = 5.dp, horizontal = horizontalPadding)) {
                Text(
                    text = i18n("sniffing_intro"),
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .padding(vertical = 10.dp, horizontal = horizontalPadding)
                )
            }
            SectionWithTitle(title = i18n("sniffing_hygiene_title")) {
                Text(
                    text = i18n("sniffing_hygiene_body"),
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .padding(horizontal = horizontalPadding)
                )
            }
            SectionWithTitle(title = i18n("sniffing_mine_title")) {
                Text(
                    text = i18n("sniffing_mine_body"),
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .padding(horizontal = horizontalPadding)
                )
            }
            SectionWithTitle(title = i18n("sniffing_powder_title")) {
                Text(
                    text = i18n("sniffing_powder_body"),
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .padding(horizontal = horizontalPadding)
                )
            }
            SectionWithTitle(title = i18n("sniffing_care_title")) {
                Text(
                    text = i18n("sniffing_care_body"),
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .padding(horizontal = horizontalPadding)
                )
            }
            SectionWithTitle(title = i18n("sniffing_rinsing_title")) {
                Text(
                    text = i18n("sniffing_rinsing_body"),
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .padding(horizontal = horizontalPadding)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}