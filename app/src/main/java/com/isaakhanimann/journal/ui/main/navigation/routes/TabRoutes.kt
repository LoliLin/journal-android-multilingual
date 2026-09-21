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

package com.isaakhanimann.journal.ui.main.navigation.routes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

/**
 * One entry of the bottom navigation bar.
 *
 * [startRoute] is the root of the tab's back stack: it identifies the tab, drives which bar item is
 * selected and marks the destinations where the bar is shown. [labelKey] is the i18n key of the
 * label and doubles as the stable id used to persist the selected tab.
 */
data class TopLevelDestination(
    val startRoute: NavKey,
    val labelKey: String,
    val icon: ImageVector,
    val iconSelected: ImageVector
)

object TopLevelDestinations {
    val Stats = TopLevelDestination(
        startRoute = StatsRoute,
        labelKey = "stats",
        icon = Icons.Outlined.BarChart,
        iconSelected = Icons.Filled.BarChart
    )
    val Journal = TopLevelDestination(
        startRoute = JournalRoute,
        labelKey = "journal",
        icon = Icons.Outlined.Book,
        iconSelected = Icons.Filled.Book
    )
    val Substances = TopLevelDestination(
        startRoute = SubstancesRoute,
        labelKey = "substances",
        icon = Icons.Outlined.Medication,
        iconSelected = Icons.Filled.Medication
    )
    val Safer = TopLevelDestination(
        startRoute = SaferRoute,
        labelKey = "safer",
        icon = Icons.Outlined.HealthAndSafety,
        iconSelected = Icons.Filled.HealthAndSafety
    )
    val Settings = TopLevelDestination(
        startRoute = SettingsRoute,
        labelKey = "settings",
        icon = Icons.Outlined.Settings,
        iconSelected = Icons.Filled.Settings
    )

    /** The five bottom-bar tabs, in the order they appear in the navigation bar. */
    val all = listOf(Stats, Journal, Substances, Safer, Settings)

    /** The tab previously persisted under [labelKey], or the journal tab when it is unknown. */
    fun forLabelKey(labelKey: String): TopLevelDestination =
        all.firstOrNull { it.labelKey == labelKey } ?: Journal
}
