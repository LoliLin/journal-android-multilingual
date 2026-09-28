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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion
import com.isaakhanimann.journal.data.substances.classes.Tolerance
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.localization.i18nOrDefault
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.CardWithTitle
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.ToleranceSection
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import com.isaakhanimann.journal.ui.utils.administrationRouteKey
import com.isaakhanimann.journal.ui.utils.getDateWithWeekdayText
import com.isaakhanimann.journal.ui.utils.getTimeText

@Composable
fun SubstanceCompanionScreen(
    navigateToCategoryScreen: (categoryName: String) -> Unit,
    navigateToSubstanceScreen: (substanceName: String) -> Unit,
    navigateToIngestion: (ingestionId: Int) -> Unit,
    viewModel: SubstanceCompanionViewModel
) {
    val companion = viewModel.thisCompanionFlow.collectAsState().value
    if (companion == null) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {}
    } else {
        SubstanceCompanionScreen(
            navigateToCategoryScreen = navigateToCategoryScreen,
            navigateToSubstanceScreen = navigateToSubstanceScreen,
            navigateToIngestion = navigateToIngestion,
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
    navigateToCategoryScreen: (categoryName: String) -> Unit,
    navigateToSubstanceScreen: (substanceName: String) -> Unit,
    navigateToIngestion: (ingestionId: Int) -> Unit,
    substanceCompanion: SubstanceCompanion,
    ingestionBursts: List<IngestionsBurst>,
    tolerance: Tolerance?,
    crossTolerances: List<String>,
    consumerName: String? = null,
    substanceRepo: SubstanceRepository
) {
    val isDarkTheme = isSystemInDarkTheme()
    val substanceColor = substanceCompanion.color.getComposeColor(isDarkTheme)
    val displayName = substanceRepo.getDisplayName(substanceCompanion.substanceName)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(displayName, style = MaterialTheme.typography.titleLarge)
                        if (consumerName != null) {
                            Text(
                                text = consumerName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navigateToSubstanceScreen(substanceCompanion.substanceName) }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = i18n("substance_more_info")
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Substance Hero Card ───────────────────────────────────────────
            item {
                SubstanceHeroCard(
                    substanceName = substanceCompanion.substanceName,
                    displayName = displayName,
                    consumerName = consumerName,
                    accentColor = substanceColor,
                    experienceCount = ingestionBursts.size,
                    ingestionCount = ingestionBursts.sumOf { it.ingestions.size },
                    latestDateText = ingestionBursts.firstOrNull()?.experience?.sortDate?.getDateWithWeekdayText(),
                    onWikiClick = { navigateToSubstanceScreen(substanceCompanion.substanceName) }
                )
                Spacer(Modifier.height(8.dp))
            }

            // ── Tolerance Section ─────────────────────────────────────────────
            item {
                if (tolerance != null || crossTolerances.isNotEmpty()) {
                    CardWithTitle(
                        title = i18n("substance_tolerance_title"),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val context = LocalContext.current
                        ToleranceSection(
                            tolerance = tolerance,
                            crossTolerances = crossTolerances,
                            isSubstance = substanceRepo::isSubstance,
                            isCategory = substanceRepo::isCategory,
                            getSubstanceDisplayName = substanceRepo::getSubstanceDisplayName,
                            getCategoryDisplayName = { name ->
                                substanceRepo.getCategory(name)?.getLocalizedName(context) ?: name
                            },
                            navToCategory = navigateToCategoryScreen,
                            navToSubstance = navigateToSubstanceScreen
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── Activity Heatmap Grid ─────────────────────────────────────────
            item {
                CardWithTitle(
                    title = i18n("substance_activity_title"),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ActivityGrid(
                        ingestionBursts = ingestionBursts,
                        modifier = Modifier.fillMaxWidth(),
                        accentColor = substanceColor
                    )
                }
                Spacer(Modifier.height(16.dp))

                // Modern "Now" timeline start badge
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = i18n("time_now_label"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }
            }

            // ── Timeline Experiences & Ingestions ─────────────────────────────
            items(ingestionBursts, key = { it.experience.id }) { burst ->
                TimeArrowUp(timeText = burst.timeUntil)

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = burst.experience.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = burst.experience.sortDate.getDateWithWeekdayText(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )

                        burst.ingestions.forEachIndexed { index, ingestion ->
                            IngestionRow(
                                ingestionAndCustomUnit = ingestion,
                                onClick = {
                                    navigateToIngestion(ingestion.ingestion.id)
                                }
                            )
                            if (index < burst.ingestions.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
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
// Hero Card with key metrics
// ---------------------------------------------------------------------------

@Composable
private fun SubstanceHeroCard(
    substanceName: String,
    displayName: String,
    consumerName: String?,
    accentColor: Color,
    experienceCount: Int,
    ingestionCount: Int,
    latestDateText: String?,
    onWikiClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(accentColor, CircleShape)
                    )
                    Column {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (displayName != substanceName) {
                            Text(
                                text = substanceName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (consumerName != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = consumerName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricColumn(
                    value = experienceCount.toString(),
                    label = i18n("stats_report_experiences")
                )
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                )
                MetricColumn(
                    value = ingestionCount.toString(),
                    label = i18n("stats_analysis_ingestions")
                )
                if (latestDateText != null) {
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    )
                    MetricColumn(
                        value = latestDateText,
                        label = i18nOrDefault("stats_latest", "Latest")
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------------------------
// Ingestion row with clean route badge and chevron
// ---------------------------------------------------------------------------

@Composable
fun IngestionRow(
    ingestionAndCustomUnit: IngestionsBurst.IngestionAndCustomUnit,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val routeName = i18nOrDefault(
            administrationRouteKey(ingestionAndCustomUnit.ingestion.administrationRoute),
            ingestionAndCustomUnit.ingestion.administrationRoute.displayText
        ).lowercase()

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = routeName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                val doseText = ingestionAndCustomUnit.getDoseDescription(
                    LocalContext.current
                )
                Text(
                    text = doseText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            ingestionAndCustomUnit.customUnitDose?.calculatedDoseDescription?.let { calculated ->
                Text(
                    text = "= $calculated $routeName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = ingestionAndCustomUnit.ingestion.time.getTimeText(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Activity Heatmap Grid with substance accent color
// ---------------------------------------------------------------------------

private data class DailyCount(val date: java.time.LocalDate, val value: Double)

@Composable
fun ActivityGrid(
    ingestionBursts: List<IngestionsBurst>,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val now = java.time.LocalDate.now()
    val oneYearAgo = now.minusDays(364)

    val dateDoseMap = remember(ingestionBursts) {
        val map = mutableMapOf<java.time.LocalDate, Double>()
        val cutoff = oneYearAgo.minusDays(7)
        for (burst in ingestionBursts) {
            for (item in burst.ingestions) {
                val date = item.ingestion.time
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                if (date >= cutoff) {
                    map[date] = (map[date] ?: 0.0) + (item.ingestion.dose ?: 0.0)
                }
            }
        }
        map
    }

    val maxDose = remember(dateDoseMap) {
        (dateDoseMap.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    }

    val weeks = remember(now, dateDoseMap) {
        val result = mutableListOf<List<DailyCount>>()
        var monday = now
        while (monday.dayOfWeek != java.time.DayOfWeek.MONDAY) {
            monday = monday.minusDays(1)
        }
        var current = monday
        val end = oneYearAgo.minusDays(7)
        while (current > end) {
            val week = (0..6).map { offset ->
                val day = current.plusDays(offset.toLong())
                DailyCount(day, dateDoseMap[day] ?: 0.0)
            }
            result.add(week)
            current = current.minusDays(7)
        }
        result
    }

    val cellSize = 12.dp
    val gap = 3.dp
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val emptyCellColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f)

    Column(modifier = modifier.padding(vertical = 4.dp)) {
        // Month labels
        Row(modifier = Modifier.fillMaxWidth().padding(start = 28.dp, bottom = 4.dp)) {
            var lastMonth = -1
            weeks.forEachIndexed { col, week ->
                val mid = week[3]
                val month = mid.date.monthValue
                if (month != lastMonth) {
                    Text(
                        text = java.time.format.DateTimeFormatter.ofPattern("MMM")
                            .withLocale(java.util.Locale.US).format(mid.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(
                            start = if (lastMonth == -1) {
                                0.dp
                            } else {
                                (cellSize + gap) * (col - firstColIndexOfMonth(weeks, lastMonth))
                            }
                        )
                    )
                    lastMonth = month
                }
            }
        }

        // Grid rows
        val dayAbbr = listOf("Mon", "", "Wed", "", "Fri", "", "")
        dayAbbr.forEachIndexed { row, label ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (label.isNotEmpty()) {
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        color = textColor,
                        modifier = Modifier.width(26.dp)
                    )
                } else {
                    Spacer(Modifier.width(26.dp))
                }
                weeks.forEach { week ->
                    if (row < week.size) {
                        val cell = week[row]
                        val color = if (cell.date <= now) {
                            if (cell.value <= 0.0) {
                                emptyCellColor
                            } else {
                                accentColor.copy(
                                    alpha = (0.25f + 0.75f * (cell.value / maxDose).toFloat())
                                        .coerceIn(0.25f, 1f)
                                )
                            }
                        } else {
                            Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(color, RoundedCornerShape(3.dp))
                        )
                        Spacer(Modifier.width(gap))
                    }
                }
            }
            Spacer(Modifier.height(gap))
        }
    }
}

private fun firstColIndexOfMonth(weeks: List<List<DailyCount>>, targetMonth: Int): Int {
    weeks.forEachIndexed { i, week ->
        if (week[3].date.monthValue == targetMonth) return i
    }
    return 0
}
