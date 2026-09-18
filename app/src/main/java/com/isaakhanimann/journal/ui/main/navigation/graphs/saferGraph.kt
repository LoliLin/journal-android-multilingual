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
import com.isaakhanimann.journal.ui.VOLUMETRIC_DOSE_ARTICLE_URL
import com.isaakhanimann.journal.ui.main.navigation.composableWithTransitions
import com.isaakhanimann.journal.ui.main.navigation.routes.AdministrationRouteExplanationRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DEEP_LINK_SAFER
import com.isaakhanimann.journal.ui.main.navigation.routes.DosageExplanationOnSaferTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DosageGuideRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DrugTestingRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ReagentTestingRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferHallucinogensRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferStimulantsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferTab
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferTabUrlRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.VolumetricDosingOnSaferTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToAdministrationRouteExplanation
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToDosageExplanationOnSaferTab
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToDosageGuide
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToDrugTesting
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToReagentTesting
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToSaferHallucinogens
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToURLOnSaferTab
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToVolumetricDosingOnSaferTab
import com.isaakhanimann.journal.ui.tabs.safer.DoseExplanationScreen
import com.isaakhanimann.journal.ui.tabs.safer.DoseGuideScreen
import com.isaakhanimann.journal.ui.tabs.safer.DrugTestingScreen
import com.isaakhanimann.journal.ui.tabs.safer.ReagentTestingScreen
import com.isaakhanimann.journal.ui.tabs.safer.RouteExplanationScreen
import com.isaakhanimann.journal.ui.tabs.safer.SaferHallucinogensScreen
import com.isaakhanimann.journal.ui.tabs.safer.SaferUseScreen
import com.isaakhanimann.journal.ui.tabs.safer.VolumetricDosingScreen
import com.isaakhanimann.journal.ui.tabs.search.substance.SaferStimulantsScreen
import com.isaakhanimann.journal.ui.tabs.search.substance.UrlScreen

fun NavGraphBuilder.saferGraph(navController: NavController) {
    navigation<SaferTab>(
        startDestination = SaferRoute
    ) {
        composableWithTransitions<SaferRoute>(
            deepLinks = listOf(navDeepLink<SaferRoute>(basePath = DEEP_LINK_SAFER))
        ) {
            SaferUseScreen(
                navigateToDrugTestingScreen = navController::navigateToDrugTesting,
                navigateToSaferHallucinogensScreen = navController::navigateToSaferHallucinogens,
                navigateToVolumetricDosingScreen = navController::navigateToVolumetricDosingOnSaferTab,
                navigateToDosageGuideScreen = navController::navigateToDosageGuide,
                navigateToDosageClassificationScreen = navController::navigateToDosageExplanationOnSaferTab,
                navigateToRouteExplanationScreen = navController::navigateToAdministrationRouteExplanation,
                navigateToURL = navController::navigateToURLOnSaferTab,
                navigateToReagentTestingScreen = navController::navigateToReagentTesting
            )
        }
        composableWithTransitions<SaferHallucinogensRoute> {
            SaferHallucinogensScreen()
        }
        composableWithTransitions<SaferStimulantsRoute> {
            // Kept for parity with SaferHallucinogensRoute above: the safer-use tab's own copy of the
            // screen. The substances tab has a separate destination for the same screen, which is
            // what SubstanceScreen links to.
            SaferStimulantsScreen()
        }
        composableWithTransitions<DosageExplanationOnSaferTabRoute> {
            DoseExplanationScreen()
        }
        composableWithTransitions<AdministrationRouteExplanationRoute> {
            RouteExplanationScreen(
                navigateToURL = navController::navigateToURLOnSaferTab
            )
        }
        composableWithTransitions<SaferTabUrlRoute> { backStackEntry ->
            UrlScreen(
                url = backStackEntry.toRoute<SaferTabUrlRoute>().url,
                onHandled = navController::popBackStack
            )
        }
        composableWithTransitions<DrugTestingRoute> { DrugTestingScreen() }
        composableWithTransitions<DosageGuideRoute> {
            DoseGuideScreen(
                navigateToDoseClassification = navController::navigateToDosageExplanationOnSaferTab,
                navigateToVolumetricDosing = navController::navigateToVolumetricDosingOnSaferTab,
                navigateToPWDosageArticle = {
                    navController.navigateToURLOnSaferTab(
                        url = "https://psychonautwiki.org/wiki/Dosage"
                    )
                }
            )
        }
        composableWithTransitions<VolumetricDosingOnSaferTabRoute> {
            VolumetricDosingScreen(
                navigateToVolumetricLiquidDosingArticle = {
                    navController.navigateToURLOnSaferTab(
                        VOLUMETRIC_DOSE_ARTICLE_URL
                    )
                }
            )
        }
        composableWithTransitions<ReagentTestingRoute> {
            ReagentTestingScreen(
                navigateToReagentTestingArticle = {
                    navController.navigateToURLOnSaferTab(
                        "https://psychonautwiki.org/wiki/Reagent_testing_kits"
                    )
                }
            )
        }
    }
}
