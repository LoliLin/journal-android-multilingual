/*
 * Copyright (c) 2026. Journal Multilingual fork.
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

package com.isaakhanimann.journal.ui.tabs.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.time.DatePickerButton
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.CardWithTitle
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.toReadableString
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun StatsAnalysisScreen(
    viewModel: StatsAnalysisViewModel = hiltViewModel(),
    navigateToSubstanceCompanion: (substanceName: String, consumerName: String?) -> Unit,
    selectedSection: StatsSection = StatsSection.ANALYSIS,
    onSelectSection: (StatsSection) -> Unit = {}
) {
    val usedSubstances = viewModel.usedSubstancesFlow.collectAsState().value
    val consumerNames = viewModel.consumerNamesFlow.collectAsState().value
    val selectedConsumerName = viewModel.selectedConsumerNameFlow.collectAsState().value
    val model = viewModel.modelFlow.collectAsState().value
    val startDate = viewModel.startDateFlow.collectAsState().value
    val endDate = viewModel.endDateFlow.collectAsState().value
    StatsAnalysisScreenContent(
        usedSubstances = usedSubstances,
        consumerNames = consumerNames,
        selectedConsumerName = selectedConsumerName,
        setSelectedConsumerName = viewModel::setSelectedConsumerName,
        getSubstanceDisplayName = viewModel.substanceRepo::getDisplayName,
        selectedSubstances = model.selectedSubstances,
        toggleSubstance = viewModel::toggleSubstance,
        clearSubstances = viewModel::clearSubstances,
        startDate = startDate,
        endDate = endDate,
        setStartDate = viewModel::setStartDate,
        setEndDate = viewModel::setEndDate,
        clearTimeRange = viewModel::clearTimeRange,
        model = model,
        navigateToSubstanceCompanion = navigateToSubstanceCompanion,
        selectedSection = selectedSection,
        onSelectSection = onSelectSection
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StatsAnalysisScreenContent(
    usedSubstances: List<String>,
    consumerNames: List<String>,
    selectedConsumerName: String?,
    setSelectedConsumerName: (String?) -> Unit,
    getSubstanceDisplayName: (String) -> String,
    selectedSubstances: Set<String>,
    toggleSubstance: (String) -> Unit,
    clearSubstances: () -> Unit,
    startDate: LocalDate?,
    endDate: LocalDate?,
    setStartDate: (LocalDate?) -> Unit,
    setEndDate: (LocalDate?) -> Unit,
    clearTimeRange: () -> Unit,
    model: StatsAnalysisModel,
    navigateToSubstanceCompanion: (substanceName: String, consumerName: String?) -> Unit,
    selectedSection: StatsSection = StatsSection.ANALYSIS,
    onSelectSection: (StatsSection) -> Unit = {}
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text(i18n("stats_analysis_title")) })
                SecondaryTabRow(selectedTabIndex = selectedSection.ordinal) {
                    StatsSection.entries.forEach { section ->
                        Tab(
                            text = {
                                Text(
                                    if (section == StatsSection.OVERVIEW) {
                                        i18n("stats_section_overview")
                                    } else {
                                        i18n("stats_section_analysis")
                                    }
                                )
                            },
                            selected = selectedSection == section,
                            onClick = { onSelectSection(section) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            CardWithTitle(title = i18n("stats_analysis_consumer")) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 8.dp)
                ) {
                    var isConsumerDropdownExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { isConsumerDropdownExpanded = true }) {
                            Text(
                                text = selectedConsumerName ?: i18n("stats_analysis_all_consumers"),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                        DropdownMenu(
                            expanded = isConsumerDropdownExpanded,
                            onDismissRequest = { isConsumerDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(i18n("stats_analysis_all_consumers")) },
                                onClick = {
                                    setSelectedConsumerName(null)
                                    isConsumerDropdownExpanded = false
                                },
                                leadingIcon = {
                                    if (selectedConsumerName == null) {
                                        Icon(Icons.Filled.Check, contentDescription = null)
                                    }
                                }
                            )
                            consumerNames.forEach { consumerName ->
                                DropdownMenuItem(
                                    text = { Text(consumerName) },
                                    onClick = {
                                        setSelectedConsumerName(consumerName)
                                        isConsumerDropdownExpanded = false
                                    },
                                    leadingIcon = {
                                        if (selectedConsumerName == consumerName) {
                                            Icon(Icons.Filled.Check, contentDescription = null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            CardWithTitle(title = i18n("stats_analysis_substances")) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 8.dp)
                ) {
                    if (usedSubstances.isEmpty()) {
                        Text(
                            text = i18n("stats_analysis_no_ingestions"),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        var substanceSearchText by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = substanceSearchText,
                            onValueChange = { substanceSearchText = it },
                            placeholder = { Text(i18n("stats_analysis_search_substances")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val filteredSubstances = usedSubstances.filter { substanceName ->
                            substanceSearchText.isBlank() ||
                                getSubstanceDisplayName(substanceName).contains(
                                    substanceSearchText,
                                    ignoreCase = true
                                )
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredSubstances.forEach { substanceName ->
                                SubstanceChip(
                                    label = getSubstanceDisplayName(substanceName),
                                    isSelected = substanceName in selectedSubstances,
                                    onClick = { toggleSubstance(substanceName) }
                                )
                            }
                        }
                        if (selectedSubstances.isNotEmpty()) {
                            IconButton(
                                onClick = clearSubstances,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(Icons.Outlined.Clear, contentDescription = i18n("stats_analysis_clear_selection"))
                            }
                        }
                    }
                }
            }

            CardWithTitle(title = i18n("stats_analysis_time_range")) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = i18n("stats_analysis_from"),
                            modifier = Modifier.width(48.dp)
                        )
                        DatePickerButton(
                            localDateTime = (startDate ?: LocalDate.now()).atStartOfDay(),
                            onChange = { setStartDate(it.toLocalDate()) },
                            dateString = startDate?.toString()
                                ?: i18n("stats_analysis_any_date")
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = i18n("stats_analysis_to"),
                            modifier = Modifier.width(48.dp)
                        )
                        DatePickerButton(
                            localDateTime = (endDate ?: LocalDate.now()).atStartOfDay(),
                            onChange = { setEndDate(it.toLocalDate()) },
                            dateString = endDate?.toString()
                                ?: i18n("stats_analysis_any_date")
                        )
                    }
                    if (startDate != null || endDate != null) {
                        TextButton(onClick = clearTimeRange) {
                            Text(i18n("stats_analysis_clear_range"))
                        }
                    }
                }
            }

            if (selectedSubstances.isEmpty()) {
                EmptyAnalysisHint()
            } else if (model.ingestionCount == 0) {
                EmptyAnalysisHint(noData = true)
            } else {
                SummaryCards(model, getSubstanceDisplayName)
                DoseFrequencyChart(model)
                PerDayChart(model)
                IngestionList(model, getSubstanceDisplayName, navigateToSubstanceCompanion)
            }
        }
    }
}

@Composable
private fun SubstanceChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = if (isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun EmptyAnalysisHint(noData: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (noData) {
                i18n("stats_analysis_no_data")
            } else {
                i18n("stats_analysis_select_hint")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SummaryCards(
    model: StatsAnalysisModel,
    getSubstanceDisplayName: (String) -> String
) {
    CardWithTitle(title = i18n("stats_analysis_summary")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = i18n(
                    "stats_analysis_ingestion_count",
                    mapOf("count" to model.ingestionCount.toString())
                ),
                style = MaterialTheme.typography.titleMedium
            )
            val isDarkTheme = isSystemInDarkTheme()
            model.totalDoseBySubstance.forEach { line ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = line.color?.getComposeColor(isDarkTheme)
                                    ?: MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            )
                    )
                    Text(
                        text = "${line.totalDose.toReadableString()} ${line.units}".trim(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = getSubstanceDisplayName(line.substanceName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DoseFrequencyChart(model: StatsAnalysisModel) {
    val chartColor = model.primaryColor?.getComposeColor(isSystemInDarkTheme())
        ?: MaterialTheme.colorScheme.primary
    CardWithTitle(title = i18n("stats_analysis_dose_frequency")) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val maxCount = model.doseFrequency.maxOfOrNull { it.second } ?: 1
            model.doseFrequency.forEach { (dose, count) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height((count.toFloat() / maxCount * 96f).dp)
                            .fillMaxWidth()
                            .background(
                                color = chartColor,
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = dose.toReadableString(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun PerDayChart(model: StatsAnalysisModel) {
    val chartColor = model.primaryColor?.getComposeColor(isSystemInDarkTheme())
        ?: MaterialTheme.colorScheme.tertiary
    CardWithTitle(title = i18n("stats_analysis_per_day")) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val maxCount = model.perDayCounts.maxOfOrNull { it.second } ?: 1
            model.perDayCounts.takeLast(60).forEach { (date, count) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(28.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height((count.toFloat() / maxCount * 64f).dp)
                            .fillMaxWidth()
                            .background(
                                color = chartColor,
                                shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                            )
                    )
                    Text(
                        text = date.toString(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun IngestionList(
    model: StatsAnalysisModel,
    getSubstanceDisplayName: (String) -> String,
    navigateToSubstanceCompanion: (substanceName: String, consumerName: String?) -> Unit
) {
    CardWithTitle(title = i18n("stats_analysis_ingestions")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            model.ingestions.take(50).forEach { ingestionWith ->
                val ingestion = ingestionWith.ingestion
                val doseText = ingestion.dose?.toReadableString()
                val unitText = ingestion.units ?: ""
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navigateToSubstanceCompanion(ingestion.substanceName, ingestion.consumerName)
                        },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = ingestionWith.substanceCompanion?.color?.getComposeColor(isSystemInDarkTheme())
                                    ?: MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(5.dp)
                            )
                    )
                    Text(
                        text = ingestion.time.atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$doseText $unitText",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = getSubstanceDisplayName(ingestion.substanceName),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

