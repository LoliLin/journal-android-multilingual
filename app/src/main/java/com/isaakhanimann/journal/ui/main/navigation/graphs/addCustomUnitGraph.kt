/*
 * Copyright (c) 2024. Isaak Hanimann.
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
import androidx.navigation.toRoute
import com.isaakhanimann.journal.ui.main.navigation.composableWithTransitions
import com.isaakhanimann.journal.ui.main.navigation.routes.AddCustomUnitsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AddCustomUnitsSearchSubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseRouteOfAddCustomUnitRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.FinishAddCustomUnitRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.dismissAddCustomUnits
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToChooseRouteOfAddCustomUnit
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToFinishAddCustomUnit
import com.isaakhanimann.journal.ui.tabs.settings.customunits.add.ChooseRouteDuringAddCustomUnitScreen
import com.isaakhanimann.journal.ui.tabs.settings.customunits.add.ChooseSubstanceScreen
import com.isaakhanimann.journal.ui.tabs.settings.customunits.add.FinishAddCustomUnitScreen

fun NavGraphBuilder.addCustomUnitGraph(navController: NavController) {
    navigation<AddCustomUnitsRoute>(
        startDestination = AddCustomUnitsSearchSubstanceRoute
    ) {
        composableWithTransitions<AddCustomUnitsSearchSubstanceRoute> {
            ChooseSubstanceScreen(
                navigateToChooseRoute = { substanceName ->
                    navController.navigateToChooseRouteOfAddCustomUnit(substanceName)
                }
            )
        }
        composableWithTransitions<ChooseRouteOfAddCustomUnitRoute> { backStackEntry ->
            val substanceName =
                backStackEntry.toRoute<ChooseRouteOfAddCustomUnitRoute>().substanceName
            ChooseRouteDuringAddCustomUnitScreen(
                onRouteChosen = { administrationRoute ->
                    navController.navigateToFinishAddCustomUnit(
                        substanceName = substanceName,
                        administrationRoute = administrationRoute
                    )
                }
            )
        }
        composableWithTransitions<FinishAddCustomUnitRoute> {
            FinishAddCustomUnitScreen(
                dismissAddCustomUnit = { navController.dismissAddCustomUnits() }
            )
        }
    }
}
