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
import com.isaakhanimann.journal.ui.main.navigation.routes.AddRatingRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AddTimedNoteRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CalendarRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DEEP_LINK_EXPERIENCE
import com.isaakhanimann.journal.ui.main.navigation.routes.DEEP_LINK_JOURNAL
import com.isaakhanimann.journal.ui.main.navigation.routes.DEEP_LINK_QUICK_NOTE
import com.isaakhanimann.journal.ui.main.navigation.routes.DEEP_LINK_TIME_CAPSULE
import com.isaakhanimann.journal.ui.main.navigation.routes.DosageExplanationOnJournalTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditExperienceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditRatingRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditTimedNoteRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ExperienceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ExplainTimelineOnJournalTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.IngestionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.JournalRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.JournalTab
import com.isaakhanimann.journal.ui.main.navigation.routes.JournalTabUrlRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.QuickTimedNoteRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferSniffingOnJournalTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.TimeCapsuleRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.TimelineScreenRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.VolumetricDosingOnJournalTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToAddIngestion
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToAddRating
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToAddTimedNote
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToCalendar
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToEditExperience
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToEditRating
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToEditTimedNote
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToExperience
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToExplainTimelineOnJournalTab
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToIngestion
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToQuickTimedNote
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToTimelineScreen
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToURLInJournalTab
import com.isaakhanimann.journal.ui.tabs.journal.JournalScreen
import com.isaakhanimann.journal.ui.tabs.journal.calendar.CalendarJournalScreen
import com.isaakhanimann.journal.ui.tabs.journal.experience.OneExperienceScreen
import com.isaakhanimann.journal.ui.tabs.journal.experience.edit.EditExperienceScreen
import com.isaakhanimann.journal.ui.tabs.journal.experience.editingestion.EditIngestionScreen
import com.isaakhanimann.journal.ui.tabs.journal.experience.rating.add.AddRatingScreen
import com.isaakhanimann.journal.ui.tabs.journal.experience.rating.edit.EditRatingScreen
import com.isaakhanimann.journal.ui.tabs.journal.experience.timednote.add.AddTimedNoteScreen
import com.isaakhanimann.journal.ui.tabs.journal.experience.timednote.add.QuickTimedNoteScreen
import com.isaakhanimann.journal.ui.tabs.journal.experience.timednote.edit.EditTimedNoteScreen
import com.isaakhanimann.journal.ui.tabs.journal.experience.timeline.ExplainTimelineScreen
import com.isaakhanimann.journal.ui.tabs.journal.experience.timeline.screen.TimelineScreen
import com.isaakhanimann.journal.ui.tabs.journal.timecapsule.TimeCapsuleScreen
import com.isaakhanimann.journal.ui.tabs.safer.DoseExplanationScreen
import com.isaakhanimann.journal.ui.tabs.safer.VolumetricDosingScreen
import com.isaakhanimann.journal.ui.tabs.search.substance.SaferSniffingScreen
import com.isaakhanimann.journal.ui.tabs.search.substance.UrlScreen

fun NavGraphBuilder.journalGraph(navController: NavController) {
    navigation<JournalTab>(
        startDestination = JournalRoute
    ) {
        composableWithTransitions<JournalRoute>(
            deepLinks = listOf(navDeepLink<JournalRoute>(basePath = DEEP_LINK_JOURNAL))
        ) {
            JournalScreen(
                navigateToExperiencePopNothing = navController::navigateToExperience,
                navigateToAddIngestion = navController::navigateToAddIngestion,
                navigateToCalendar = navController::navigateToCalendar,
                navigateToQuickTimedNote = navController::navigateToQuickTimedNote
            )
        }
        composableWithTransitions<EditExperienceRoute> {
            EditExperienceScreen(navigateBack = navController::popBackStack)
        }
        composableWithTransitions<AddRatingRoute> {
            AddRatingScreen(navigateBack = navController::popBackStack)
        }
        composableWithTransitions<AddTimedNoteRoute> {
            AddTimedNoteScreen(navigateBack = navController::popBackStack)
        }
        composableWithTransitions<QuickTimedNoteRoute>(
            deepLinks = listOf(navDeepLink<QuickTimedNoteRoute>(basePath = DEEP_LINK_QUICK_NOTE))
        ) {
            QuickTimedNoteScreen(navigateBack = navController::popBackStack)
        }
        composableWithTransitions<TimeCapsuleRoute>(
            deepLinks = listOf(navDeepLink<TimeCapsuleRoute>(basePath = DEEP_LINK_TIME_CAPSULE))
        ) {
            TimeCapsuleScreen(
                navigateBack = navController::popBackStack,
                navigateToExperience = navController::navigateToExperience
            )
        }
        composableWithTransitions<EditRatingRoute> {
            EditRatingScreen(navigateBack = navController::popBackStack)
        }
        composableWithTransitions<EditTimedNoteRoute> {
            EditTimedNoteScreen(navigateBack = navController::popBackStack)
        }
        composableWithTransitions<TimelineScreenRoute> {
            TimelineScreen()
        }
        composableWithTransitions<VolumetricDosingOnJournalTabRoute> {
            VolumetricDosingScreen(
                navigateToVolumetricLiquidDosingArticle = {
                    navController.navigateToURLInJournalTab(url = VOLUMETRIC_DOSE_ARTICLE_URL)
                }
            )
        }
        composableWithTransitions<ExperienceRoute>(
            deepLinks = listOf(navDeepLink<ExperienceRoute>(basePath = DEEP_LINK_EXPERIENCE))
        ) { backStackEntry ->
            val experienceId = backStackEntry.toRoute<ExperienceRoute>().experienceId
            OneExperienceScreen(
                navigateToAddIngestionSearch = navController::navigateToAddIngestion,
                navigateToExplainTimeline = navController::navigateToExplainTimelineOnJournalTab,
                navigateToEditExperienceScreen = {
                    navController.navigateToEditExperience(experienceId)
                },
                navigateToIngestionScreen = { ingestionId ->
                    navController.navigateToIngestion(ingestionId)
                },
                navigateBack = navController::popBackStack,
                navigateToAddRatingScreen = {
                    navController.navigateToAddRating(experienceId)
                },
                navigateToAddTimedNoteScreen = {
                    navController.navigateToAddTimedNote(experienceId)
                },
                navigateToURL = navController::navigateToURLInJournalTab,
                navigateToEditRatingScreen = navController::navigateToEditRating,
                navigateToTimelineScreen = { consumerName ->
                    navController.navigateToTimelineScreen(consumerName, experienceId)
                },
                navigateToEditTimedNoteScreen = { timedNoteID ->
                    navController.navigateToEditTimedNote(
                        timedNoteId = timedNoteID,
                        experienceId = experienceId
                    )
                }
            )
        }
        composableWithTransitions<IngestionRoute> {
            EditIngestionScreen(navigateBack = navController::popBackStack)
        }
        // Article viewer of the journal tab. Registered here rather than inside addIngestionGraph:
        // the add-ingestion screens open articles too, and a destination may only exist once per
        // graph hierarchy, so the flow links to this sibling (same as the explain/safer screens
        // below).
        composableWithTransitions<JournalTabUrlRoute> { backStackEntry ->
            UrlScreen(
                url = backStackEntry.toRoute<JournalTabUrlRoute>().url,
                onHandled = navController::popBackStack
            )
        }
        composableWithTransitions<ExplainTimelineOnJournalTabRoute> {
            ExplainTimelineScreen()
        }
        composableWithTransitions<DosageExplanationOnJournalTabRoute> {
            DoseExplanationScreen()
        }
        composableWithTransitions<SaferSniffingOnJournalTabRoute> {
            SaferSniffingScreen()
        }
        composableWithTransitions<CalendarRoute> {
            CalendarJournalScreen(
                navigateToExperiencePopNothing = navController::navigateToExperience
            )
        }
        addIngestionGraph(navController)
    }
}
