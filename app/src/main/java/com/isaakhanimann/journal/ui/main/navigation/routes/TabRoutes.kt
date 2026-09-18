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
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable

/**
 * Type-safe route of the root graph of each bottom-bar tab.
 *
 * The graphs themselves carry no arguments; the destination each graph starts on is declared in the
 * tab's own screen file and referenced by [TopLevelDestination.startRoute].
 */
@Serializable
data object JournalTab

@Serializable
data object StatsTab

@Serializable
data object SubstancesTab

@Serializable
data object SaferTab

@Serializable
data object SettingsTab

/**
 * One entry of the bottom navigation bar.
 *
 * [graphRoute] identifies the tab's nested graph and drives which tab is highlighted; [startRoute]
 * identifies the graph's start destination and drives both "am I on a tab root" (the bar is only
 * shown there) and re-tapping the selected tab (pop back to that root).
 *
 * Both are typed as [Any] because the bar iterates a homogeneous list, while the route class is
 * still recovered at runtime by [NavDestination.hasRouteOf].
 */
data class TopLevelDestination(
    val graphRoute: Any,
    val startRoute: Any,
    val labelKey: String,
    val icon: ImageVector,
    val iconSelected: ImageVector
)

/** The five bottom-bar tabs, in the order they appear in the navigation bar. */
object TopLevelDestinations {

    val Stats = TopLevelDestination(
        graphRoute = StatsTab,
        startRoute = StatsRoute,
        labelKey = "stats",
        icon = Icons.Outlined.BarChart,
        iconSelected = Icons.Filled.BarChart
    )

    val Journal = TopLevelDestination(
        graphRoute = JournalTab,
        startRoute = JournalRoute,
        labelKey = "journal",
        icon = Icons.Outlined.Book,
        iconSelected = Icons.Filled.Book
    )

    val Substances = TopLevelDestination(
        graphRoute = SubstancesTab,
        startRoute = SubstancesRoute,
        labelKey = "substances",
        icon = Icons.Outlined.Medication,
        iconSelected = Icons.Filled.Medication
    )

    val Safer = TopLevelDestination(
        graphRoute = SaferTab,
        startRoute = SaferRoute,
        labelKey = "safer",
        icon = Icons.Outlined.HealthAndSafety,
        iconSelected = Icons.Filled.HealthAndSafety
    )

    val Settings = TopLevelDestination(
        graphRoute = SettingsTab,
        startRoute = SettingsRoute,
        labelKey = "settings",
        icon = Icons.Outlined.Settings,
        iconSelected = Icons.Filled.Settings
    )

    val all = listOf(Stats, Journal, Substances, Safer, Settings)
}

@Suppress("UNCHECKED_CAST")
private fun NavDestination.hasRouteOf(route: Any): Boolean =
    hasRoute(route::class as KClass<Any>)

/**
 * True when this destination is the root of a bottom-bar tab.
 *
 * The bottom navigation bar is only shown on these destinations; every nested/detail screen hides
 * it. This replaces the previously hand-maintained set of tab-root route strings: the hierarchy is
 * walked instead, so the check also works for destinations inside nested graphs.
 */
fun NavDestination.isTopLevelDestinationRoot(): Boolean =
    TopLevelDestinations.all.any { destination ->
        hierarchy.any { it.hasRouteOf(destination.startRoute) }
    }

/** The tab this destination belongs to, or null when it is outside every tab graph. */
fun NavDestination.topLevelDestinationOrNull(): TopLevelDestination? =
    TopLevelDestinations.all.firstOrNull { destination ->
        hierarchy.any { it.hasRouteOf(destination.graphRoute) }
    }
