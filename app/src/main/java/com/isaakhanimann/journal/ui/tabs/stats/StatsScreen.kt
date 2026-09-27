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

package com.isaakhanimann.journal.ui.tabs.stats

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.isaakhanimann.journal.data.substances.classes.roa.DoseClass
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.localization.i18nOrDefault
import com.isaakhanimann.journal.ui.main.bottomBarNestedScroll
import com.isaakhanimann.journal.ui.main.bottomBarOverlayPadding
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.CardWithTitle
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.toReadableString
import com.isaakhanimann.journal.ui.tabs.settings.AvatarUtil
import com.isaakhanimann.journal.ui.theme.JournalTheme
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import com.isaakhanimann.journal.ui.utils.administrationRouteKey
import com.isaakhanimann.journal.ui.utils.renderComposeViewToBitmap
import com.isaakhanimann.journal.ui.utils.shareBitmap
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Entry point: connects StatsViewModel and StatsAnalysisViewModel seamlessly
// ---------------------------------------------------------------------------

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    analysisViewModel: StatsAnalysisViewModel = hiltViewModel(),
    navigateToSubstanceCompanion: (substanceName: String, consumerName: String?) -> Unit,
) {
    val statsModel by viewModel.statsModelFlow.collectAsState()
    val consumerNamesSorted by viewModel.sortedConsumerNamesFlow.collectAsState()
    val ownerUserName by viewModel.ownerUserNameFlow.collectAsState()
    val analysisModel by analysisViewModel.modelFlow.collectAsState()

    var focusedSubstance by rememberSaveable { mutableStateOf<String?>(null) }

    // Synchronize consumer with analysisViewModel
    LaunchedEffect(statsModel.consumerName) {
        analysisViewModel.syncConsumer(statsModel.consumerName)
    }

    // Synchronize time range with analysisViewModel
    LaunchedEffect(statsModel.selectedOption) {
        val today = LocalDate.now()
        val start = today.minus(statsModel.selectedOption.allBucketSizes)
        analysisViewModel.syncDateRange(start, today)
    }

    // Synchronize selected substances:
    // If a substance is focused, analyze only that substance.
    // If none is focused, analyze all substances present in the current stats overview.
    LaunchedEffect(focusedSubstance, statsModel.statItems) {
        if (focusedSubstance != null) {
            analysisViewModel.setSelectedSubstances(setOf(focusedSubstance!!))
        } else {
            val allSubstances = statsModel.statItems.map { it.substanceName }.toSet()
            analysisViewModel.setSelectedSubstances(allSubstances)
        }
    }

    MergedStatsScreen(
        statsModel = statsModel,
        onTapOption = viewModel::onTapOption,
        onChangeConsumerName = viewModel::onChangeConsumer,
        consumerNamesSorted = consumerNamesSorted,
        ownerUserName = ownerUserName ?: "You",
        analysisModel = analysisModel,
        getSubstanceDisplayName = analysisViewModel.substanceRepo::getDisplayName,
        focusedSubstance = focusedSubstance,
        onFocusSubstance = { substanceName ->
            focusedSubstance = if (focusedSubstance == substanceName) null else substanceName
        },
        onClearFocus = { focusedSubstance = null },
        navigateToSubstanceCompanion = navigateToSubstanceCompanion,
    )
}

// ---------------------------------------------------------------------------
// Merged page: completely unified scrollable screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun MergedStatsScreen(
    statsModel: StatsModel,
    onTapOption: (TimePickerOption) -> Unit,
    onChangeConsumerName: (String?) -> Unit,
    consumerNamesSorted: List<String>,
    ownerUserName: String,
    analysisModel: StatsAnalysisModel,
    getSubstanceDisplayName: (String) -> String,
    focusedSubstance: String?,
    onFocusSubstance: (String) -> Unit,
    onClearFocus: () -> Unit,
    navigateToSubstanceCompanion: (substanceName: String, consumerName: String?) -> Unit,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSharing by remember { mutableStateOf(false) }
    val currentView = LocalView.current
    val density = LocalDensity.current
    val widthPx = (LocalConfiguration.current.screenWidthDp * density.density).toInt()
    val maxBucketColumns = analysisModel.perSubstanceCharts.maxOfOrNull { it.doseBuckets.size } ?: 0
    val shareWidthPx = widthPx.coerceAtLeast(((56 + maxBucketColumns * 62) * density.density).toInt())

    val overviewConsumerName = statsModel.consumerName

    // Highlight focused substance color in BarChart, dim others
    val focusedColor = remember(focusedSubstance, statsModel.statItems) {
        focusedSubstance?.let { name ->
            statsModel.statItems.firstOrNull { it.substanceName == name }?.color
        }
    }

    val shareAnalysisContent: @Composable () -> Unit = {
        JournalTheme {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (overviewConsumerName != null) {
                            i18n("stats_analysis_title_for_consumer", mapOf("consumer" to overviewConsumerName))
                        } else {
                            i18n("stats_analysis_title")
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (analysisModel.ingestionCount > 0) {
                        analysisModel.perSubstanceCharts
                            .sortedWith(compareByDescending<SubstanceChartData> { it.relativeTotal }.thenBy { it.substanceName })
                            .forEach { chart ->
                                SubstanceChartCardInner(
                                    chart = chart,
                                    getSubstanceDisplayName = getSubstanceDisplayName,
                                    isFocused = false,
                                    onClick = {}
                                )
                            }
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.bottomBarNestedScroll(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 4.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (overviewConsumerName != null) {
                            i18n("stats_title_for_consumer", replacements = mapOf("consumer" to overviewConsumerName))
                        } else if (ownerUserName != "You") {
                            i18n("stats_title_for_consumer", replacements = mapOf("consumer" to ownerUserName))
                        } else {
                            i18n("stats_title")
                        },
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )

                    // Share analysis report button
                    if (analysisModel.ingestionCount > 0) {
                        IconButton(
                            onClick = {
                                if (!isSharing) {
                                    isSharing = true
                                    coroutineScope.launch {
                                        try {
                                            val activity = context as? androidx.activity.ComponentActivity
                                            if (activity != null) {
                                                val bitmap = renderComposeViewToBitmap(
                                                    context = context,
                                                    widthPx = shareWidthPx,
                                                    lifecycleView = currentView,
                                                    content = shareAnalysisContent,
                                                    postLayoutDelayMs = 300L
                                                )
                                                shareBitmap(context, bitmap)
                                            }
                                        } catch (e: Exception) {
                                            Log.e("StatsScreen", "share error", e)
                                            Toast.makeText(context, "${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isSharing = false
                                        }
                                    }
                                }
                            },
                            enabled = !isSharing
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = i18n("common_share"),
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                        }
                    }

                    // Single unified consumer selector
                    var isConsumerSelectionExpanded by remember { mutableStateOf(false) }
                    val currentConsumerName = overviewConsumerName ?: ownerUserName
                    val currentAvatarFile = remember(currentConsumerName) {
                        AvatarUtil.getUserAvatar(context, currentConsumerName)
                    }
                    IconButton(onClick = { isConsumerSelectionExpanded = true }) {
                        if (currentAvatarFile != null) {
                            AsyncImage(
                                model = currentAvatarFile,
                                contentDescription = i18n("stats_consumer"),
                                modifier = Modifier.size(32.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Outlined.Person, contentDescription = i18n("stats_consumer"))
                        }
                    }
                    DropdownMenu(
                        expanded = isConsumerSelectionExpanded,
                        onDismissRequest = { isConsumerSelectionExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(ownerUserName) },
                            onClick = {
                                onChangeConsumerName(null)
                                isConsumerSelectionExpanded = false
                            },
                            leadingIcon = {
                                if (overviewConsumerName == null) {
                                    Icon(Icons.Filled.Check, null, Modifier.size(ButtonDefaults.IconSize))
                                }
                            }
                        )
                        consumerNamesSorted.forEach { consumerName ->
                            DropdownMenuItem(
                                text = { Text(consumerName) },
                                onClick = {
                                    onChangeConsumerName(consumerName)
                                    isConsumerSelectionExpanded = false
                                },
                                leadingIcon = {
                                    if (overviewConsumerName == consumerName) {
                                        Icon(Icons.Filled.Check, null, Modifier.size(ButtonDefaults.IconSize))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (!statsModel.areThereAnyIngestions) {
            EmptyScreenDisclaimer(
                title = i18n("stats_empty_title"),
                description = i18n("stats_empty_description")
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = bottomBarOverlayPadding()
            ) {
                // ── Time range picker ─────────────────────────────────────────
                item {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding, vertical = 8.dp)
                    ) {
                        TimePickerOption.entries.forEachIndexed { index, option ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = TimePickerOption.entries.size
                                ),
                                selected = statsModel.selectedOption.tabIndex == index,
                                onClick = { onTapOption(option) }
                            ) {
                                Text(option.displayText)
                            }
                        }
                    }
                }

                if (statsModel.statItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            ) {
                                Text(
                                    text = i18n(
                                        "stats_no_ingestions_since",
                                        replacements = mapOf("period" to statsModel.selectedOption.longDisplayText)
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = i18n("stats_choose_longer_duration"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // ── Bar chart (scrollable, with focus highlight) ───────────
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .padding(horizontal = horizontalPadding)
                                .padding(bottom = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = i18n(
                                        if (statsModel.isByIngestionTime) "stats_ingestions_since" else "stats_experiences_since",
                                        replacements = mapOf("date" to statsModel.startDateText)
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp)
                                )
                                Text(
                                    text = i18n(
                                        if (statsModel.isByIngestionTime) "stats_chart_by_ingestion_time" else "stats_substance_counted_once"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                                )
                                BarChart(
                                    buckets = statsModel.chartBuckets,
                                    startDateText = statsModel.startDateText,
                                    highlightedColor = focusedColor,
                                )
                            }
                        }
                    }

                    // ── Focused substance pinned at top ───────────────────────
                    if (focusedSubstance != null) {
                        val focused = statsModel.statItems.firstOrNull { it.substanceName == focusedSubstance }
                        if (focused != null) {
                            item(key = "pin_${focused.substanceName}") {
                                FocusedSubstancePinCard(
                                    subStat = focused,
                                    isDarkTheme = isDarkTheme,
                                    getSubstanceDisplayName = getSubstanceDisplayName,
                                    onDismiss = onClearFocus,
                                    navigateToSubstanceCompanion = {
                                        navigateToSubstanceCompanion(focused.substanceName, overviewConsumerName)
                                    }
                                )
                            }
                        }
                    }

                    // ── Substance rows (long-press to focus) ──────────────────
                    items(statsModel.statItems, key = { "row_${it.substanceName}" }) { subStat ->
                        if (subStat.substanceName == focusedSubstance) return@items
                        StatItemRow(
                            subStat = subStat,
                            isDarkTheme = isDarkTheme,
                            getSubstanceDisplayName = getSubstanceDisplayName,
                            onClick = {
                                navigateToSubstanceCompanion(subStat.substanceName, overviewConsumerName)
                            },
                            onLongClick = {
                                onFocusSubstance(subStat.substanceName)
                            }
                        )
                    }

                    // ── Seamless Analysis section header ───────────────────────
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 12.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (focusedSubstance != null) {
                                    "${i18n("stats_section_analysis")} · ${getSubstanceDisplayName(focusedSubstance)}"
                                } else {
                                    i18n("stats_section_analysis")
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (focusedSubstance != null) {
                                IconButton(onClick = onClearFocus) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = i18n("stats_analysis_clear_selection"),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ── Analysis Content ──────────────────────────────────────
                    if (analysisModel.ingestionCount > 0) {
                        // Summary card comparing all substances (only when not focusing a single substance)
                        if (focusedSubstance == null && analysisModel.totalDoseBySubstance.size > 1) {
                            item(key = "analysis_summary") {
                                AnalysisSummaryBlock(analysisModel, getSubstanceDisplayName)
                            }
                        }

                        // Per-substance chart cards
                        val displayedCharts = if (focusedSubstance != null) {
                            analysisModel.perSubstanceCharts.filter { it.substanceName == focusedSubstance }
                        } else {
                            analysisModel.perSubstanceCharts
                                .sortedWith(compareByDescending<SubstanceChartData> { it.relativeTotal }.thenBy { it.substanceName })
                        }

                        items(displayedCharts, key = { "chart_${it.substanceName}" }) { chart ->
                            SubstanceChartCardInner(
                                chart = chart,
                                getSubstanceDisplayName = getSubstanceDisplayName,
                                isFocused = chart.substanceName == focusedSubstance,
                                onClick = {
                                    navigateToSubstanceCompanion(
                                        chart.substanceName,
                                        overviewConsumerName
                                    )
                                }
                            )
                        }

                        // Ingestions history block
                        item(key = "analysis_ingestions") {
                            AnalysisIngestionListBlock(
                                model = analysisModel,
                                getSubstanceDisplayName = getSubstanceDisplayName,
                                navigateToSubstanceCompanion = navigateToSubstanceCompanion
                            )
                        }
                    } else {
                        item(key = "analysis_empty") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = i18n("stats_analysis_no_data"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Focused substance pinned card
// ---------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FocusedSubstancePinCard(
    subStat: StatItem,
    isDarkTheme: Boolean,
    getSubstanceDisplayName: (String) -> String,
    onDismiss: () -> Unit,
    navigateToSubstanceCompanion: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 4.dp)
            .combinedClickable(onClick = navigateToSubstanceCompanion, onLongClick = onDismiss)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(color = subStat.color.getComposeColor(isDarkTheme), shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getSubstanceDisplayName(subStat.substanceName),
                    style = MaterialTheme.typography.titleMedium
                )
                val countText = if (subStat.experienceCount == 1) {
                    i18n("stats_experience_count_one", replacements = mapOf("count" to subStat.experienceCount.toString()))
                } else {
                    i18n("stats_experience_count_other", replacements = mapOf("count" to subStat.experienceCount.toString()))
                }
                Text(
                    text = countText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Stat row with long-press support
// ---------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StatItemRow(
    subStat: StatItem,
    isDarkTheme: Boolean,
    getSubstanceDisplayName: (String) -> String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 4.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(color = subStat.color.getComposeColor(isDarkTheme), shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getSubstanceDisplayName(subStat.substanceName),
                    style = MaterialTheme.typography.titleMedium
                )
                val experienceCountText = if (subStat.experienceCount == 1) {
                    i18n("stats_experience_count_one", replacements = mapOf("count" to subStat.experienceCount.toString()))
                } else {
                    i18n("stats_experience_count_other", replacements = mapOf("count" to subStat.experienceCount.toString()))
                }
                Text(
                    text = experienceCountText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                val dose = subStat.totalDose
                val rel = subStat.relativeTotalDose
                when {
                    rel != null && dose != null -> {
                        val key = if (dose.isEstimate) {
                            if (dose.estimatedDoseStandardDeviation != null) {
                                "stats_total_dose_relative_estimated_sd"
                            } else {
                                "stats_total_dose_relative_estimated"
                            }
                        } else {
                            "stats_total_dose_relative"
                        }
                        val replacements = buildMap {
                            put("dose", dose.dose.toReadableString())
                            put("units", dose.units)
                            put("relative", rel.toReadableString())
                            if (dose.estimatedDoseStandardDeviation != null) {
                                put("sd", dose.estimatedDoseStandardDeviation.toReadableString())
                            }
                        }
                        Text(text = i18n(key, replacements = replacements), style = MaterialTheme.typography.bodyMedium)
                    }
                    rel != null -> Text(
                        text = i18n("stats_total_dose_relative_only", replacements = mapOf("dose" to rel.toReadableString())),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    dose != null -> {
                        val key = if (dose.isEstimate) {
                            if (dose.estimatedDoseStandardDeviation != null) {
                                "stats_total_dose_estimated_with_sd"
                            } else {
                                "stats_total_dose_estimated"
                            }
                        } else {
                            "stats_total_dose"
                        }
                        val replacements = buildMap {
                            put("dose", dose.dose.toReadableString())
                            put("units", dose.units)
                            if (dose.estimatedDoseStandardDeviation != null) {
                                put("sd", dose.estimatedDoseStandardDeviation.toReadableString())
                            }
                        }
                        Text(text = i18n(key, replacements = replacements), style = MaterialTheme.typography.bodyMedium)
                    }
                    else -> Text(
                        text = i18n("stats_total_dose_unknown"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    subStat.routeCounts.forEach {
                        val routeName = i18nOrDefault(
                            administrationRouteKey(it.administrationRoute),
                            it.administrationRoute.displayText
                        ).lowercase()
                        Text(
                            text = "$routeName ${it.count}×",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Analysis blocks (clean, integrated directly)
// ---------------------------------------------------------------------------

@Composable
private fun AnalysisSummaryBlock(model: StatsAnalysisModel, getSubstanceDisplayName: (String) -> String) {
    CardWithTitle(title = i18n("stats_analysis_summary")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = i18n("stats_analysis_ingestion_count", mapOf("count" to model.ingestionCount.toString())),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = i18n("stats_analysis_relative_total_hint"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val isDarkTheme = isSystemInDarkTheme()
            model.totalDoseBySubstance
                .sortedWith(compareByDescending<TotalDoseLine> { it.relativeTotal }.thenBy { it.substanceName })
                .forEach { line ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = line.color?.getComposeColor(isDarkTheme) ?: MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                        Text(
                            text = when {
                                line.relativeTotal != null -> "${line.relativeTotal.toReadableString()}× (${line.absoluteTotal.toReadableString()} ${line.units})".trim()
                                else -> "${line.absoluteTotal.toReadableString()} ${line.units}".trim()
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
private fun SubstanceChartCardInner(
    chart: SubstanceChartData,
    getSubstanceDisplayName: (String) -> String,
    isFocused: Boolean,
    onClick: () -> Unit,
) {
    val chartColor = chart.color?.getComposeColor(isSystemInDarkTheme()) ?: MaterialTheme.colorScheme.primary
    Card(
        onClick = onClick,
        colors = if (isFocused) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
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
                Text(text = getSubstanceDisplayName(chart.substanceName), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = chart.relativeTotal?.let { "${it.toReadableString()}×" } ?: chart.absoluteTotal.toReadableString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (chart.unknownDoseCount > 0) {
                Text(
                    text = i18n("stats_analysis_unknown_doses", mapOf("count" to chart.unknownDoseCount.toString())),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = i18n("stats_analysis_dose_frequency"), style = MaterialTheme.typography.labelLarge)
            // Dose bucket bars (horizontal scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val maxCount = chart.doseBuckets.maxOfOrNull { it.second } ?: 1
                chart.doseBuckets.forEach { (bucket, count) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(52.dp)) {
                        Box(
                            modifier = Modifier
                                .height((count.toFloat() / maxCount * 96f).dp)
                                .fillMaxWidth()
                                .background(color = chartColor, shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                        Text(text = count.toString(), style = MaterialTheme.typography.labelSmall)
                        Text(text = chart.bucketLabel(bucket), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            if (chart.doseClassCounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = i18n("stats_analysis_dose_class"), style = MaterialTheme.typography.labelLarge)
                val isDarkTheme = isSystemInDarkTheme()
                val maxClassCount = chart.doseClassCounts.maxOfOrNull { it.second } ?: 1
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    chart.doseClassCounts.forEach { (doseClass, count) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(color = doseClass.getComposeColor(isDarkTheme), shape = RoundedCornerShape(5.dp))
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
                                    .background(color = doseClass.getComposeColor(isDarkTheme).copy(alpha = 0.3f), shape = RoundedCornerShape(5.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(count.toFloat() / maxClassCount)
                                        .height(10.dp)
                                        .background(color = doseClass.getComposeColor(isDarkTheme), shape = RoundedCornerShape(5.dp))
                                )
                            }
                            Text(text = count.toString(), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            if (chart.perDayCumulativeRelative.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = i18n("stats_analysis_cumulative"), style = MaterialTheme.typography.labelLarge)
                CumulativeSparklineInner(points = chart.perDayCumulativeRelative, color = chartColor)
            }
        }
    }
}

@Composable
private fun CumulativeSparklineInner(points: List<Pair<LocalDate, Double>>, color: Color) {
    val firstDate = points.first().first
    val lastDate = points.last().first
    val daySpan = ChronoUnit.DAYS.between(firstDate, lastDate).toInt() + 1
    if (daySpan < 2) {
        Text(text = "${points.last().second.toReadableString()}×", style = MaterialTheme.typography.bodyMedium)
        return
    }
    val max = points.maxOfOrNull { it.second } ?: 0.0
    if (max <= 0) {
        Text(text = "0×", style = MaterialTheme.typography.bodyMedium)
        return
    }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(top = 4.dp)
    ) {
        val path = Path()
        points.forEachIndexed { index, (date, value) ->
            val x = size.width * ChronoUnit.DAYS.between(firstDate, date).toFloat() / (daySpan - 1)
            val y = size.height * (1f - (value / max).toFloat())
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
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

private fun doseClassKey(doseClass: DoseClass): String = when (doseClass) {
    DoseClass.THRESHOLD -> "dose_class_threshold"
    DoseClass.LIGHT -> "dose_class_light"
    DoseClass.COMMON -> "dose_class_common"
    DoseClass.STRONG -> "dose_class_strong"
    DoseClass.HEAVY -> "dose_class_heavy"
}

@Composable
private fun AnalysisIngestionListBlock(
    model: StatsAnalysisModel,
    getSubstanceDisplayName: (String) -> String,
    navigateToSubstanceCompanion: (substanceName: String, consumerName: String?) -> Unit,
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
                        .clickable { navigateToSubstanceCompanion(ingestion.substanceName, ingestion.consumerName) },
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

// ---------------------------------------------------------------------------
// Empty state
// ---------------------------------------------------------------------------

@Composable
fun EmptyScreenDisclaimer(title: String, description: String) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.BarChart,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
