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

package com.isaakhanimann.journal.ui.main.navigation.graphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.isaakhanimann.journal.ui.main.navigation.composableWithTransitions
import com.isaakhanimann.journal.ui.main.navigation.routes.AddIngestionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AddIngestionSearchRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CheckInteractionsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CheckSaferUseRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseDoseCustomUnitRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseDoseRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseRouteOfAddIngestionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseTimeRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CustomChooseDoseRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CustomChooseRouteRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DEEP_LINK_ADD_INGESTION
import com.isaakhanimann.journal.ui.main.navigation.routes.DEEP_LINK_CHOOSE_ROUTE
import com.isaakhanimann.journal.ui.main.navigation.routes.dismissAddIngestionScreens
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToAddCustomSubstance
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToAdministrationRouteExplanation
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToCheckInteractions
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToCheckSaferUse
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToChooseCustomRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToChooseDose
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToChooseDoseCustom
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToChooseDoseCustomUnit
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToChooseRouteOfAddIngestion
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToChooseTime
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToSaferSniffingOnJournalTab
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToURLInJournalTab
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToVolumetricDosingOnJournalTab
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.dose.ChooseDoseScreen
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.dose.customsubstance.CustomChooseDose
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.dose.customunit.ChooseDoseCustomUnitScreen
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.interactions.CheckInteractionsScreen
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.route.ChooseRouteScreen
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.route.CustomSubstanceChooseRouteScreen
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.saferuse.CheckSaferUseScreen
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.search.AddIngestionSearchScreen
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.time.FinishIngestionScreen

fun NavGraphBuilder.addIngestionGraph(navController: NavController) {
    navigation<AddIngestionRoute>(
        startDestination = AddIngestionSearchRoute
    ) {
        composableWithTransitions<AddIngestionSearchRoute>(
            deepLinks = listOf(
                navDeepLink<AddIngestionSearchRoute>(basePath = DEEP_LINK_ADD_INGESTION)
            )
        ) {
            AddIngestionSearchScreen(
                navigateToCheckInteractions = {
                    navController.navigateToCheckInteractions(substanceName = it)
                },
                navigateToCheckSaferUse = {
                    navController.navigateToCheckSaferUse(substanceName = it)
                },
                navigateToCustomSubstanceChooseRoute = navController::navigateToChooseCustomRoute,
                navigateToChooseTime = {
                        substanceName,
                        administrationRoute,
                        dose,
                        units,
                        isEstimate,
                        estimatedDoseStandardDeviation,
                        customUnitId,
                        releaseForm
                    ->
                    navController.navigateToChooseTime(
                        administrationRoute = administrationRoute,
                        isEstimate = isEstimate,
                        units = units,
                        dose = dose,
                        estimatedDoseStandardDeviation = estimatedDoseStandardDeviation,
                        substanceName = substanceName,
                        customUnitId = customUnitId,
                        releaseForm = releaseForm
                    )
                },
                navigateToCustomDose = { customSubstanceId, administrationRoute ->
                    navController.navigateToChooseDoseCustom(customSubstanceId, administrationRoute)
                },
                navigateToDose = { substanceName, administrationRoute ->
                    navController.navigateToChooseDose(substanceName, administrationRoute)
                },
                navigateToChooseRoute = { substanceName ->
                    navController.navigateToChooseRouteOfAddIngestion(substanceName)
                },
                navigateToAddCustomSubstanceScreen = navController::navigateToAddCustomSubstance,
                navigateToCustomUnitChooseDose = navController::navigateToChooseDoseCustomUnit
            )
        }
        composableWithTransitions<CheckInteractionsRoute> { backStackEntry ->
            val substanceName =
                backStackEntry.toRoute<CheckInteractionsRoute>().substanceName
            CheckInteractionsScreen(
                navigateToNext = {
                    navController.navigateToChooseRouteOfAddIngestion(substanceName = substanceName)
                },
                navigateToURL = navController::navigateToURLInJournalTab
            )
        }
        composableWithTransitions<CheckSaferUseRoute> { backStackEntry ->
            val substanceName = backStackEntry.toRoute<CheckSaferUseRoute>().substanceName
            CheckSaferUseScreen(
                navigateToNext = {
                    navController.navigateToCheckInteractions(substanceName = substanceName)
                }
            )
        }
        composableWithTransitions<ChooseDoseCustomUnitRoute> {
            ChooseDoseCustomUnitScreen(navigateToChooseTimeAndMaybeColor = {
                    administrationRoute,
                    units,
                    isEstimate,
                    dose,
                    estimatedDoseStandardDeviation,
                    substanceName,
                    customUnitId
                ->
                navController.navigateToChooseTime(
                    administrationRoute = administrationRoute,
                    isEstimate = isEstimate,
                    units = units,
                    dose = dose,
                    estimatedDoseStandardDeviation = estimatedDoseStandardDeviation,
                    substanceName = substanceName,
                    customUnitId = customUnitId
                )
            })
        }
        composableWithTransitions<ChooseRouteOfAddIngestionRoute>(
            deepLinks = listOf(
                navDeepLink<ChooseRouteOfAddIngestionRoute>(basePath = DEEP_LINK_CHOOSE_ROUTE)
            )
        ) { backStackEntry ->
            val substanceName =
                backStackEntry.toRoute<ChooseRouteOfAddIngestionRoute>().substanceName
            ChooseRouteScreen(
                navigateToChooseDose = { administrationRoute ->
                    navController.navigateToChooseDose(
                        substanceName = substanceName,
                        administrationRoute = administrationRoute
                    )
                },
                navigateToRouteExplanationScreen = navController::navigateToAdministrationRouteExplanation,
                navigateToURL = navController::navigateToURLInJournalTab
            )
        }
        composableWithTransitions<CustomChooseRouteRoute> { backStackEntry ->
            val customSubstanceId =
                backStackEntry.toRoute<CustomChooseRouteRoute>().customSubstanceId
            CustomSubstanceChooseRouteScreen(
                onRouteTap = { administrationRoute ->
                    navController.navigateToChooseDoseCustom(
                        customSubstanceId = customSubstanceId,
                        administrationRoute = administrationRoute
                    )
                }
            )
        }
        composableWithTransitions<CustomChooseDoseRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<CustomChooseDoseRoute>()
            CustomChooseDose(
                navigateToChooseTimeAndMaybeColor = {
                        units,
                        isEstimate,
                        dose,
                        estimatedDoseStandardDeviation
                    ->
                    navController.navigateToChooseTime(
                        administrationRoute = route.administrationRoute,
                        isEstimate = isEstimate,
                        units = units,
                        dose = dose,
                        estimatedDoseStandardDeviation = estimatedDoseStandardDeviation,
                        customSubstanceId = route.customSubstanceId
                    )
                },
                navigateToSaferSniffingScreen = navController::navigateToSaferSniffingOnJournalTab,
                navigateToURL = navController::navigateToURLInJournalTab
            )
        }
        composableWithTransitions<ChooseDoseRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ChooseDoseRoute>()
            ChooseDoseScreen(
                navigateToChooseTimeAndMaybeColor = {
                        units,
                        isEstimate,
                        dose,
                        estimatedDoseStandardDeviation,
                        customUnitId
                    ->
                    navController.navigateToChooseTime(
                        administrationRoute = route.administrationRoute,
                        isEstimate = isEstimate,
                        units = units,
                        dose = dose,
                        estimatedDoseStandardDeviation = estimatedDoseStandardDeviation,
                        substanceName = route.substanceName,
                        customUnitId = customUnitId
                    )
                },
                navigateToVolumetricDosingScreenOnJournalTab = navController::navigateToVolumetricDosingOnJournalTab,
                navigateToSaferSniffingScreen = navController::navigateToSaferSniffingOnJournalTab,
                navigateToURL = navController::navigateToURLInJournalTab
            )
        }
        composableWithTransitions<ChooseTimeRoute> {
            FinishIngestionScreen(
                dismissAddIngestionScreens = navController::dismissAddIngestionScreens
            )
        }
    }
}
