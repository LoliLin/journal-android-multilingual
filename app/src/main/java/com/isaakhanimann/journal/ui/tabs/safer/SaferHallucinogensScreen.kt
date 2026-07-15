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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.tabs.search.substance.BulletPoints
import com.isaakhanimann.journal.ui.tabs.search.substance.SectionText
import com.isaakhanimann.journal.ui.tabs.search.substance.SectionWithTitle
import com.isaakhanimann.journal.ui.tabs.search.substance.VerticalSpace
import com.isaakhanimann.journal.ui.theme.horizontalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SaferHallucinogensScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(i18n("hallucinogens_screen_title")) }
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
                Text(
                    text = i18n("safer_hallucinogen_subtitle"),
                    modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 5.dp)
                )
                VerticalSpace()
            }
            SectionWithTitle(title = i18n("hallucinogens_setting_title")) {
                Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    SectionText(text = i18n("hallucinogens_setting_body1"))
                    BulletPoints(
                        points = listOf(
                            i18n("hallucinogens_setting_bullet1"),
                            i18n("hallucinogens_setting_bullet2"),
                            i18n("hallucinogens_setting_bullet3"),
                            i18n("hallucinogens_setting_bullet4")
                        ),
                        modifier = Modifier.padding(horizontal = horizontalPadding)
                    )
                    SectionText(text = i18n("hallucinogens_setting_body2"))
                    VerticalSpace()
                }
            }
            SectionWithTitle(title = i18n("hallucinogens_set_mind_title")) {
                Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    SectionText(
                        text = i18n("hallucinogens_set_mind_body").trimIndent()
                    )
                    VerticalSpace()
                }
            }
            SectionWithTitle(title = i18n("hallucinogens_bodily_title")) {
                Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    SectionText(
                        text = i18n("hallucinogens_bodily_body").trimIndent()
                    )
                    VerticalSpace()
                }
            }
            SectionWithTitle(title = i18n("hallucinogens_tripsitter_title")) {
                Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    SectionText(
                        text = i18n("hallucinogens_tripsitter_body").trimIndent()
                    )
                    VerticalSpace()
                }
            }
            SectionWithTitle(title = i18n("hallucinogens_anchors_title")) {
                Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    SectionText(text = i18n("hallucinogens_anchors_body"))
                    SectionText(text = i18n("hallucinogens_anchors_examples_intro"))
                    BulletPoints(
                        points = listOf(
                            i18n("hallucinogens_anchor_bullet1"),
                            i18n("hallucinogens_anchor_bullet2"),
                            i18n("hallucinogens_anchor_bullet3"),
                            i18n("hallucinogens_anchor_bullet4"),
                            i18n("hallucinogens_anchor_bullet5")
                        ),
                        modifier = Modifier.padding(horizontal = horizontalPadding)
                    )
                    VerticalSpace()
                }
            }

            SectionWithTitle(title = i18n("hallucinogens_aborting_title")) {
                Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    SectionText(

                        text = i18n("hallucinogens_aborting_body").trimIndent()

                    )

                    VerticalSpace()
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
