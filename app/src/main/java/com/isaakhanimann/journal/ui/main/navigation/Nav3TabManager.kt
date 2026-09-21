package com.isaakhanimann.journal.ui.main.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.isaakhanimann.journal.ui.main.navigation.routes.AddCustomUnitsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AddIngestionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AdministrationRouteExplanationRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CategoryRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseRouteOfAddCustomUnitRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseRouteOfAddIngestionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CustomChooseDoseRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CustomChooseRouteRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AddCustomUnitsSearchSubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DosageExplanationOnSaferTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DosageExplanationOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DosageGuideRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DrugTestingRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditCustomSubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditCustomUnitRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditExperienceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditRatingRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditTimedNoteRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ExplainTimelineOnJournalTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ExplainTimelineOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ExtensionPackRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.FAQRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.FinishAddCustomUnitRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CustomUnitArchiveRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.IconPickerRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.IngestionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.JournalTabUrlRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.StatsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceCompanionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstancesTabUrlRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.TopLevelDestination
import com.isaakhanimann.journal.ui.main.navigation.routes.TopLevelDestinations
import com.isaakhanimann.journal.ui.main.navigation.routes.VolumetricDosingOnJournalTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.VolumetricDosingOnSaferTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.VolumetricDosingOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AddCustomSubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CalendarRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CheckInteractionsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CheckSaferUseRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseDoseCustomUnitRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseDoseRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseTimeRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CombinationSettingsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DonateRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ExperienceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.PreferencesRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.QuickTimedNoteRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ReagentTestingRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferHallucinogensOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferHallucinogensRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferSniffingOnJournalTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferStimulantsOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferStimulantsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferTabUrlRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SettingsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstancesRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.TimeCapsuleRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AddRatingRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AddTimedNoteRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DosageExplanationOnJournalTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CustomUnitsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceColorsRoute

/** Owns the five independent Nav3 back stacks and their tab-level back behavior. */
class Nav3TabManager(
    destinations: List<TopLevelDestination> = TopLevelDestinations.all
) {
    private val destinations = destinations.toSet()
    private val stacks = destinations.associateWith { destination ->
        mutableStateListOf<Any>(destination.startRoute)
    }

    var selectedTab by mutableStateOf(TopLevelDestinations.Journal)
        private set

    val currentBackStack: SnapshotStateList<Any>
        get() = stacks.getValue(selectedTab)

    fun backStack(tab: TopLevelDestination): SnapshotStateList<Any> = stacks.getValue(tab)

    fun switchToTab(tab: TopLevelDestination) {
        require(tab in destinations) { "Unknown top-level destination: $tab" }
        selectedTab = tab
    }

    fun navigate(key: Any) {
        val tab = tabFor(key)
        if (tab != null) selectedTab = tab
        val stack = currentBackStack
        if (stack.lastOrNull() != key) stack.add(key)
    }

    fun pop(): Boolean {
        val stack = currentBackStack
        if (stack.size > 1) {
            stack.removeAt(stack.lastIndex)
            return true
        }
        if (selectedTab != TopLevelDestinations.Journal) {
            selectedTab = TopLevelDestinations.Journal
            return true
        }
        return false
    }

    fun popToRoot(tab: TopLevelDestination = selectedTab) {
        val stack = stacks.getValue(tab)
        while (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    fun dismissFlow(root: Any): Boolean {
        val stack = currentBackStack
        val index = stack.indexOfLast { it == root }
        if (index < 0) return false
        while (stack.size > index) stack.removeAt(stack.lastIndex)
        return true
    }

    fun isAtRoot(tab: TopLevelDestination = selectedTab): Boolean =
        stacks.getValue(tab).size == 1

    private fun tabFor(key: Any): TopLevelDestination? = when (key) {
        is StatsRoute, is SubstanceCompanionRoute -> TopLevelDestinations.Stats
        is SubstancesRoute, is SubstanceRoute, is CategoryRoute, is EditCustomSubstanceRoute,
        is AddCustomSubstanceRoute, is SubstancesTabUrlRoute,
        is ExplainTimelineOnSubstancesTabRoute, is DosageExplanationOnSubstancesTabRoute,
        is VolumetricDosingOnSubstancesTabRoute, is SaferHallucinogensOnSubstancesTabRoute,
        is SaferStimulantsOnSubstancesTabRoute -> TopLevelDestinations.Substances
        is SaferRoute, is SaferHallucinogensRoute, is SaferStimulantsRoute,
        is DosageExplanationOnSaferTabRoute, is AdministrationRouteExplanationRoute,
        is DrugTestingRoute, is DosageGuideRoute, is VolumetricDosingOnSaferTabRoute,
        is ReagentTestingRoute, is SaferTabUrlRoute -> TopLevelDestinations.Safer
        is SettingsRoute, is FAQRoute, is DonateRoute, is PreferencesRoute, is IconPickerRoute,
        is ExtensionPackRoute, is CombinationSettingsRoute, is SubstanceColorsRoute,
        is CustomUnitsRoute, is CustomUnitArchiveRoute, is EditCustomUnitRoute,
        is AddCustomUnitsRoute, is AddCustomUnitsSearchSubstanceRoute,
        is ChooseRouteOfAddCustomUnitRoute, is FinishAddCustomUnitRoute ->
            TopLevelDestinations.Settings
        else -> TopLevelDestinations.Journal
    }
}
