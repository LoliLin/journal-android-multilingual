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

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.ReleaseForm

/**
 * Navigates to [route], de-duplicating it when it is already on top.
 *
 * Every screen in the app is reached by tapping a link, so repeated taps on the same item would
 * otherwise push an identical copy of the destination onto the back stack (`launchSingleTop` only
 * suppresses that when the route and all of its arguments match exactly).
 */
private fun NavController.navigateTo(route: Any) {
    navigate(route) {
        launchSingleTop = true
    }
}

// ---------------------------------------------------------------------------------------------
// Bottom navigation bar
// ---------------------------------------------------------------------------------------------

/**
 * Switches to [destination], keeping one saved back stack per tab so returning to a tab restores
 * where the user left off.
 */
fun NavController.switchToTopLevelDestination(destination: TopLevelDestination) {
    navigate(destination.graphRoute) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Returns to the root of [destination] when it is already the selected tab.
 * Every destination stacked above the tab root is popped, which is what re-tapping the selected
 * tab is expected to do.
 */
fun NavController.popToTopLevelDestinationRoot(destination: TopLevelDestination) {
    popBackStack(destination.startRoute, inclusive = false)
}

// ---------------------------------------------------------------------------------------------
// Journal tab
// ---------------------------------------------------------------------------------------------

fun NavController.navigateToExperience(experienceId: Int) =
    navigateTo(ExperienceRoute(experienceId))

fun NavController.navigateToEditExperience(experienceId: Int) =
    navigateTo(EditExperienceRoute(experienceId))

fun NavController.navigateToIngestion(ingestionId: Int) =
    navigateTo(IngestionRoute(ingestionId))

fun NavController.navigateToAddRating(experienceId: Int) =
    navigateTo(AddRatingRoute(experienceId))

fun NavController.navigateToEditRating(ratingId: Int) =
    navigateTo(EditRatingRoute(ratingId))

fun NavController.navigateToAddTimedNote(experienceId: Int) =
    navigateTo(AddTimedNoteRoute(experienceId))

fun NavController.navigateToEditTimedNote(timedNoteId: Int, experienceId: Int) =
    navigateTo(EditTimedNoteRoute(timedNoteId = timedNoteId, experienceId = experienceId))

fun NavController.navigateToQuickTimedNote(experienceId: Int) =
    navigateTo(QuickTimedNoteRoute(experienceId))

fun NavController.navigateToTimelineScreen(consumerName: String, experienceId: Int) =
    navigateTo(TimelineScreenRoute(consumerName = consumerName, experienceId = experienceId))

fun NavController.navigateToTimeCapsule() = navigateTo(TimeCapsuleRoute)

fun NavController.navigateToCalendar() = navigateTo(CalendarRoute)

fun NavController.navigateToExplainTimelineOnJournalTab() =
    navigateTo(ExplainTimelineOnJournalTabRoute)

fun NavController.navigateToDosageExplanationOnJournalTab() =
    navigateTo(DosageExplanationOnJournalTabRoute)

fun NavController.navigateToSaferSniffingOnJournalTab() =
    navigateTo(SaferSniffingOnJournalTabRoute)

fun NavController.navigateToVolumetricDosingOnJournalTab() =
    navigateTo(VolumetricDosingOnJournalTabRoute)

fun NavController.navigateToURLInJournalTab(url: String) = navigateTo(JournalTabUrlRoute(url))

/** Starts the nested add-ingestion flow from its first screen. */
fun NavController.navigateToAddIngestion() = navigateTo(AddIngestionRoute)

/** Pops the whole add-ingestion flow, including every screen inside it. */
fun NavController.dismissAddIngestionScreens() {
    popBackStack(AddIngestionRoute, inclusive = true)
}

// ---------------------------------------------------------------------------------------------
// Add-ingestion flow
// ---------------------------------------------------------------------------------------------

fun NavController.navigateToCheckInteractions(substanceName: String) =
    navigateTo(CheckInteractionsRoute(substanceName))

fun NavController.navigateToCheckSaferUse(substanceName: String) =
    navigateTo(CheckSaferUseRoute(substanceName))

fun NavController.navigateToChooseRouteOfAddIngestion(substanceName: String) =
    navigateTo(ChooseRouteOfAddIngestionRoute(substanceName))

fun NavController.navigateToChooseDose(
    substanceName: String,
    administrationRoute: AdministrationRoute
) = navigateTo(
    ChooseDoseRoute(substanceName = substanceName, administrationRoute = administrationRoute)
)

fun NavController.navigateToChooseDoseCustomUnit(customUnitId: Int) =
    navigateTo(ChooseDoseCustomUnitRoute(customUnitId))

fun NavController.navigateToChooseCustomRoute(customSubstanceId: Int) =
    navigateTo(CustomChooseRouteRoute(customSubstanceId))

fun NavController.navigateToChooseDoseCustom(
    customSubstanceId: Int,
    administrationRoute: AdministrationRoute
) = navigateTo(
    CustomChooseDoseRoute(
        customSubstanceId = customSubstanceId,
        administrationRoute = administrationRoute
    )
)

/** Opens the final step of the add-ingestion flow, carrying the dose the user chose so far. */
fun NavController.navigateToChooseTime(
    administrationRoute: AdministrationRoute,
    isEstimate: Boolean,
    units: String? = null,
    dose: Double? = null,
    estimatedDoseStandardDeviation: Double? = null,
    substanceName: String? = null,
    customUnitId: Int? = null,
    customSubstanceId: Int? = null,
    releaseForm: ReleaseForm? = null
) = navigateTo(
    ChooseTimeRoute(
        administrationRoute = administrationRoute,
        isEstimate = isEstimate,
        units = units,
        dose = dose,
        estimatedDoseStandardDeviation = estimatedDoseStandardDeviation,
        substanceName = substanceName,
        customUnitId = customUnitId,
        customSubstanceId = customSubstanceId,
        releaseForm = releaseForm
    )
)

// ---------------------------------------------------------------------------------------------
// Substances tab
// ---------------------------------------------------------------------------------------------

fun NavController.navigateToSubstanceScreen(substanceName: String) =
    navigateTo(SubstanceRoute(substanceName))

fun NavController.navigateToCategoryScreen(categoryName: String) =
    navigateTo(CategoryRoute(categoryName))

fun NavController.navigateToEditCustomSubstance(customSubstanceId: Int) =
    navigateTo(EditCustomSubstanceRoute(customSubstanceId))

fun NavController.navigateToAddCustomSubstance() = navigateTo(AddCustomSubstanceRoute)

fun NavController.navigateToURLOnSubstancesTab(url: String) = navigateTo(SubstancesTabUrlRoute(url))

fun NavController.navigateToExplainTimelineOnSubstancesTab() =
    navigateTo(ExplainTimelineOnSubstancesTabRoute)

fun NavController.navigateToDosageExplanationOnSubstancesTab() =
    navigateTo(DosageExplanationOnSubstancesTabRoute)

fun NavController.navigateToVolumetricDosingOnSubstancesTab() =
    navigateTo(VolumetricDosingOnSubstancesTabRoute)

fun NavController.navigateToSaferHallucinogensOnSubstancesTab() =
    navigateTo(SaferHallucinogensOnSubstancesTabRoute)

fun NavController.navigateToSaferStimulantsOnSubstancesTab() =
    navigateTo(SaferStimulantsOnSubstancesTabRoute)

// ---------------------------------------------------------------------------------------------
// Safer-use tab
// ---------------------------------------------------------------------------------------------

fun NavController.navigateToSaferHallucinogens() = navigateTo(SaferHallucinogensRoute)

fun NavController.navigateToSaferStimulants() = navigateTo(SaferStimulantsRoute)

fun NavController.navigateToDosageExplanationOnSaferTab() =
    navigateTo(DosageExplanationOnSaferTabRoute)

fun NavController.navigateToAdministrationRouteExplanation() =
    navigateTo(AdministrationRouteExplanationRoute)

fun NavController.navigateToDrugTesting() = navigateTo(DrugTestingRoute)

fun NavController.navigateToDosageGuide() = navigateTo(DosageGuideRoute)

fun NavController.navigateToVolumetricDosingOnSaferTab() =
    navigateTo(VolumetricDosingOnSaferTabRoute)

fun NavController.navigateToReagentTesting() = navigateTo(ReagentTestingRoute)

fun NavController.navigateToURLOnSaferTab(url: String) = navigateTo(SaferTabUrlRoute(url))

// ---------------------------------------------------------------------------------------------
// Statistics tab
// ---------------------------------------------------------------------------------------------

fun NavController.navigateToSubstanceCompanionScreen(substanceName: String, consumerName: String?) =
    navigateTo(SubstanceCompanionRoute(substanceName = substanceName, consumerName = consumerName))

// ---------------------------------------------------------------------------------------------
// Settings tab
// ---------------------------------------------------------------------------------------------

fun NavController.navigateToFAQ() = navigateTo(FAQRoute)

fun NavController.navigateToDonate() = navigateTo(DonateRoute)

fun NavController.navigateToPreferences() = navigateTo(PreferencesRoute)

fun NavController.navigateToIconPicker() = navigateTo(IconPickerRoute)

fun NavController.navigateToExtensionPack() = navigateTo(ExtensionPackRoute)

fun NavController.navigateToComboSettings() = navigateTo(CombinationSettingsRoute)

fun NavController.navigateToSubstanceColors() = navigateTo(SubstanceColorsRoute)

fun NavController.navigateToCustomUnits() = navigateTo(CustomUnitsRoute)

fun NavController.navigateToCustomUnitArchive() = navigateTo(CustomUnitArchiveRoute)

fun NavController.navigateToEditCustomUnit(customUnitId: Int) =
    navigateTo(EditCustomUnitRoute(customUnitId))

/** Starts the nested add-custom-unit flow from its first screen. */
fun NavController.navigateToAddCustomUnits() = navigateTo(AddCustomUnitsRoute)

/** Pops the whole add-custom-unit flow, including every screen inside it. */
fun NavController.dismissAddCustomUnits() {
    popBackStack(AddCustomUnitsRoute, inclusive = true)
}

fun NavController.navigateToChooseRouteOfAddCustomUnit(substanceName: String) =
    navigateTo(ChooseRouteOfAddCustomUnitRoute(substanceName))

fun NavController.navigateToFinishAddCustomUnit(
    substanceName: String,
    administrationRoute: AdministrationRoute
) = navigateTo(
    FinishAddCustomUnitRoute(
        substanceName = substanceName,
        administrationRoute = administrationRoute
    )
)
