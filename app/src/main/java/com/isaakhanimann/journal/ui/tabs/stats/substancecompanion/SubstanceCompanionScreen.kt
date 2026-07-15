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

import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
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
import androidx.compose.ui.unit.times 
import androidx.compose.ui.platform.LocalContext

@Composable
fun SubstanceCompanionScreen(
    navigateToCategoryScreen: (categoryName: String) -> Unit,
    navigateToSubstanceScreen: (substanceName: String) -> Unit,
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
            navigateToCategoryScreen = navigateToCategoryScreen,
            navigateToSubstanceScreen = navigateToSubstanceScreen,
            
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

                        val context = LocalContext.current

                        ToleranceSection(

                            tolerance = tolerance,

                            crossTolerances = crossTolerances,

                            isSubstance = substanceRepo::isSubstance,
                            isCategory = substanceRepo::isCategory
                            getSubstanceDisplayName = substanceRepo::getSubstanceDisplayName,
                            getCategoryDisplayName = { name ->
                                substanceRepo.getCategory(name).getLocalizedName(context)
                            },
                            navToCategory = navigateToCategoryScreen,
                            navToSubstance = navigateToSubstanceScreen
                        )

                    }

                    Spacer(Modifier.height(12.dp))

                }

                CardWithTitle(title = i18n("substance_activity_title"), modifier = Modifier.fillMaxWidth()) {
                    ActivityGrid(
                        ingestionBursts = ingestionBursts,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

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
            append(ingestionAndCustomUnit.getDoseDescription(androidx.compose.ui.platform.LocalContext.current))
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



private data class DailyCount(

    val date: java.time.LocalDate,

    val count: Int

)


@Composable
fun ActivityGrid(
    ingestionBursts: List<IngestionsBurst>,
    modifier: Modifier = Modifier
) {
    val now = java.time.LocalDate.now()
    val oneYearAgo = now.minusDays(364)

    // Map ingestion counts by date (past 12 months)
    val dateCountMap = androidx.compose.runtime.remember(ingestionBursts) {
        val map = mutableMapOf<java.time.LocalDate, Int>()
        val cutoff = oneYearAgo.minusDays(7)
        for (burst in ingestionBursts) {
            for (item in burst.ingestions) {
                val date = item.ingestion.time
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                if (date >= cutoff) {
                    map[date] = (map[date] ?: 0) + 1
                }
            }
        }
        map
    }

    val maxCount = androidx.compose.runtime.remember(dateCountMap) {
        (dateCountMap.values.maxOrNull() ?: 1).coerceAtLeast(1)
    }

    // Build weeks FROM this week backward TO one year ago
    val weeks = androidx.compose.runtime.remember(now, dateCountMap) {
        val result = mutableListOf<List<DailyCount>>()
        // Start from Monday of this week
        var monday = now
        while (monday.dayOfWeek != java.time.DayOfWeek.MONDAY) {
            monday = monday.minusDays(1)
        }
        // Go backward week by week
        var current = monday
        val end = oneYearAgo.minusDays(7)
        while (current > end) {
            val week = (0..6).map { offset ->
                val day = current.plusDays(offset.toLong())
                DailyCount(day, dateCountMap[day] ?: 0)
            }
            result.add(week)
            current = current.minusDays(7)
        }
        result
    }

    val cellSize = 12.dp
    val gap = 3.dp
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val isDark = isSystemInDarkTheme()

    Column(modifier = modifier.padding(4.dp)) {
        // Month labels
        Row(modifier = Modifier.fillMaxWidth().padding(start = 28.dp)) {
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
                        modifier = Modifier.padding(start = if (lastMonth == -1) 0.dp
                            else (cellSize + gap) * (col - firstColIndexOfMonth(weeks, lastMonth)))
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
                    Text(text = label, fontSize = 9.sp, color = textColor, modifier = Modifier.width(26.dp))
                } else {
                    Spacer(Modifier.width(26.dp))
                }
                weeks.forEach { week ->
                    if (row < week.size) {
                        val cell = week[row]
                        val color = if (cell.date <= now) {
                            if (cell.count == 0) {
                                if (isDark) Color(0xFF2D2D2D) else Color(0xFFEBEDF0)
                            } else {
                                when {
                                    cell.count == 1 -> if (isDark) Color(0xFF1E4529) else Color(0xFF9BE9A8)
                                    cell.count == 2 -> if (isDark) Color(0xFF195C2E) else Color(0xFF40C463)
                                    cell.count >= 5 -> if (isDark) Color(0xFF0E630F) else Color(0xFF196127)
                                    else -> if (isDark) Color(0xFF0E4429) else Color(0xFF216E39)
                                }
                            }
                        } else {
                            Color.Transparent
                        }
                        Box(modifier = Modifier.size(cellSize).background(color, RoundedCornerShape(2.dp)))
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
