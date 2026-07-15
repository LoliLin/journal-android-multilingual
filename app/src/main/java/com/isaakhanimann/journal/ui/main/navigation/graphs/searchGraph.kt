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

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigation
import com.isaakhanimann.journal.ui.main.navigation.composableWithTransitions
import com.isaakhanimann.journal.ui.main.navigation.DrugsTopLevelRoute
import com.isaakhanimann.journal.ui.tabs.journal.experience.timeline.ExplainTimelineScreen
import com.isaakhanimann.journal.ui.tabs.safer.DoseExplanationScreen
import com.isaakhanimann.journal.ui.tabs.safer.VolumetricDosingScreen
import com.isaakhanimann.journal.ui.tabs.search.SearchScreen
import com.isaakhanimann.journal.ui.tabs.search.custom.AddCustomSubstanceScreen
import com.isaakhanimann.journal.ui.tabs.search.custom.EditCustomSubstanceScreen
import com.isaakhanimann.journal.ui.tabs.search.substance.SubstanceScreen
import com.isaakhanimann.journal.ui.tabs.search.substance.category.CategoryScreen
import kotlinx.serialization.Serializable

fun NavGraphBuilder.searchGraph(navController: NavHostController) {
    navigation<DrugsTopLevelRoute>(
        startDestination = DrugsScreenRoute,
    ) {
        composableWithTransitions<DrugsScreenRoute>{
            SearchScreen(
                onSubstanceTap = { substanceModel ->
                    navController.navigate(SubstanceRoute(substanceName = substanceModel.name))
                },
                onCustomSubstanceTap = { customSubstanceId ->
                    navController.navigate(EditCustomSubstanceRoute(customSubstanceId))
                },
                navigateToAddCustomSubstanceScreen = {
                    navController.navigate(AddCustomSubstanceRouteOnSearchGraph)
                }
            )
        }
        composableWithTransitions<SubstanceRoute> {

            SubstanceScreen(

                navigateToDosageExplanationScreen = {

                    navController.navigate(DosageExplanationRouteOnSearchTab)

                },

                navigateToSaferHallucinogensScreen = {

                    navController.navigate(SaferHallucinogensRoute)

                },

                navigateToSaferStimulantsScreen = {

                    navController.navigate(SaferStimulantsRoute)

                },

                navigateToExplainTimeline = {

                    navController.navigate(ExplainTimelineOnSearchTabRoute)

                },

                navigateToCategoryScreen = { categoryName ->

                    navController.navigate(CategoryRoute(categoryName))

                },

                navigateToVolumetricDosingScreen = {

                    navController.navigate(VolumetricDosingOnSearchTabRoute)

                },

                navigateToArticle = { url ->

                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))

                    navController.context.startActivity(intent)

                },

            )

        }
        composableWithTransitions<CategoryRoute> {

            CategoryScreen(

                navigateToURL = { url ->

                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))

                    navController.context.startActivity(intent)

                },

            )

        }
        composableWithTransitions<EditCustomSubstanceRoute> {
            EditCustomSubstanceScreen(navigateBack = navController::popBackStack)
        }
        composableWithTransitions<AddCustomSubstanceRouteOnSearchGraph> {
            AddCustomSubstanceScreen(
                navigateBack = navController::popBackStack
            )
        }
        composableWithTransitions<VolumetricDosingOnSearchTabRoute> {
            VolumetricDosingScreen(
                navigateToVolumetricLiquidDosingArticle = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://psychonautwiki.org/wiki/Volumetric_dosing"))
                    navController.context.startActivity(intent)
                },
            )
        }
        composableWithTransitions<ExplainTimelineOnSearchTabRoute> { ExplainTimelineScreen() }

        composableWithTransitions<DosageExplanationRouteOnSearchTab> { DoseExplanationScreen() }

    }
}

@Serializable
object DrugsScreenRoute

@Serializable
data class SubstanceRoute(val substanceName: String)

@Serializable
data class CategoryRoute(val categoryName: String)

@Serializable
data class EditCustomSubstanceRoute(val customSubstanceId: Int)

@Serializable
object AddCustomSubstanceRouteOnSearchGraph

@Serializable
object VolumetricDosingOnSearchTabRoute

@Serializable
object ExplainTimelineOnSearchTabRoute

@Serializable
object DosageExplanationRouteOnSearchTab