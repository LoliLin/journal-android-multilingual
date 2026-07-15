/*
 * Copyright (c) 2022-2023. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.tabs.journal.experience.timeline

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.CardWithTitle
import com.isaakhanimann.journal.ui.tabs.search.substance.BulletPoints
import com.isaakhanimann.journal.ui.tabs.search.substance.SectionText
import com.isaakhanimann.journal.ui.tabs.search.substance.VerticalSpace
import com.isaakhanimann.journal.ui.theme.horizontalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ExplainTimelineScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(i18n("timeline_info_title")) }
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
            VerticalSpace()
            CardWithTitle(title = i18n("timeline_simplifying_title")) {
                val text = buildAnnotatedString {
                    append(i18n("timeline_assumptions_intro_before"))
                    withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                        append(i18n("timeline_assumptions_intro_bold"))
                    }
                    append(i18n("timeline_assumptions_intro_after"))
                }
                SectionText(text = text)
                BulletPoints(
                    points = listOf(
                        i18n("timeline_assumption_bullet1"),
                        i18n("timeline_assumption_bullet2"),
                        i18n("timeline_assumption_bullet3"),
                        i18n("timeline_assumption_bullet4")
                    )
                )
            }
            CardWithTitle(title = i18n("timeline_understanding_title")) {
                BulletPoints(
                    points = listOf(
                        i18n("timeline_understanding_bullet1"),
                        i18n("timeline_understanding_bullet2"),
                        i18n("timeline_understanding_bullet3"),
                        i18n("timeline_understanding_bullet4"),
                        i18n("timeline_understanding_bullet5"),
                        i18n("timeline_understanding_bullet6")
                    )
                )
            }
            CardWithTitle(title = i18n("timeline_pw_durations_title")) {
                SectionText(
                    text = i18n("timeline_duration_intro")
                )
                val titleStyle = MaterialTheme.typography.titleSmall
                Text(text = i18n("duration_total"), style = titleStyle)
                SectionText(text = i18n("timeline_total_desc"))
                Text(text = i18n("duration_onset"), style = titleStyle)
                SectionText(text = i18n("timeline_onset_desc"))
                Text(text = i18n("duration_comeup"), style = titleStyle)
                SectionText(text = i18n("timeline_comeup_desc"))
                Text(text = i18n("duration_peak"), style = titleStyle)
                SectionText(text = i18n("timeline_peak_desc"))
                Text(text = i18n("duration_offset"), style = titleStyle)
                SectionText(text = i18n("timeline_offset_desc"))
                Text(text = i18n("duration_after_effects"), style = titleStyle)
                SectionText(
                    text = i18n("timeline_aftereffects_desc")
                )
            }
        }
    }
}
