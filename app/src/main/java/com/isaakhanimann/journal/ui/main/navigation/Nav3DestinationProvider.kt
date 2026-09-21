package com.isaakhanimann.journal.ui.main.navigation

import androidx.navigation3.runtime.entryProvider
import com.isaakhanimann.journal.ui.VOLUMETRIC_DOSE_ARTICLE_URL
import com.isaakhanimann.journal.ui.main.navigation.routes.AddCustomSubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AddCustomUnitsSearchSubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AddIngestionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AddRatingRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AddTimedNoteRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AddIngestionSearchRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AdministrationRouteExplanationRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CalendarRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CategoryRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CheckInteractionsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CheckSaferUseRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseDoseCustomUnitRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseDoseRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseRouteOfAddCustomUnitRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseRouteOfAddIngestionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseTimeRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CombinationSettingsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CustomChooseDoseRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CustomChooseRouteRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CustomUnitArchiveRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CustomUnitsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DonateRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DosageExplanationOnJournalTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DosageExplanationOnSaferTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DosageExplanationOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DosageGuideRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DrugTestingRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditCustomSubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditCustomUnitRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditExperienceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditRatingRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditTimedNoteRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ExperienceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ExplainTimelineOnJournalTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ExplainTimelineOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ExtensionPackRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.FAQRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.FinishAddCustomUnitRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.IconPickerRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.IngestionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.JournalRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.JournalTabUrlRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.PreferencesRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.QuickTimedNoteRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ReagentTestingRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferHallucinogensOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferHallucinogensRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferSniffingOnJournalTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferStimulantsOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferStimulantsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferTabUrlRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SettingsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.StatsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceCompanionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceColorsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstancesRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstancesTabUrlRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.TimeCapsuleRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.TimelineScreenRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.VolumetricDosingOnJournalTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.VolumetricDosingOnSaferTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.VolumetricDosingOnSubstancesTabRoute
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
import com.isaakhanimann.journal.ui.tabs.safer.DoseGuideScreen
import com.isaakhanimann.journal.ui.tabs.safer.DrugTestingScreen
import com.isaakhanimann.journal.ui.tabs.safer.ReagentTestingScreen
import com.isaakhanimann.journal.ui.tabs.safer.RouteExplanationScreen
import com.isaakhanimann.journal.ui.tabs.safer.SaferHallucinogensScreen
import com.isaakhanimann.journal.ui.tabs.safer.SaferUseScreen
import com.isaakhanimann.journal.ui.tabs.safer.VolumetricDosingScreen
import com.isaakhanimann.journal.ui.tabs.search.SearchScreen
import com.isaakhanimann.journal.ui.tabs.search.custom.AddCustomSubstance
import com.isaakhanimann.journal.ui.tabs.search.custom.EditCustomSubstance
import com.isaakhanimann.journal.ui.tabs.search.substance.SaferStimulantsScreen
import com.isaakhanimann.journal.ui.tabs.search.substance.SaferSniffingScreen
import com.isaakhanimann.journal.ui.tabs.search.substance.SubstanceScreen
import com.isaakhanimann.journal.ui.tabs.search.substance.UrlScreen
import com.isaakhanimann.journal.ui.tabs.search.substance.category.CategoryScreen
import com.isaakhanimann.journal.ui.tabs.settings.DonateScreen
import com.isaakhanimann.journal.ui.tabs.settings.ExtensionPackScreen
import com.isaakhanimann.journal.ui.tabs.settings.FAQScreen
import com.isaakhanimann.journal.ui.tabs.settings.IconPickerScreen
import com.isaakhanimann.journal.ui.tabs.settings.PreferencesScreen
import com.isaakhanimann.journal.ui.tabs.settings.SettingsScreen
import com.isaakhanimann.journal.ui.tabs.settings.colors.SubstanceColorsScreen
import com.isaakhanimann.journal.ui.tabs.settings.combinations.CombinationSettingsScreen
import com.isaakhanimann.journal.ui.tabs.settings.customunits.CustomUnitsScreen
import com.isaakhanimann.journal.ui.tabs.settings.customunits.add.ChooseRouteDuringAddCustomUnitScreen
import com.isaakhanimann.journal.ui.tabs.settings.customunits.add.ChooseSubstanceScreen
import com.isaakhanimann.journal.ui.tabs.settings.customunits.add.FinishAddCustomUnitScreen
import com.isaakhanimann.journal.ui.tabs.settings.customunits.archive.CustomUnitArchiveScreen
import com.isaakhanimann.journal.ui.tabs.settings.customunits.edit.EditCustomUnitScreen
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.dose.ChooseDoseScreen
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.dose.customsubstance.CustomChooseDose
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.dose.customunit.ChooseDoseCustomUnitScreen
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.interactions.CheckInteractionsScreen
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.route.ChooseRouteScreen
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.route.CustomSubstanceChooseRouteScreen
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.saferuse.CheckSaferUseScreen
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.search.AddIngestionSearchScreen
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.time.FinishIngestionScreen

/** Registers every app destination with Nav3's key-to-content provider. */
fun nav3EntryProvider(manager: Nav3TabManager) = entryProvider<Any> {
    entry<JournalRoute> {
        JournalScreen(
            navigateToExperiencePopNothing = { manager.navigate(ExperienceRoute(it)) },
            navigateToAddIngestion = { manager.navigate(com.isaakhanimann.journal.ui.main.navigation.routes.AddIngestionRoute) },
            navigateToCalendar = { manager.navigate(CalendarRoute) },
            navigateToQuickTimedNote = { manager.navigate(QuickTimedNoteRoute(it)) }
        )
    }
    entry<EditExperienceRoute> { EditExperienceScreen(navigateBack = { manager.pop() }) }
    entry<AddRatingRoute> { AddRatingScreen(navigateBack = { manager.pop() }) }
    entry<AddTimedNoteRoute> { AddTimedNoteScreen(navigateBack = { manager.pop() }) }
    entry<QuickTimedNoteRoute> { route -> QuickTimedNoteScreen(navigateBack = { manager.pop() }) }
    entry<TimeCapsuleRoute> {
        TimeCapsuleScreen(
            navigateBack = { manager.pop() },
            navigateToExperience = { manager.navigate(ExperienceRoute(it)) }
        )
    }
    entry<EditRatingRoute> { EditRatingScreen(navigateBack = { manager.pop() }) }
    entry<EditTimedNoteRoute> { EditTimedNoteScreen(navigateBack = { manager.pop() }) }
    entry<TimelineScreenRoute> { TimelineScreen() }
    entry<VolumetricDosingOnJournalTabRoute> {
        VolumetricDosingScreen {
            manager.navigate(JournalTabUrlRoute(VOLUMETRIC_DOSE_ARTICLE_URL))
        }
    }
    entry<ExperienceRoute> { route ->
        OneExperienceScreen(
            navigateToAddIngestionSearch = { manager.navigate(com.isaakhanimann.journal.ui.main.navigation.routes.AddIngestionRoute) },
            navigateToExplainTimeline = { manager.navigate(ExplainTimelineOnJournalTabRoute) },
            navigateToEditExperienceScreen = { manager.navigate(EditExperienceRoute(route.experienceId)) },
            navigateToIngestionScreen = { manager.navigate(IngestionRoute(it)) },
            navigateBack = { manager.pop() },
            navigateToAddRatingScreen = { manager.navigate(AddRatingRoute(route.experienceId)) },
            navigateToAddTimedNoteScreen = { manager.navigate(AddTimedNoteRoute(route.experienceId)) },
            navigateToURL = { manager.navigate(JournalTabUrlRoute(it)) },
            navigateToEditRatingScreen = { manager.navigate(EditRatingRoute(it)) },
            navigateToTimelineScreen = { manager.navigate(TimelineScreenRoute(it, route.experienceId)) },
            navigateToEditTimedNoteScreen = {
                manager.navigate(EditTimedNoteRoute(it, route.experienceId))
            }
        )
    }
    entry<IngestionRoute> { EditIngestionScreen(navigateBack = { manager.pop() }) }
    entry<JournalTabUrlRoute> { route -> UrlScreen(url = route.url, onHandled = { manager.pop() }) }
    entry<ExplainTimelineOnJournalTabRoute> { ExplainTimelineScreen() }
    entry<DosageExplanationOnJournalTabRoute> { DoseExplanationScreen() }
    entry<SaferSniffingOnJournalTabRoute> { SaferSniffingScreen() }
    entry<CalendarRoute> { CalendarJournalScreen { manager.navigate(ExperienceRoute(it)) } }
    entry<AddIngestionRoute> {
        com.isaakhanimann.journal.ui.tabs.journal.addingestion.search.AddIngestionSearchScreen(
            navigateToCheckInteractions = { manager.navigate(CheckInteractionsRoute(it)) },
            navigateToCheckSaferUse = { manager.navigate(CheckSaferUseRoute(it)) },
            navigateToCustomSubstanceChooseRoute = {
                manager.navigate(CustomChooseRouteRoute(it))
            },
            navigateToChooseTime = { substanceName, route, dose, units, estimate, sd, customUnit, releaseForm ->
                manager.navigate(
                    ChooseTimeRoute(route, estimate, units, dose, sd, substanceName, customUnitId = customUnit, releaseForm = releaseForm)
                )
            },
            navigateToCustomDose = { customSubstanceId, route ->
                manager.navigate(CustomChooseDoseRoute(customSubstanceId, route))
            },
            navigateToDose = { substanceName, route ->
                manager.navigate(ChooseDoseRoute(substanceName, route))
            },
            navigateToChooseRoute = { manager.navigate(ChooseRouteOfAddIngestionRoute(it)) },
            navigateToAddCustomSubstanceScreen = { manager.navigate(AddCustomSubstanceRoute) },
            navigateToCustomUnitChooseDose = { manager.navigate(ChooseDoseCustomUnitRoute(it)) }
        )
    }

    entry<AddIngestionSearchRoute> {
        com.isaakhanimann.journal.ui.tabs.journal.addingestion.search.AddIngestionSearchScreen(
            navigateToCheckInteractions = { manager.navigate(CheckInteractionsRoute(it)) },
            navigateToCheckSaferUse = { manager.navigate(CheckSaferUseRoute(it)) },
            navigateToCustomSubstanceChooseRoute = {
                manager.navigate(CustomChooseRouteRoute(it))
            },
            navigateToChooseTime = { substanceName, route, dose, units, estimate, sd, customUnit, releaseForm ->
                manager.navigate(
                    ChooseTimeRoute(route, estimate, units, dose, sd, substanceName, customUnit, releaseForm = releaseForm)
                )
            },
            navigateToCustomDose = { customSubstanceId, route ->
                manager.navigate(CustomChooseDoseRoute(customSubstanceId, route))
            },
            navigateToDose = { substanceName, route -> manager.navigate(ChooseDoseRoute(substanceName, route)) },
            navigateToChooseRoute = { manager.navigate(ChooseRouteOfAddIngestionRoute(it)) },
            navigateToAddCustomSubstanceScreen = { manager.navigate(AddCustomSubstanceRoute) },
            navigateToCustomUnitChooseDose = { manager.navigate(ChooseDoseCustomUnitRoute(it)) }
        )
    }
    entry<CheckInteractionsRoute> { route ->
        CheckInteractionsScreen(
            navigateToNext = { manager.navigate(ChooseRouteOfAddIngestionRoute(route.substanceName)) },
            navigateToURL = { manager.navigate(JournalTabUrlRoute(it)) }
        )
    }
    entry<CheckSaferUseRoute> { route ->
        CheckSaferUseScreen { manager.navigate(CheckInteractionsRoute(route.substanceName)) }
    }
    entry<ChooseDoseCustomUnitRoute> {
        ChooseDoseCustomUnitScreen { administrationRoute, units, estimate, dose, sd, substanceName, customUnitId ->
            manager.navigate(ChooseTimeRoute(administrationRoute, estimate, units, dose, sd, substanceName, customUnitId = customUnitId))
        }
    }
    entry<ChooseRouteOfAddIngestionRoute> { route ->
        ChooseRouteScreen(
            navigateToChooseDose = { manager.navigate(ChooseDoseRoute(route.substanceName, it)) },
            navigateToURL = { manager.navigate(JournalTabUrlRoute(it)) },
            navigateToRouteExplanationScreen = { manager.navigate(AdministrationRouteExplanationRoute) }
        )
    }
    entry<CustomChooseRouteRoute> { route ->
        CustomSubstanceChooseRouteScreen { manager.navigate(CustomChooseDoseRoute(route.customSubstanceId, it)) }
    }
    entry<CustomChooseDoseRoute> { route ->
        CustomChooseDose(
            navigateToChooseTimeAndMaybeColor = { units, estimate, dose, sd ->
                manager.navigate(ChooseTimeRoute(route.administrationRoute, estimate, units, dose, sd, customSubstanceId = route.customSubstanceId))
            },
            navigateToSaferSniffingScreen = { manager.navigate(SaferSniffingOnJournalTabRoute) },
            navigateToURL = { manager.navigate(JournalTabUrlRoute(it)) }
        )
    }
    entry<ChooseDoseRoute> { route ->
        ChooseDoseScreen(
            navigateToChooseTimeAndMaybeColor = { units, estimate, dose, sd, customUnitId ->
                manager.navigate(ChooseTimeRoute(route.administrationRoute, estimate, units, dose, sd, route.substanceName, customUnitId = customUnitId))
            },
            navigateToVolumetricDosingScreenOnJournalTab = { manager.navigate(VolumetricDosingOnJournalTabRoute) },
            navigateToSaferSniffingScreen = { manager.navigate(SaferSniffingOnJournalTabRoute) },
            navigateToURL = { manager.navigate(JournalTabUrlRoute(it)) }
        )
    }
    entry<ChooseTimeRoute> { FinishIngestionScreen { manager.dismissAddIngestionScreens() } }

    entry<StatsRoute> {
        StatsScreen { substanceName, consumerName ->
            manager.navigate(SubstanceCompanionRoute(substanceName, consumerName))
        }
    }
    entry<SubstanceCompanionRoute> {
        SubstanceCompanionScreen(
            navigateToCategoryScreen = { manager.navigate(CategoryRoute(it)) },
            navigateToSubstanceScreen = { manager.navigate(SubstanceRoute(it)) },
            navigateToIngestion = { manager.navigate(IngestionRoute(it)) }
        )
    }

    entry<SubstancesRoute> {
        SearchScreen(
            onSubstanceTap = { manager.navigate(SubstanceRoute(it.name)) },
            onCustomSubstanceTap = { manager.navigate(EditCustomSubstanceRoute(it)) },
            navigateToAddCustomSubstanceScreen = { manager.navigate(AddCustomSubstanceRoute) }
        )
    }
    entry<SubstanceRoute> {
        SubstanceScreen(
            navigateToDosageExplanationScreen = { manager.navigate(DosageExplanationOnSubstancesTabRoute) },
            navigateToSaferHallucinogensScreen = { manager.navigate(SaferHallucinogensOnSubstancesTabRoute) },
            navigateToSaferStimulantsScreen = { manager.navigate(SaferStimulantsOnSubstancesTabRoute) },
            navigateToExplainTimeline = { manager.navigate(ExplainTimelineOnSubstancesTabRoute) },
            navigateToCategoryScreen = { manager.navigate(CategoryRoute(it)) },
            navigateToVolumetricDosingScreen = { manager.navigate(VolumetricDosingOnSubstancesTabRoute) },
            navigateToArticle = { manager.navigate(SubstancesTabUrlRoute(it)) },
            navigateToSubstanceScreen = { manager.navigate(SubstanceRoute(it)) }
        )
    }
    entry<SubstancesTabUrlRoute> { route -> UrlScreen(route.url) { manager.pop() } }
    entry<CategoryRoute> {
        CategoryScreen(
            navigateToURL = { manager.navigate(SubstancesTabUrlRoute(it)) },
            onSubstanceTap = { manager.navigate(SubstanceRoute(it.name)) }
        )
    }
    entry<EditCustomSubstanceRoute> { EditCustomSubstance { manager.pop() } }
    entry<AddCustomSubstanceRoute> { AddCustomSubstance { manager.pop() } }
    entry<VolumetricDosingOnSubstancesTabRoute> {
        VolumetricDosingScreen { manager.navigate(SubstancesTabUrlRoute(VOLUMETRIC_DOSE_ARTICLE_URL)) }
    }
    entry<ExplainTimelineOnSubstancesTabRoute> { ExplainTimelineScreen() }
    entry<DosageExplanationOnSubstancesTabRoute> { DoseExplanationScreen() }
    entry<SaferHallucinogensOnSubstancesTabRoute> { SaferHallucinogensScreen() }
    entry<SaferStimulantsOnSubstancesTabRoute> { SaferStimulantsScreen() }

    entry<SaferRoute> {
        SaferUseScreen(
            navigateToDrugTestingScreen = { manager.navigate(DrugTestingRoute) },
            navigateToSaferHallucinogensScreen = { manager.navigate(SaferHallucinogensRoute) },
            navigateToVolumetricDosingScreen = { manager.navigate(VolumetricDosingOnSaferTabRoute) },
            navigateToDosageGuideScreen = { manager.navigate(DosageGuideRoute) },
            navigateToDosageClassificationScreen = { manager.navigate(DosageExplanationOnSaferTabRoute) },
            navigateToRouteExplanationScreen = { manager.navigate(AdministrationRouteExplanationRoute) },
            navigateToURL = { manager.navigate(SaferTabUrlRoute(it)) },
            navigateToReagentTestingScreen = { manager.navigate(ReagentTestingRoute) }
        )
    }
    entry<SaferHallucinogensRoute> { SaferHallucinogensScreen() }
    entry<SaferStimulantsRoute> { SaferStimulantsScreen() }
    entry<DosageExplanationOnSaferTabRoute> { DoseExplanationScreen() }
    entry<AdministrationRouteExplanationRoute> {
        RouteExplanationScreen { manager.navigate(SaferTabUrlRoute(it)) }
    }
    entry<SaferTabUrlRoute> { route -> UrlScreen(route.url) { manager.pop() } }
    entry<DrugTestingRoute> { DrugTestingScreen() }
    entry<DosageGuideRoute> {
        DoseGuideScreen(
            navigateToDoseClassification = { manager.navigate(DosageExplanationOnSaferTabRoute) },
            navigateToVolumetricDosing = { manager.navigate(VolumetricDosingOnSaferTabRoute) },
            navigateToPWDosageArticle = { manager.navigate(SaferTabUrlRoute("https://psychonautwiki.org/wiki/Dosage")) }
        )
    }
    entry<VolumetricDosingOnSaferTabRoute> {
        VolumetricDosingScreen { manager.navigate(SaferTabUrlRoute(VOLUMETRIC_DOSE_ARTICLE_URL)) }
    }
    entry<ReagentTestingRoute> {
        ReagentTestingScreen {
            manager.navigate(SaferTabUrlRoute("https://psychonautwiki.org/wiki/Reagent_testing_kits"))
        }
    }

    entry<SettingsRoute> {
        SettingsScreen(
            navigateToFAQ = { manager.navigate(FAQRoute) },
            navigateToComboSettings = { manager.navigate(CombinationSettingsRoute) },
            navigateToSubstanceColors = { manager.navigate(SubstanceColorsRoute) },
            navigateToCustomUnits = { manager.navigate(CustomUnitsRoute) },
            navigateToDonate = { manager.navigate(DonateRoute) },
            navigateToExtensionPack = { manager.navigate(ExtensionPackRoute) },
            navigateToIconPicker = { manager.navigate(IconPickerRoute) },
            navigateToPreferences = { manager.navigate(PreferencesRoute) }
        )
    }
    entry<FAQRoute> { FAQScreen() }
    entry<DonateRoute> { DonateScreen() }
    entry<PreferencesRoute> {
        PreferencesScreen(
            navigateBack = { manager.pop() },
            navigateToIconPicker = { manager.navigate(IconPickerRoute) }
        )
    }
    entry<IconPickerRoute> { IconPickerScreen() }
    entry<com.isaakhanimann.journal.ui.main.navigation.routes.AddCustomUnitsRoute> {
        ChooseSubstanceScreen {
            manager.navigate(ChooseRouteOfAddCustomUnitRoute(it))
        }
    }
    entry<ExtensionPackRoute> { ExtensionPackScreen() }
    entry<CombinationSettingsRoute> { CombinationSettingsScreen() }
    entry<SubstanceColorsRoute> { SubstanceColorsScreen() }
    entry<CustomUnitArchiveRoute> {
        CustomUnitArchiveScreen { manager.navigate(EditCustomUnitRoute(it)) }
    }
    entry<CustomUnitsRoute> {
        CustomUnitsScreen(
            navigateToAddCustomUnit = { manager.navigate(com.isaakhanimann.journal.ui.main.navigation.routes.AddCustomUnitsRoute) },
            navigateToEditCustomUnit = { manager.navigate(EditCustomUnitRoute(it)) },
            navigateToCustomUnitArchive = { manager.navigate(CustomUnitArchiveRoute) }
        )
    }
    entry<EditCustomUnitRoute> { EditCustomUnitScreen { manager.pop() } }
    entry<AddCustomUnitsSearchSubstanceRoute> {
        ChooseSubstanceScreen { manager.navigate(ChooseRouteOfAddCustomUnitRoute(it)) }
    }
    entry<ChooseRouteOfAddCustomUnitRoute> { route ->
        ChooseRouteDuringAddCustomUnitScreen {
            manager.navigate(FinishAddCustomUnitRoute(route.substanceName, it))
        }
    }
    entry<FinishAddCustomUnitRoute> { FinishAddCustomUnitScreen { manager.dismissAddCustomUnits() } }
}
