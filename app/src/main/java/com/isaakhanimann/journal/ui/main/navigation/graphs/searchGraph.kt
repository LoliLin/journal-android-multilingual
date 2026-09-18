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
import com.isaakhanimann.journal.ui.main.navigation.routes.AddCustomSubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CategoryRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DEEP_LINK_CATEGORY
import com.isaakhanimann.journal.ui.main.navigation.routes.DEEP_LINK_SUBSTANCE
import com.isaakhanimann.journal.ui.main.navigation.routes.DEEP_LINK_SUBSTANCES
import com.isaakhanimann.journal.ui.main.navigation.routes.DosageExplanationOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditCustomSubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ExplainTimelineOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferHallucinogensOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferStimulantsOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstancesRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstancesTab
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstancesTabUrlRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.VolumetricDosingOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToAddCustomSubstance
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToCategoryScreen
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToDosageExplanationOnSubstancesTab
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToEditCustomSubstance
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToExplainTimelineOnSubstancesTab
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToSaferHallucinogensOnSubstancesTab
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToSaferStimulantsOnSubstancesTab
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToSubstanceScreen
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToURLOnSubstancesTab
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToVolumetricDosingOnSubstancesTab
import com.isaakhanimann.journal.ui.tabs.journal.experience.timeline.ExplainTimelineScreen
import com.isaakhanimann.journal.ui.tabs.safer.DoseExplanationScreen
import com.isaakhanimann.journal.ui.tabs.safer.SaferHallucinogensScreen
import com.isaakhanimann.journal.ui.tabs.safer.VolumetricDosingScreen
import com.isaakhanimann.journal.ui.tabs.search.SearchScreen
import com.isaakhanimann.journal.ui.tabs.search.custom.AddCustomSubstance
import com.isaakhanimann.journal.ui.tabs.search.custom.EditCustomSubstance
import com.isaakhanimann.journal.ui.tabs.search.substance.SaferStimulantsScreen
import com.isaakhanimann.journal.ui.tabs.search.substance.SubstanceScreen
import com.isaakhanimann.journal.ui.tabs.search.substance.UrlScreen
import com.isaakhanimann.journal.ui.tabs.search.substance.category.CategoryScreen

fun NavGraphBuilder.searchGraph(navController: NavController) {
    navigation<SubstancesTab>(
        startDestination = SubstancesRoute
    ) {
        composableWithTransitions<SubstancesRoute>(
            deepLinks = listOf(navDeepLink<SubstancesRoute>(basePath = DEEP_LINK_SUBSTANCES))
        ) {
            SearchScreen(
                onSubstanceTap = {
                    navController.navigateToSubstanceScreen(substanceName = it.name)
                },
                onCustomSubstanceTap = navController::navigateToEditCustomSubstance,
                navigateToAddCustomSubstanceScreen = navController::navigateToAddCustomSubstance
            )
        }
        composableWithTransitions<SubstanceRoute>(
            deepLinks = listOf(navDeepLink<SubstanceRoute>(basePath = DEEP_LINK_SUBSTANCE))
        ) {
            SubstanceScreen(
                navigateToDosageExplanationScreen = navController::navigateToDosageExplanationOnSubstancesTab,
                navigateToSaferHallucinogensScreen = navController::navigateToSaferHallucinogensOnSubstancesTab,
                navigateToSaferStimulantsScreen = navController::navigateToSaferStimulantsOnSubstancesTab,
                navigateToExplainTimeline = navController::navigateToExplainTimelineOnSubstancesTab,
                navigateToCategoryScreen = navController::navigateToCategoryScreen,
                navigateToVolumetricDosingScreen = navController::navigateToVolumetricDosingOnSubstancesTab,
                navigateToArticle = navController::navigateToURLOnSubstancesTab,
                navigateToSubstanceScreen = navController::navigateToSubstanceScreen
            )
        }
        composableWithTransitions<SubstancesTabUrlRoute> { backStackEntry ->
            UrlScreen(
                url = backStackEntry.toRoute<SubstancesTabUrlRoute>().url,
                onHandled = navController::popBackStack
            )
        }
        composableWithTransitions<CategoryRoute>(
            deepLinks = listOf(navDeepLink<CategoryRoute>(basePath = DEEP_LINK_CATEGORY))
        ) {
            CategoryScreen(
                navigateToURL = navController::navigateToURLOnSubstancesTab,
                onSubstanceTap = {
                    navController.navigateToSubstanceScreen(substanceName = it.name)
                }
            )
        }
        composableWithTransitions<EditCustomSubstanceRoute> {
            EditCustomSubstance(navigateBack = navController::popBackStack)
        }
        composableWithTransitions<AddCustomSubstanceRoute> {
            AddCustomSubstance(
                navigateBack = navController::popBackStack
            )
        }
        composableWithTransitions<VolumetricDosingOnSubstancesTabRoute> {
            VolumetricDosingScreen(navigateToVolumetricLiquidDosingArticle = {
                navController.navigateToURLOnSubstancesTab(
                    VOLUMETRIC_DOSE_ARTICLE_URL
                )
            })
        }
        composableWithTransitions<ExplainTimelineOnSubstancesTabRoute> {
            ExplainTimelineScreen()
        }
        composableWithTransitions<DosageExplanationOnSubstancesTabRoute> {
            DoseExplanationScreen()
        }
        composableWithTransitions<SaferHallucinogensOnSubstancesTabRoute> {
            SaferHallucinogensScreen()
        }
        composableWithTransitions<SaferStimulantsOnSubstancesTabRoute> {
            SaferStimulantsScreen()
        }
    }
}
