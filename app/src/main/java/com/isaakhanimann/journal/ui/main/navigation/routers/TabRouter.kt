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

package com.isaakhanimann.journal.ui.main.navigation.routers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class TabRouter(
    val route: String,
    val childRoute: String,
    val labelKey: String,
    val icon: ImageVector
) {
    object Journal : TabRouter(
        route = "journalTab",
        childRoute = NoArgumentRouter.JournalRouter.route,
        labelKey = "journal",
        icon = Icons.Outlined.Book
    )

    object Statistics : TabRouter(
        route = "statisticsTab",
        childRoute = NoArgumentRouter.StatsRouter.route,
        labelKey = "stats",
        icon = Icons.Outlined.BarChart
    )

    object Substances : TabRouter(
        route = "substancesTab",
        childRoute = NoArgumentRouter.SubstancesRouter.route,
        labelKey = "substances",
        icon = Icons.Outlined.Medication
    )

    object SaferUse : TabRouter(
        route = "saferTab",
        childRoute = NoArgumentRouter.SaferRouter.route,
        labelKey = "safer",
        icon = Icons.Outlined.HealthAndSafety
    )

    object Settings : TabRouter(
        route = "settingsTab",
        childRoute = NoArgumentRouter.SettingsRouter.route,
        labelKey = "settings",
        icon = Icons.Outlined.Settings
    )
}