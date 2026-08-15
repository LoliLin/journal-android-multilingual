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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.substances.classes.roa.DoseClass
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.time.DatePickerButton
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.CardWithTitle
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.toReadableString
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

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
        selectedPreset = model.selectedPreset,
        setPreset = viewModel::setPreset,
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

@OptIn(ExperimentalMaterial3Api::class)
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
    selectedPreset: AnalysisPeriodPreset,
    setPreset: (AnalysisPeriodPreset) -> Unit,
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
                TopAppBar(
                    title = { Text(i18n("stats_analysis_title")) },
                    actions = {
                        var isConsumerDropdownExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { isConsumerDropdownExpanded = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = i18n("stats_analysis_consumer"),
                                    tint = if (selectedConsumerName != null) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
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
                )
                StatsSectionTabs(
                    selectedSection = selectedSection,
                    onSelectSection = onSelectSection
                )
            }
        }
    ) { padding ->
        if (usedSubstances.isEmpty()) {
            EmptyAnalysisHint(noData = false)
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                item {
                    SubstanceSelector(
                        usedSubstances = usedSubstances,
                        getSubstanceDisplayName = getSubstanceDisplayName,
                        selectedSubstances = selectedSubstances,
                        toggleSubstance = toggleSubstance,
                        clearSubstances = clearSubstances
                    )
                }
                item {
                    PeriodPresetSelector(
                        selectedPreset = selectedPreset,
                        setPreset = setPreset
                    )
                }
                item {
                    TimeRangeRow(
                        startDate = startDate,
                        endDate = endDate,
                        setStartDate = setStartDate,
                        setEndDate = setEndDate,
                        clearTimeRange = clearTimeRange
                    )
                }
                if (selectedSubstances.isEmpty()) {
                    item { EmptyAnalysisHint() }
                } else if (model.ingestionCount == 0) {
                    item { EmptyAnalysisHint(noData = true) }
                } else {
                    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                        item {
                            Text(
                                text = i18n("stats_analysis_invalid_range"),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = horizontalPadding)
                            )
                        }
                    }
                    item {
                        SummaryCards(model, getSubstanceDisplayName)
                    }
                    items(model.perSubstanceCharts, key = { it.substanceName }) { chart ->
                        SubstanceChartCard(
                            chart = chart,
                            getSubstanceDisplayName = getSubstanceDisplayName,
                            onClick = {
                                navigateToSubstanceCompanion(chart.substanceName, selectedConsumerName)
                            }
                        )
                    }
                    item {
                        IngestionList(model, getSubstanceDisplayName, navigateToSubstanceCompanion)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubstanceSelector(
    usedSubstances: List<String>,
    getSubstanceDisplayName: (String) -> String,
    selectedSubstances: Set<String>,
    toggleSubstance: (String) -> Unit,
    clearSubstances: () -> Unit
) {
    CardWithTitle(title = i18n("stats_analysis_substances")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 8.dp)
        ) {
            var substanceSearchText by rememberSaveable { mutableStateOf("") }
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
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 168.dp)
            ) {
                items(filteredSubstances) { substanceName ->
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
                    Icon(
                        Icons.Outlined.Clear,
                        contentDescription = i18n("stats_analysis_clear_selection")
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PeriodPresetSelector(
    selectedPreset: AnalysisPeriodPreset,
    setPreset: (AnalysisPeriodPreset) -> Unit
) {
    CardWithTitle(title = i18n("stats_analysis_period")) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnalysisPeriodPreset.entries
                .filter { it != AnalysisPeriodPreset.CUSTOM }
                .forEach { preset ->
                    FilterChip(
                        selected = selectedPreset == preset,
                        onClick = { setPreset(preset) },
                        label = { Text(presetLabel(preset)) }
                    )
                }
        }
    }
}

private fun presetLabel(preset: AnalysisPeriodPreset): String = when (preset) {
    AnalysisPeriodPreset.ALL_TIME -> i18n("stats_analysis_preset_all_time")
    AnalysisPeriodPreset.LAST_30_DAYS -> i18n("stats_analysis_preset_last_30")
    AnalysisPeriodPreset.LAST_90_DAYS -> i18n("stats_analysis_preset_last_90")
    AnalysisPeriodPreset.THIS_YEAR -> i18n("stats_analysis_preset_this_year")
    AnalysisPeriodPreset.LAST_YEAR -> i18n("stats_analysis_preset_last_year")
    AnalysisPeriodPreset.CUSTOM -> i18n("stats_analysis_time_range")
}

@Composable
private fun TimeRangeRow(
    startDate: LocalDate?,
    endDate: LocalDate?,
    setStartDate: (LocalDate?) -> Unit,
    setEndDate: (LocalDate?) -> Unit,
    clearTimeRange: () -> Unit
) {
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
            Text(
                text = i18n("stats_analysis_relative_total_hint"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val isDarkTheme = isSystemInDarkTheme()
            // relative totals first (comparable across substances), absolute as annotation
            val sortedLines = model.totalDoseBySubstance.sortedWith(
                compareByDescending<TotalDoseLine> { it.relativeTotal }
                    .thenBy { it.substanceName }
            )
            sortedLines.forEach { line ->
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
                        text = when {
                            line.relativeTotal != null ->
                                "${line.relativeTotal.toReadableString()}× " +
                                    "(${line.absoluteTotal.toReadableString()} ${line.units})".trim()
                            else ->
                                "${line.absoluteTotal.toReadableString()} ${line.units}".trim()
                        },
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
private fun SubstanceChartCard(
    chart: SubstanceChartData,
    getSubstanceDisplayName: (String) -> String,
    onClick: () -> Unit
) {
    val chartColor = chart.color?.getComposeColor(isSystemInDarkTheme())
        ?: MaterialTheme.colorScheme.primary
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getSubstanceDisplayName(chart.substanceName),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = chart.relativeTotal?.let { "${it.toReadableString()}×" }
                        ?: chart.absoluteTotal.toReadableString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (chart.unknownDoseCount > 0) {
                Text(
                    text = i18n(
                        "stats_analysis_unknown_doses",
                        mapOf("count" to chart.unknownDoseCount.toString())
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = i18n("stats_analysis_dose_frequency"),
                style = MaterialTheme.typography.labelLarge
            )
            DoseBucketBars(chart, chartColor)
            if (chart.doseClassCounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = i18n("stats_analysis_dose_class"),
                    style = MaterialTheme.typography.labelLarge
                )
                DoseClassDistribution(chart.doseClassCounts)
            }
            if (chart.perDayCumulativeRelative.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = i18n("stats_analysis_cumulative"),
                    style = MaterialTheme.typography.labelLarge
                )
                CumulativeSparkline(
                    points = chart.perDayCumulativeRelative,
                    color = chartColor
                )
            }
        }
    }
}

@Composable
private fun DoseBucketBars(chart: SubstanceChartData, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val maxCount = chart.doseBuckets.maxOfOrNull { it.second } ?: 1
        chart.doseBuckets.forEach { (bucket, count) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(52.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height((count.toFloat() / maxCount * 96f).dp)
                        .fillMaxWidth()
                        .background(
                            color = color,
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = chart.bucketLabel(bucket),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun DoseClassDistribution(counts: List<Pair<DoseClass, Int>>) {
    val isDarkTheme = isSystemInDarkTheme()
    val maxCount = counts.maxOfOrNull { it.second } ?: 1
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        counts.forEach { (doseClass, count) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = doseClass.getComposeColor(isDarkTheme),
                            shape = RoundedCornerShape(5.dp)
                        )
                )
                Text(
                    text = i18n(doseClassKey(doseClass)),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(88.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .background(
                            color = doseClass.getComposeColor(isDarkTheme).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(5.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(count.toFloat() / maxCount)
                            .height(10.dp)
                            .background(
                                color = doseClass.getComposeColor(isDarkTheme),
                                shape = RoundedCornerShape(5.dp)
                            )
                    )
                }
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun doseClassKey(doseClass: DoseClass): String = when (doseClass) {
    DoseClass.THRESHOLD -> "dose_class_threshold"
    DoseClass.LIGHT -> "dose_class_light"
    DoseClass.COMMON -> "dose_class_common"
    DoseClass.STRONG -> "dose_class_strong"
    DoseClass.HEAVY -> "dose_class_heavy"
}

@Composable
private fun CumulativeSparkline(points: List<Pair<LocalDate, Double>>, color: Color) {
    val firstDate = points.first().first
    val lastDate = points.last().first
    val daySpan = ChronoUnit.DAYS.between(firstDate, lastDate).toInt() + 1
    if (daySpan < 2) {
        Text(
            text = "${points.last().second.toReadableString()}×",
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }
    val max = points.maxOfOrNull { it.second } ?: 0.0
    if (max <= 0) {
        Text(
            text = "0×",
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(72.dp)
        .padding(top = 4.dp)
    ) {
        val path = Path()
        points.forEachIndexed { index, (date, value) ->
            val x = size.width * ChronoUnit.DAYS.between(firstDate, date).toFloat() / (daySpan - 1)
            val y = size.height * (1f - (value / max).toFloat())
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        val areaPath = Path().apply {
            addPath(path)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(areaPath, color = color.copy(alpha = 0.12f))
        drawPath(path, color = color, style = Stroke(width = 2.dp.toPx()))
    }
    Text(
        text = "${max.toReadableString()}×",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
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
            val shown = model.ingestions.take(50)
            if (model.ingestions.size > shown.size) {
                Text(
                    text = i18n(
                        "stats_analysis_showing_first",
                        mapOf("count" to shown.size.toString(), "total" to model.ingestions.size.toString())
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            shown.forEach { ingestionWith ->
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
