/*
 * Copyright (c) 2023. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.main.navigation.graphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import androidx.navigation.navDeepLink
import com.isaakhanimann.journal.ui.main.navigation.composableWithTransitions
import com.isaakhanimann.journal.ui.main.navigation.routes.CombinationSettingsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CustomUnitArchiveRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CustomUnitsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DEEP_LINK_SETTINGS
import com.isaakhanimann.journal.ui.main.navigation.routes.DonateRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditCustomUnitRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ExtensionPackRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.FAQRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.IconPickerRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.PreferencesRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SettingsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SettingsTab
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceColorsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToAddCustomUnits
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToComboSettings
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToCustomUnitArchive
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToCustomUnits
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToDonate
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToEditCustomUnit
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToExtensionPack
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToFAQ
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToIconPicker
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToPreferences
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToSubstanceColors
import com.isaakhanimann.journal.ui.tabs.settings.DonateScreen
import com.isaakhanimann.journal.ui.tabs.settings.ExtensionPackScreen
import com.isaakhanimann.journal.ui.tabs.settings.FAQScreen
import com.isaakhanimann.journal.ui.tabs.settings.IconPickerScreen
import com.isaakhanimann.journal.ui.tabs.settings.PreferencesScreen
import com.isaakhanimann.journal.ui.tabs.settings.SettingsScreen
import com.isaakhanimann.journal.ui.tabs.settings.colors.SubstanceColorsScreen
import com.isaakhanimann.journal.ui.tabs.settings.combinations.CombinationSettingsScreen
import com.isaakhanimann.journal.ui.tabs.settings.customunits.CustomUnitsScreen
import com.isaakhanimann.journal.ui.tabs.settings.customunits.archive.CustomUnitArchiveScreen
import com.isaakhanimann.journal.ui.tabs.settings.customunits.edit.EditCustomUnitScreen

fun NavGraphBuilder.settingsGraph(navController: NavController) {
    navigation<SettingsTab>(
        startDestination = SettingsRoute
    ) {
        composableWithTransitions<SettingsRoute>(
            deepLinks = listOf(navDeepLink<SettingsRoute>(basePath = DEEP_LINK_SETTINGS))
        ) {
            SettingsScreen(
                navigateToFAQ = navController::navigateToFAQ,
                navigateToComboSettings = navController::navigateToComboSettings,
                navigateToSubstanceColors = navController::navigateToSubstanceColors,
                navigateToCustomUnits = navController::navigateToCustomUnits,
                navigateToDonate = navController::navigateToDonate,
                navigateToExtensionPack = navController::navigateToExtensionPack,
                navigateToIconPicker = navController::navigateToIconPicker,
                navigateToPreferences = navController::navigateToPreferences
            )
        }
        composableWithTransitions<FAQRoute> { FAQScreen() }
        composableWithTransitions<DonateRoute> { DonateScreen() }
        composableWithTransitions<PreferencesRoute> {
            PreferencesScreen(
                navigateBack = navController::popBackStack,
                navigateToIconPicker = navController::navigateToIconPicker
            )
        }
        composableWithTransitions<IconPickerRoute> {
            IconPickerScreen()
        }
        composableWithTransitions<ExtensionPackRoute> {
            ExtensionPackScreen()
        }
        composableWithTransitions<CombinationSettingsRoute> {
            CombinationSettingsScreen()
        }
        composableWithTransitions<SubstanceColorsRoute> {
            SubstanceColorsScreen()
        }
        composableWithTransitions<CustomUnitArchiveRoute> {
            CustomUnitArchiveScreen(
                navigateToEditCustomUnit = navController::navigateToEditCustomUnit
            )
        }
        composableWithTransitions<CustomUnitsRoute> {
            CustomUnitsScreen(
                navigateToAddCustomUnit = navController::navigateToAddCustomUnits,
                navigateToEditCustomUnit = navController::navigateToEditCustomUnit,
                navigateToCustomUnitArchive = navController::navigateToCustomUnitArchive
            )
        }
        composableWithTransitions<EditCustomUnitRoute> {
            EditCustomUnitScreen(navigateBack = navController::popBackStack)
        }
        addCustomUnitGraph(navController)
    }
}
