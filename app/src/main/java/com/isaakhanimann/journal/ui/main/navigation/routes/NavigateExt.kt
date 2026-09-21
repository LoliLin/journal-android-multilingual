package com.isaakhanimann.journal.ui.main.navigation.routes

import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.ReleaseForm
import com.isaakhanimann.journal.ui.main.navigation.Nav3TabManager

private fun Nav3TabManager.navigateTo(route: Any) = navigate(route)

fun Nav3TabManager.switchToTopLevelDestination(destination: TopLevelDestination) = switchToTab(destination)

fun Nav3TabManager.popToTopLevelDestinationRoot(destination: TopLevelDestination) = popToRoot(destination)

fun Nav3TabManager.navigateToExperience(experienceId: Int) = navigateTo(ExperienceRoute(experienceId))
fun Nav3TabManager.navigateToEditExperience(experienceId: Int) = navigateTo(EditExperienceRoute(experienceId))
fun Nav3TabManager.navigateToIngestion(ingestionId: Int) = navigateTo(IngestionRoute(ingestionId))
fun Nav3TabManager.navigateToAddRating(experienceId: Int) = navigateTo(AddRatingRoute(experienceId))
fun Nav3TabManager.navigateToEditRating(ratingId: Int) = navigateTo(EditRatingRoute(ratingId))
fun Nav3TabManager.navigateToAddTimedNote(experienceId: Int) = navigateTo(AddTimedNoteRoute(experienceId))
fun Nav3TabManager.navigateToEditTimedNote(timedNoteId: Int, experienceId: Int) =
    navigateTo(EditTimedNoteRoute(timedNoteId = timedNoteId, experienceId = experienceId))
fun Nav3TabManager.navigateToQuickTimedNote(experienceId: Int) = navigateTo(QuickTimedNoteRoute(experienceId))
fun Nav3TabManager.navigateToTimelineScreen(consumerName: String, experienceId: Int) =
    navigateTo(TimelineScreenRoute(consumerName = consumerName, experienceId = experienceId))
fun Nav3TabManager.navigateToTimeCapsule() = navigateTo(TimeCapsuleRoute)
fun Nav3TabManager.navigateToCalendar() = navigateTo(CalendarRoute)
fun Nav3TabManager.navigateToExplainTimelineOnJournalTab() = navigateTo(ExplainTimelineOnJournalTabRoute)
fun Nav3TabManager.navigateToDosageExplanationOnJournalTab() =
    navigateTo(DosageExplanationOnJournalTabRoute)
fun Nav3TabManager.navigateToSaferSniffingOnJournalTab() = navigateTo(SaferSniffingOnJournalTabRoute)
fun Nav3TabManager.navigateToVolumetricDosingOnJournalTab() =
    navigateTo(VolumetricDosingOnJournalTabRoute)
fun Nav3TabManager.navigateToURLInJournalTab(url: String) = navigateTo(JournalTabUrlRoute(url))
fun Nav3TabManager.navigateToAddIngestion() = navigateTo(AddIngestionRoute)
fun Nav3TabManager.dismissAddIngestionScreens() = dismissFlow(AddIngestionRoute)

fun Nav3TabManager.navigateToCheckInteractions(substanceName: String) =
    navigateTo(CheckInteractionsRoute(substanceName))
fun Nav3TabManager.navigateToCheckSaferUse(substanceName: String) =
    navigateTo(CheckSaferUseRoute(substanceName))
fun Nav3TabManager.navigateToChooseRouteOfAddIngestion(substanceName: String) =
    navigateTo(ChooseRouteOfAddIngestionRoute(substanceName))
fun Nav3TabManager.navigateToChooseDose(
    substanceName: String,
    administrationRoute: AdministrationRoute
) = navigateTo(ChooseDoseRoute(substanceName, administrationRoute))
fun Nav3TabManager.navigateToChooseDoseCustomUnit(customUnitId: Int) =
    navigateTo(ChooseDoseCustomUnitRoute(customUnitId))
fun Nav3TabManager.navigateToChooseCustomRoute(customSubstanceId: Int) =
    navigateTo(CustomChooseRouteRoute(customSubstanceId))
fun Nav3TabManager.navigateToChooseDoseCustom(
    customSubstanceId: Int,
    administrationRoute: AdministrationRoute
) = navigateTo(CustomChooseDoseRoute(customSubstanceId, administrationRoute))
fun Nav3TabManager.navigateToChooseTime(
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
        administrationRoute,
        isEstimate,
        units,
        dose,
        estimatedDoseStandardDeviation,
        substanceName,
        customUnitId,
        customSubstanceId,
        releaseForm
    )
)

fun Nav3TabManager.navigateToSubstanceScreen(substanceName: String) = navigateTo(SubstanceRoute(substanceName))
fun Nav3TabManager.navigateToCategoryScreen(categoryName: String) = navigateTo(CategoryRoute(categoryName))
fun Nav3TabManager.navigateToEditCustomSubstance(customSubstanceId: Int) =
    navigateTo(EditCustomSubstanceRoute(customSubstanceId))
fun Nav3TabManager.navigateToAddCustomSubstance() = navigateTo(AddCustomSubstanceRoute)
fun Nav3TabManager.navigateToURLOnSubstancesTab(url: String) = navigateTo(SubstancesTabUrlRoute(url))
fun Nav3TabManager.navigateToExplainTimelineOnSubstancesTab() =
    navigateTo(ExplainTimelineOnSubstancesTabRoute)
fun Nav3TabManager.navigateToDosageExplanationOnSubstancesTab() =
    navigateTo(DosageExplanationOnSubstancesTabRoute)
fun Nav3TabManager.navigateToVolumetricDosingOnSubstancesTab() =
    navigateTo(VolumetricDosingOnSubstancesTabRoute)
fun Nav3TabManager.navigateToSaferHallucinogensOnSubstancesTab() =
    navigateTo(SaferHallucinogensOnSubstancesTabRoute)
fun Nav3TabManager.navigateToSaferStimulantsOnSubstancesTab() =
    navigateTo(SaferStimulantsOnSubstancesTabRoute)

fun Nav3TabManager.navigateToSaferHallucinogens() = navigateTo(SaferHallucinogensRoute)
fun Nav3TabManager.navigateToSaferStimulants() = navigateTo(SaferStimulantsRoute)
fun Nav3TabManager.navigateToDosageExplanationOnSaferTab() =
    navigateTo(DosageExplanationOnSaferTabRoute)
fun Nav3TabManager.navigateToAdministrationRouteExplanation() =
    navigateTo(AdministrationRouteExplanationRoute)
fun Nav3TabManager.navigateToDrugTesting() = navigateTo(DrugTestingRoute)
fun Nav3TabManager.navigateToDosageGuide() = navigateTo(DosageGuideRoute)
fun Nav3TabManager.navigateToVolumetricDosingOnSaferTab() =
    navigateTo(VolumetricDosingOnSaferTabRoute)
fun Nav3TabManager.navigateToReagentTesting() = navigateTo(ReagentTestingRoute)
fun Nav3TabManager.navigateToURLOnSaferTab(url: String) = navigateTo(SaferTabUrlRoute(url))

fun Nav3TabManager.navigateToSubstanceCompanionScreen(substanceName: String, consumerName: String?) =
    navigateTo(SubstanceCompanionRoute(substanceName, consumerName))

fun Nav3TabManager.navigateToFAQ() = navigateTo(FAQRoute)
fun Nav3TabManager.navigateToDonate() = navigateTo(DonateRoute)
fun Nav3TabManager.navigateToPreferences() = navigateTo(PreferencesRoute)
fun Nav3TabManager.navigateToIconPicker() = navigateTo(IconPickerRoute)
fun Nav3TabManager.navigateToExtensionPack() = navigateTo(ExtensionPackRoute)
fun Nav3TabManager.navigateToComboSettings() = navigateTo(CombinationSettingsRoute)
fun Nav3TabManager.navigateToSubstanceColors() = navigateTo(SubstanceColorsRoute)
fun Nav3TabManager.navigateToCustomUnits() = navigateTo(CustomUnitsRoute)
fun Nav3TabManager.navigateToCustomUnitArchive() = navigateTo(CustomUnitArchiveRoute)
fun Nav3TabManager.navigateToEditCustomUnit(customUnitId: Int) =
    navigateTo(EditCustomUnitRoute(customUnitId))
fun Nav3TabManager.navigateToAddCustomUnits() = navigateTo(AddCustomUnitsRoute)
fun Nav3TabManager.dismissAddCustomUnits() = dismissFlow(AddCustomUnitsRoute)
fun Nav3TabManager.navigateToChooseRouteOfAddCustomUnit(substanceName: String) =
    navigateTo(ChooseRouteOfAddCustomUnitRoute(substanceName))
fun Nav3TabManager.navigateToFinishAddCustomUnit(
    substanceName: String,
    administrationRoute: AdministrationRoute
) = navigateTo(FinishAddCustomUnitRoute(substanceName, administrationRoute))
