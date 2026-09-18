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
import com.isaakhanimann.journal.ui.main.navigation.composableWithTransitions
import com.isaakhanimann.journal.ui.main.navigation.routes.DEEP_LINK_STATS
import com.isaakhanimann.journal.ui.main.navigation.routes.DEEP_LINK_SUBSTANCE_COMPANION
import com.isaakhanimann.journal.ui.main.navigation.routes.StatsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.StatsTab
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceCompanionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToCategoryScreen
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToIngestion
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToSubstanceCompanionScreen
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToSubstanceScreen
import com.isaakhanimann.journal.ui.tabs.stats.StatsScreen
import com.isaakhanimann.journal.ui.tabs.stats.substancecompanion.SubstanceCompanionScreen

fun NavGraphBuilder.statsGraph(navController: NavController) {
    navigation<StatsTab>(
        startDestination = StatsRoute
    ) {
        composableWithTransitions<StatsRoute>(
            deepLinks = listOf(navDeepLink<StatsRoute>(basePath = DEEP_LINK_STATS))
        ) {
            StatsScreen(
                navigateToSubstanceCompanion = { substanceName, consumerName ->
                    navController.navigateToSubstanceCompanionScreen(
                        substanceName = substanceName,
                        consumerName = consumerName
                    )
                }
            )
        }
        composableWithTransitions<SubstanceCompanionRoute>(
            deepLinks = listOf(
                navDeepLink<SubstanceCompanionRoute>(basePath = DEEP_LINK_SUBSTANCE_COMPANION)
            )
        ) {
            SubstanceCompanionScreen(
                navigateToCategoryScreen = navController::navigateToCategoryScreen,
                navigateToSubstanceScreen = navController::navigateToSubstanceScreen,
                navigateToIngestion = navController::navigateToIngestion
            )
        }
    }
}
