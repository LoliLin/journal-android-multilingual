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

import androidx.navigation3.runtime.NavKey
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.ReleaseForm
import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------------------------
// Journal tab
// ---------------------------------------------------------------------------------------------

/** Start destination of the journal tab. */
@Serializable
data object JournalRoute : NavKey

@Serializable
data object CalendarRoute : NavKey

@Serializable
data class ExperienceRoute(val experienceId: Int) : NavKey

@Serializable
data class EditExperienceRoute(val experienceId: Int) : NavKey

@Serializable
data class IngestionRoute(val ingestionId: Int) : NavKey

@Serializable
data class AddRatingRoute(val experienceId: Int) : NavKey

@Serializable
data class EditRatingRoute(val ratingId: Int) : NavKey

@Serializable
data class AddTimedNoteRoute(val experienceId: Int) : NavKey

@Serializable
data class EditTimedNoteRoute(val timedNoteId: Int, val experienceId: Int) : NavKey

@Serializable
data class QuickTimedNoteRoute(val experienceId: Int) : NavKey

@Serializable
data class TimelineScreenRoute(val consumerName: String, val experienceId: Int) : NavKey

/** Daily "one year ago" recap. */
@Serializable
data object TimeCapsuleRoute : NavKey

@Serializable
data object ExplainTimelineOnJournalTabRoute : NavKey

@Serializable
data object DosageExplanationOnJournalTabRoute : NavKey

@Serializable
data object SaferSniffingOnJournalTabRoute : NavKey

@Serializable
data object VolumetricDosingOnJournalTabRoute : NavKey

/**
 * In-app article viewer on the journal tab.
 *
 * Each tab registers its own URL destination class: route strings are derived from the class name,
 * so sharing one class between two graphs would create two destinations with the same route.
 */
@Serializable
data class JournalTabUrlRoute(val url: String) : NavKey

// ---------------------------------------------------------------------------------------------
// Add-ingestion flow: screens the journal tab pushes on top of its root
// ---------------------------------------------------------------------------------------------

/** First screen of the add-ingestion flow; navigating here starts the flow. */
@Serializable
data object AddIngestionRoute : NavKey

@Serializable
data class CheckInteractionsRoute(val substanceName: String) : NavKey

@Serializable
data class CheckSaferUseRoute(val substanceName: String) : NavKey

@Serializable
data class ChooseRouteOfAddIngestionRoute(val substanceName: String) : NavKey

@Serializable
data class ChooseDoseRoute(
    val substanceName: String,
    val administrationRoute: AdministrationRoute
) : NavKey

@Serializable
data class ChooseDoseCustomUnitRoute(val customUnitId: Int) : NavKey

@Serializable
data class CustomChooseRouteRoute(val customSubstanceId: Int) : NavKey

@Serializable
data class CustomChooseDoseRoute(
    val customSubstanceId: Int,
    val administrationRoute: AdministrationRoute
) : NavKey

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
) : NavKey

/**
 * `SavedStateHandle` key holding the selected release form.
 *
 * The final add-ingestion screen receives it as a route argument and also keeps the user's later
 * choice in the handle, and the edit-ingestion screen reuses the same slot as scratch state.
 * Deriving the key from the route property keeps both sides in sync when it is renamed.
 */
val RELEASE_FORM_HANDLE_KEY: String = ChooseTimeRoute::releaseForm.name
