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

import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.ReleaseForm
import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------------------------
// Journal tab
// ---------------------------------------------------------------------------------------------

/** Start destination of the journal tab. */
@Serializable
data object JournalRoute

@Serializable
data object CalendarRoute

@Serializable
data class ExperienceRoute(val experienceId: Int)

@Serializable
data class EditExperienceRoute(val experienceId: Int)

@Serializable
data class IngestionRoute(val ingestionId: Int)

@Serializable
data class AddRatingRoute(val experienceId: Int)

@Serializable
data class EditRatingRoute(val ratingId: Int)

@Serializable
data class AddTimedNoteRoute(val experienceId: Int)

@Serializable
data class EditTimedNoteRoute(val timedNoteId: Int, val experienceId: Int)

@Serializable
data class QuickTimedNoteRoute(val experienceId: Int)

@Serializable
data class TimelineScreenRoute(val consumerName: String, val experienceId: Int)

/** Daily "one year ago" recap. */
@Serializable
data object TimeCapsuleRoute

@Serializable
data object ExplainTimelineOnJournalTabRoute

@Serializable
data object DosageExplanationOnJournalTabRoute

@Serializable
data object SaferSniffingOnJournalTabRoute

@Serializable
data object VolumetricDosingOnJournalTabRoute

/**
 * In-app article viewer on the journal tab.
 *
 * Each tab registers its own URL destination class: route strings are derived from the class name,
 * so sharing one class between two graphs would create two destinations with the same route.
 */
@Serializable
data class JournalTabUrlRoute(val url: String)

// ---------------------------------------------------------------------------------------------
// Add-ingestion flow: a nested graph on the journal tab
// ---------------------------------------------------------------------------------------------

/** Route of the nested add-ingestion graph; navigating here starts the flow. */
@Serializable
data object AddIngestionRoute

/** Start destination of the add-ingestion flow. */
@Serializable
data object AddIngestionSearchRoute

@Serializable
data class CheckInteractionsRoute(val substanceName: String)

@Serializable
data class CheckSaferUseRoute(val substanceName: String)

@Serializable
data class ChooseRouteOfAddIngestionRoute(val substanceName: String)

@Serializable
data class ChooseDoseRoute(
    val substanceName: String,
    val administrationRoute: AdministrationRoute
)

@Serializable
data class ChooseDoseCustomUnitRoute(val customUnitId: Int)

@Serializable
data class CustomChooseRouteRoute(val customSubstanceId: Int)

@Serializable
data class CustomChooseDoseRoute(
    val customSubstanceId: Int,
    val administrationRoute: AdministrationRoute
)

/**
 * Final step of the add-ingestion flow.
 *
 * [administrationRoute] and [isEstimate] are path arguments (no default); every other property is
 * optional and therefore becomes a query argument.
 */
@Serializable
data class ChooseTimeRoute(
    val administrationRoute: AdministrationRoute,
    val isEstimate: Boolean,
    val units: String? = null,
    val dose: Double? = null,
    val estimatedDoseStandardDeviation: Double? = null,
    val substanceName: String? = null,
    val customUnitId: Int? = null,
    val customSubstanceId: Int? = null,
    val releaseForm: ReleaseForm? = null
)

/**
 * `SavedStateHandle` key holding the selected release form.
 *
 * The final add-ingestion screen receives it as a route argument and also keeps the user's later
 * choice in the handle, and the edit-ingestion screen reuses the same slot as scratch state.
 * Deriving the key from the route property keeps both sides in sync when it is renamed.
 */
val RELEASE_FORM_HANDLE_KEY: String = ChooseTimeRoute::releaseForm.name
