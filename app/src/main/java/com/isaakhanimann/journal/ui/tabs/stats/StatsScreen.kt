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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.main.bottomBarNestedScroll
import com.isaakhanimann.journal.ui.main.bottomBarOverlayPadding
import com.isaakhanimann.journal.localization.i18nOrDefault
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.toReadableString
import com.isaakhanimann.journal.ui.tabs.settings.AvatarUtil
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import com.isaakhanimann.journal.ui.utils.administrationRouteKey

enum class StatsSection { OVERVIEW, ANALYSIS }

@Composable
fun StatsSectionTabs(
    selectedSection: StatsSection,
    onSelectSection: (StatsSection) -> Unit
) {
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

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    navigateToSubstanceCompanion: (substanceName: String, consumerName: String?) -> Unit
) {
    var selectedSection by rememberSaveable { mutableStateOf(StatsSection.OVERVIEW) }
    val sectionStateHolder = rememberSaveableStateHolder()
    // The secondary navigation is shared between both sections; each section keeps its
    // internal state (search text, scroll position) across tab switches.
    when (selectedSection) {
        StatsSection.OVERVIEW -> sectionStateHolder.SaveableStateProvider(StatsSection.OVERVIEW.name) {
            StatsScreen(
                navigateToSubstanceCompanion = navigateToSubstanceCompanion,
                onTapOption = viewModel::onTapOption,
                statsModel = viewModel.statsModelFlow.collectAsState().value,
                onChangeConsumerName = viewModel::onChangeConsumer,
                consumerNamesSorted = viewModel.sortedConsumerNamesFlow.collectAsState().value,
                ownerUserName = viewModel.ownerUserNameFlow.collectAsState().value ?: "You",
                selectedSection = selectedSection,
                onSelectSection = { selectedSection = it }
            )
        }
        StatsSection.ANALYSIS -> sectionStateHolder.SaveableStateProvider(StatsSection.ANALYSIS.name) {
            StatsAnalysisScreen(
                navigateToSubstanceCompanion = navigateToSubstanceCompanion,
                selectedSection = selectedSection,
                onSelectSection = { selectedSection = it }
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navigateToSubstanceCompanion: (substanceName: String, consumerName: String?) -> Unit,
    onTapOption: (option: TimePickerOption) -> Unit,
    statsModel: StatsModel,
    onChangeConsumerName: (String?) -> Unit,
    consumerNamesSorted: List<String>,
    ownerUserName: String,
    selectedSection: StatsSection = StatsSection.OVERVIEW,
    onSelectSection: (StatsSection) -> Unit = {}
) {
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
                        text = if (statsModel.consumerName != null) {
                            i18n(
                                "stats_title_for_consumer",
                                replacements = mapOf("consumer" to statsModel.consumerName)
                            )
                        } else if (ownerUserName != "You") {
                            i18n(
                                "stats_title_for_consumer",
                                replacements = mapOf("consumer" to ownerUserName)
                            )
                        } else {
                            i18n("stats_title")
                        },
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )

                    var isConsumerSelectionExpanded by remember { mutableStateOf(false) }

                    val context = LocalContext.current

                    val currentConsumerName = statsModel.consumerName ?: ownerUserName

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
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = i18n("stats_consumer")
                            )
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
                                if (statsModel.consumerName == null) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = i18n("common_check"),
                                        modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
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
                                    if (statsModel.consumerName == consumerName) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = i18n("common_check"),
                                            modifier = Modifier.size(ButtonDefaults.IconSize)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
                StatsSectionTabs(
                    selectedSection = selectedSection,
                    onSelectSection = onSelectSection
                )
            }
        },
    ) { padding ->
        if (!statsModel.areThereAnyIngestions) {
            EmptyScreenDisclaimer(
                title = i18n("stats_empty_title"),
                description = i18n("stats_empty_description")
            )
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 8.dp)
                ) {
                    TimePickerOption.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = TimePickerOption.entries.size),
                            selected = statsModel.selectedOption.tabIndex == index,
                            onClick = { onTapOption(option) }
                        ) {
                            Text(option.displayText)
                        }
                    }
                }
                if (statsModel.statItems.isNotEmpty()) {
                    val isDarkTheme = isSystemInDarkTheme()
                    Column(modifier = Modifier.weight(1f)) {
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
                                        if (statsModel.isByIngestionTime) {
                                            "stats_ingestions_since"
                                        } else {
                                            "stats_experiences_since"
                                        },
                                        replacements = mapOf("date" to statsModel.startDateText)
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        top = 12.dp,
                                        end = 16.dp
                                    )
                                )
                                Text(
                                    text = i18n(
                                        if (statsModel.isByIngestionTime) {
                                            "stats_chart_by_ingestion_time"
                                        } else {
                                            "stats_substance_counted_once"
                                        }
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        bottom = 8.dp
                                    )
                                )
                                BarChart(
                                    buckets = statsModel.chartBuckets,
                                    startDateText = statsModel.startDateText
                                )
                            }
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = bottomBarOverlayPadding()
                        ) {
                            items(statsModel.statItems) { subStat ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = horizontalPadding,
                                            vertical = 4.dp
                                        )
                                        .clickable {
                                            navigateToSubstanceCompanion(
                                                subStat.substanceName,
                                                statsModel.consumerName
                                            )
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                horizontal = 14.dp,
                                                vertical = 12.dp
                                            )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .background(
                                                    color = subStat.color.getComposeColor(
                                                        isDarkTheme
                                                    ),
                                                    shape = CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = (
                                                    subStat.substanceRepo?.getDisplayName(
                                                        subStat.substanceName
                                                    )
                                                        ?: subStat.substanceName
                                                    ),
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            val experienceCountText =
                                                if (subStat.experienceCount == 1) {
                                                    i18n(
                                                        "stats_experience_count_one",
                                                        replacements = mapOf(
                                                            "count" to
                                                                subStat.experienceCount.toString()
                                                        )
                                                    )
                                                } else {
                                                    i18n(
                                                        "stats_experience_count_other",
                                                        replacements = mapOf(
                                                            "count" to
                                                                subStat.experienceCount.toString()
                                                        )
                                                    )
                                                }
                                            Text(
                                                text = experienceCountText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(horizontalAlignment = Alignment.End) {
                                            val cumulativeDose = subStat.totalDose
                                            val relativeTotal = subStat.relativeTotalDose
                                            when {
                                                relativeTotal != null && cumulativeDose != null -> {
                                                    if (cumulativeDose.isEstimate) {
                                                        if (cumulativeDose.estimatedDoseStandardDeviation !=
                                                            null
                                                        ) {
                                                            Text(
                                                                text = i18n(
                                                                    "stats_total_dose_relative_estimated_sd",
                                                                    replacements = mapOf(
                                                                        "dose" to
                                                                            cumulativeDose.dose.toReadableString(),
                                                                        "sd" to
                                                                            cumulativeDose.estimatedDoseStandardDeviation.toReadableString(),
                                                                        "units" to cumulativeDose.units,
                                                                        "relative" to
                                                                            relativeTotal.toReadableString()
                                                                    )
                                                                ),
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                        } else {
                                                            Text(
                                                                text = i18n(
                                                                    "stats_total_dose_relative_estimated",
                                                                    replacements = mapOf(
                                                                        "dose" to
                                                                            cumulativeDose.dose.toReadableString(),
                                                                        "units" to cumulativeDose.units,
                                                                        "relative" to
                                                                            relativeTotal.toReadableString()
                                                                    )
                                                                ),
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                        }
                                                    } else {
                                                        Text(
                                                            text = i18n(
                                                                "stats_total_dose_relative",
                                                                replacements = mapOf(
                                                                    "dose" to
                                                                        cumulativeDose.dose.toReadableString(),
                                                                    "units" to cumulativeDose.units,
                                                                    "relative" to
                                                                        relativeTotal.toReadableString()
                                                                )
                                                            ),
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                    }
                                                }
                                                relativeTotal != null -> {
                                                    Text(
                                                        text = i18n(
                                                            "stats_total_dose_relative_only",
                                                            replacements = mapOf(
                                                                "dose" to
                                                                    relativeTotal.toReadableString()
                                                            )
                                                        ),
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                                cumulativeDose != null -> {
                                                    if (cumulativeDose.isEstimate) {
                                                        if (cumulativeDose.estimatedDoseStandardDeviation !=
                                                            null
                                                        ) {
                                                            Text(
                                                                text = i18n(
                                                                    "stats_total_dose_estimated_with_sd",
                                                                    replacements = mapOf(
                                                                        "dose" to
                                                                            cumulativeDose.dose.toReadableString(),
                                                                        "sd" to
                                                                            cumulativeDose.estimatedDoseStandardDeviation.toReadableString(),
                                                                        "units" to cumulativeDose.units
                                                                    )
                                                                ),
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                        } else {
                                                            Text(
                                                                text = i18n(
                                                                    "stats_total_dose_estimated",
                                                                    replacements = mapOf(
                                                                        "dose" to
                                                                            cumulativeDose.dose.toReadableString(),
                                                                        "units" to cumulativeDose.units
                                                                    )
                                                                ),
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                        }
                                                    } else {
                                                        Text(
                                                            text = i18n(
                                                                "stats_total_dose",
                                                                replacements = mapOf(
                                                                    "dose" to
                                                                        cumulativeDose.dose.toReadableString(),
                                                                    "units" to cumulativeDose.units
                                                                )
                                                            ),
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                    }
                                                }
                                                else -> {
                                                    Text(
                                                        text = i18n("stats_total_dose_unknown"),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
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
                                                            .padding(
                                                                horizontal = 6.dp,
                                                                vertical = 2.dp
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    EmptyScreenDisclaimer(
                        title = i18n(
                            "stats_no_ingestions_since",
                            replacements = mapOf(
                                "period" to statsModel.selectedOption.longDisplayText
                            )
                        ),
                        description = i18n("stats_choose_longer_duration")
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyScreenDisclaimer(title: String, description: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
