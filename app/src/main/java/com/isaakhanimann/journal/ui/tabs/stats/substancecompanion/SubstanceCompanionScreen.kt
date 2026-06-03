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

package com.isaakhanimann.journal.ui.tabs.stats.substancecompanion

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion
import com.isaakhanimann.journal.data.substances.classes.Tolerance
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.localization.i18nOrDefault
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.CardWithTitle
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.ToleranceSection
import com.isaakhanimann.journal.ui.theme.JournalTheme
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import com.isaakhanimann.journal.ui.utils.administrationRouteKey
import com.isaakhanimann.journal.ui.utils.getStringOfPattern
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository

@Composable
fun SubstanceCompanionScreen(
    viewModel: SubstanceCompanionViewModel = hiltViewModel()
) {
    val companion = viewModel.thisCompanionFlow.collectAsState().value
    if (companion == null) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {}
    } else {
        SubstanceCompanionScreen(
            substanceCompanion = companion,

            ingestionBursts = viewModel.ingestionBurstsFlow.collectAsState().value,

            tolerance = viewModel.tolerance,

            crossTolerances = viewModel.crossTolerances,

            consumerName = viewModel.consumerName,

            substanceRepo = viewModel.substanceRepo
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstanceCompanionScreen(

    substanceCompanion: SubstanceCompanion,

    ingestionBursts: List<IngestionsBurst>,

    tolerance: Tolerance?,

    crossTolerances: List<String>,

    consumerName: String? = null,

    substanceRepo: SubstanceRepository

) {

    Scaffold(

        topBar = {

            val title = if (consumerName == null) {

                (substanceRepo?.getDisplayName(substanceCompanion.substanceName) ?: substanceCompanion.substanceName)
            } else {
                "${(substanceRepo?.getDisplayName(substanceCompanion.substanceName) ?: substanceCompanion.substanceName)} ($consumerName)"
            }
            TopAppBar(title = { Text(title) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                if (tolerance != null || crossTolerances.isNotEmpty()) {
                    CardWithTitle(title = i18n("substance_tolerance_title"), modifier = Modifier.fillMaxWidth()) {
                        ToleranceSection(
                            tolerance = tolerance,
                            crossTolerances = crossTolerances
                        )
                    }
                }
                Text(text = i18n("time_now_label"))
            }
            items(ingestionBursts) { burst ->
                TimeArrowUp(timeText = burst.timeUntil)
                ElevatedCard(modifier = Modifier.padding(vertical = 5.dp)) {
                    Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                        ) {
                            Text(
                                text = burst.experience.title,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = burst.experience.sortDate.getStringOfPattern("EEE, dd MMM yyyy"),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        HorizontalDivider()
                        burst.ingestions.forEachIndexed { index, ingestion ->
                            IngestionRow(ingestionAndCustomUnit = ingestion)
                            if (index < burst.ingestions.size - 1) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IngestionRow(ingestionAndCustomUnit: IngestionsBurst.IngestionAndCustomUnit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val routeName = i18nOrDefault(
            administrationRouteKey(ingestionAndCustomUnit.ingestion.administrationRoute),
            ingestionAndCustomUnit.ingestion.administrationRoute.displayText
        ).lowercase()
        val text = buildAnnotatedString {
            append(ingestionAndCustomUnit.getDoseDescription())
            withStyle(style = SpanStyle(color = if (isSystemInDarkTheme()) Color.Gray else Color.LightGray )) {
                if (ingestionAndCustomUnit.customUnit == null) {
                    append(" $routeName")
                }
                ingestionAndCustomUnit.customUnitDose?.calculatedDoseDescription?.let {
                    append(" = $it $routeName")
                }
            }
        }
        Text(text = text, style = MaterialTheme.typography.titleSmall)
        val dateString = ingestionAndCustomUnit.ingestion.time.getStringOfPattern("HH:mm")
        Text(text = dateString)
    }
}
