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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.localization.i18nOrDefault
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.toReadableString
import com.isaakhanimann.journal.ui.tabs.settings.AvatarUtil
import com.isaakhanimann.journal.ui.theme.JournalTheme
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import com.isaakhanimann.journal.ui.utils.administrationRouteKey
import com.isaakhanimann.journal.ui.utils.renderComposeViewToBitmap
import com.isaakhanimann.journal.ui.utils.shareBitmap
import kotlinx.coroutines.launch

enum class StatsSection { OVERVIEW, ANALYSIS }

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    navigateToSubstanceCompanion: (substanceName: String, consumerName: String?) -> Unit
) {
    var selectedSection by rememberSaveable { mutableStateOf(StatsSection.OVERVIEW) }
    // The secondary navigation lives in each section's TopAppBar bottomBar
    // (Overview keeps the original statistics incl. the time-capsule selector;
    // Analysis is the multi-substance custom-range page).
    when (selectedSection) {
        StatsSection.OVERVIEW -> StatsScreen(
            navigateToSubstanceCompanion = navigateToSubstanceCompanion,
            onTapOption = viewModel::onTapOption,
            statsModel = viewModel.statsModelFlow.collectAsState().value,
            onChangeConsumerName = viewModel::onChangeConsumer,
            consumerNamesSorted = viewModel.sortedConsumerNamesFlow.collectAsState().value,
            ownerUserName = viewModel.ownerUserNameFlow.collectAsState().value ?: "You",
            selectedSection = selectedSection,
            onSelectSection = { selectedSection = it }
        )
        StatsSection.ANALYSIS -> StatsAnalysisScreen(
            navigateToSubstanceCompanion = navigateToSubstanceCompanion,
            selectedSection = selectedSection,
            onSelectSection = { selectedSection = it }
        )
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
    val currentView = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    var isSharing by remember { mutableStateOf(false) }
    val widthPx = (LocalConfiguration.current.screenWidthDp * LocalDensity.current.density).toInt()
    val shareStatsContent: @Composable () -> Unit = {
        JournalTheme {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text(
                        text = i18n(
                            "stats_experiences_since",
                            replacements = mapOf("date" to statsModel.startDateText)
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 10.dp, top = 5.dp)
                    )
                    Text(
                        text = i18n("stats_substance_counted_once"),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
                    )
                    BarChart(
                        buckets = statsModel.chartBuckets,
                        startDateText = statsModel.startDateText
                    )
                }
            }
        }
    }
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                title = {
                    Text(

                        if (statsModel.consumerName != null) {
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
                        }

                    )
                },
                actions = {
                    var isConsumerSelectionExpanded by remember { mutableStateOf(false) }

                    val context = LocalContext.current

                    if (statsModel.statItems.isNotEmpty()) {
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
                                                    widthPx = widthPx,
                                                    lifecycleView = currentView,
                                                    content = shareStatsContent,
                                                    postLayoutDelayMs = 300L
                                                )
                                                shareBitmap(context, bitmap)
                                            }
                                        } catch (e: Exception) {
                                            Log.e("StatsScreen", "error", e)
                                            Toast.makeText(
                                                context,
                                                "${e.localizedMessage}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } finally {
                                            isSharing = false
                                        }
                                    }
                                }
                            },
                            enabled = !isSharing
                        ) {
                            Icon(
                                Icons.Outlined.Share,
                                contentDescription = i18n("common_share"),
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                        }
                    }

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

                )
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
        },
    ) { padding ->
        if (!statsModel.areThereAnyIngestions) {
            EmptyScreenDisclaimer(
                title = i18n("stats_empty_title"),
                description = i18n("stats_empty_description")
            )
        } else {
            Column(modifier = Modifier.padding(padding)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
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
                    Column {
                        Text(
                            text = i18n(
                                "stats_experiences_since",
                                replacements = mapOf("date" to statsModel.startDateText)
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 10.dp, top = 5.dp)
                        )
                        Text(
                            text = i18n("stats_substance_counted_once"),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(
                                start = 10.dp,
                                bottom = 10.dp
                            )
                        )
                        BarChart(
                            buckets = statsModel.chartBuckets,
                            startDateText = statsModel.startDateText
                        )
                        HorizontalDivider()
                        LazyColumn {
                            items(statsModel.statItems) { subStat ->
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(intrinsicSize = IntrinsicSize.Min)
                                            .clickable {
                                                navigateToSubstanceCompanion(
                                                    subStat.substanceName,
                                                    statsModel.consumerName
                                                )
                                            }
                                            .padding(
                                                horizontal = horizontalPadding,
                                                vertical = 5.dp
                                            )
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(3.dp),
                                            color = subStat.color.getComposeColor(
                                                isDarkTheme
                                            ),
                                            modifier = Modifier
                                                .width(11.dp)
                                                .fillMaxHeight()
                                        ) {}
                                        Column {
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
                                                text = experienceCountText
                                            )
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Column(horizontalAlignment = Alignment.End) {
                                            val cumulativeDose = subStat.totalDose
                                            if (cumulativeDose != null) {
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
                                                            )
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
                                                            )
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
                                                        )
                                                    )
                                                }
                                            } else {
                                                Text(text = i18n("stats_total_dose_unknown"))
                                            }
                                            subStat.routeCounts.forEach {
                                                val routeName = i18nOrDefault(
                                                    administrationRouteKey(it.administrationRoute),
                                                    it.administrationRoute.displayText
                                                ).lowercase()
                                                Text(
                                                    text = "$routeName ${it.count}x "
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider()
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
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = description,
                textAlign = TextAlign.Center
            )
        }
    }
}
