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
import kotlinx.serialization.Serializable

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

data class TopLevelDestination(
    val graphRoute: Any,
    val startRoute: Any,
    val labelKey: String,
    val icon: ImageVector,
    val iconSelected: ImageVector
)

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
    val all = listOf(Journal, Stats, Substances, Safer, Settings)
}
