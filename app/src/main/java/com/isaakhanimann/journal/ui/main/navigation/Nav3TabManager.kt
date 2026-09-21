package com.isaakhanimann.journal.ui.main.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import com.isaakhanimann.journal.ui.main.navigation.routes.AddCustomSubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AddCustomUnitsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.AdministrationRouteExplanationRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CategoryRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseRouteOfAddCustomUnitRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CombinationSettingsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CustomUnitArchiveRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CustomUnitsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DonateRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DosageExplanationOnSaferTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DosageExplanationOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DosageGuideRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.DrugTestingRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditCustomSubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.EditCustomUnitRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ExplainTimelineOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ExtensionPackRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.FAQRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.FinishAddCustomUnitRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.IconPickerRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.PreferencesRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ReagentTestingRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferHallucinogensOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferHallucinogensRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferStimulantsOnSubstancesTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferStimulantsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SaferTabUrlRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SettingsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.StatsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceColorsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceCompanionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstancesRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstancesTabUrlRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.TopLevelDestination
import com.isaakhanimann.journal.ui.main.navigation.routes.TopLevelDestinations
import com.isaakhanimann.journal.ui.main.navigation.routes.VolumetricDosingOnSaferTabRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.VolumetricDosingOnSubstancesTabRoute

/**
 * Owns the five independent Nav3 back stacks and their tab-level back behavior.
 *
 * Every stack is a Navigation 3 [NavBackStack], so the whole navigation state — the selected tab,
 * each tab's history and the saved state of its destinations — survives configuration changes and
 * process death. Build it with [rememberNav3TabManager].
 */
class Nav3TabManager(
    private val backStacks: Map<TopLevelDestination, NavBackStack<NavKey>>,
    initialTab: TopLevelDestination,
    private val onTabSelected: (TopLevelDestination) -> Unit = {},
) {
    var selectedTab by mutableStateOf(initialTab)
        private set

    val currentBackStack: NavBackStack<NavKey>
        get() = backStacks.getValue(selectedTab)

    fun backStack(tab: TopLevelDestination): NavBackStack<NavKey> = backStacks.getValue(tab)

    fun switchToTab(tab: TopLevelDestination) {
        selectedTab = tab
        onTabSelected(tab)
    }

    /** Pushes [key] onto the stack of the tab it belongs to, switching tabs when necessary. */
    fun navigate(key: NavKey) {
        tabFor(key)?.let(::switchToTab)
        val stack = currentBackStack
        if (stack.lastOrNull() != key) stack.add(key)
    }

    /**
     * Removes the top entry. Returns false only on the start tab's root, where the caller should
     * let the system finish the activity.
     */
    fun pop(): Boolean {
        val stack = currentBackStack
        if (stack.size > 1) {
            stack.removeAt(stack.lastIndex)
            return true
        }
        if (selectedTab != TopLevelDestinations.Journal) {
            switchToTab(TopLevelDestinations.Journal)
            return true
        }
        return false
    }

    fun popToRoot(tab: TopLevelDestination = selectedTab) {
        val stack = backStacks.getValue(tab)
        while (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    /**
     * Removes [root] and everything above it, ending a multi-screen flow. The tab root itself is
     * never removed: a flow that was somehow pushed as the only entry degrades to [popToRoot].
     */
    fun dismissFlow(root: NavKey): Boolean {
        val stack = currentBackStack
        val index = stack.indexOfLast { it == root }
        if (index < 0) return false
        while (stack.size > index.coerceAtLeast(1)) stack.removeAt(stack.lastIndex)
        return true
    }

    fun isAtRoot(tab: TopLevelDestination = selectedTab): Boolean =
        backStacks.getValue(tab).size == 1

    /**
     * The tab a destination belongs to.
     *
     * Mirrors the nested graphs that grouped these destinations in Navigation 2; everything that is
     * not part of the statistics, substances, safer-use or settings graph belongs to the journal
     * tab, which is also the tab the app starts on.
     */
    private fun tabFor(key: NavKey): TopLevelDestination? = when (key) {
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
        is AddCustomUnitsRoute, is ChooseRouteOfAddCustomUnitRoute, is FinishAddCustomUnitRoute ->
            TopLevelDestinations.Settings
        else -> TopLevelDestinations.Journal
    }
}

/** Creates the tab back stacks and restores the selected tab across process death. */
@Composable
fun rememberNav3TabManager(): Nav3TabManager {
    val backStacks = TopLevelDestinations.all.associateWith { destination ->
        rememberNavBackStack(destination.startRoute)
    }
    var selectedTabLabelKey by rememberSaveable {
        mutableStateOf(TopLevelDestinations.Journal.labelKey)
    }
    return remember(backStacks) {
        Nav3TabManager(
            backStacks = backStacks,
            initialTab = TopLevelDestinations.forLabelKey(selectedTabLabelKey),
            onTabSelected = { selectedTabLabelKey = it.labelKey },
        )
    }
}

/**
 * The entries `NavDisplay` should render: the start tab's stack followed by the selected tab's.
 *
 * Every tab's stack is decorated — also the ones that are not rendered — because the decorators own
 * the saved state and the `ViewModelStore` of the entries in that stack. That is what keeps the
 * unsaved state of a tab (scroll position, half-filled forms) alive while the user is on another
 * tab, which is the behavior `saveState`/`restoreState` gave us before the Navigation 3 migration.
 */
@Composable
fun Nav3TabManager.decoratedEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): List<NavEntry<NavKey>> {
    val decoratedByTab = TopLevelDestinations.all.associateWith { tab ->
        rememberDecoratedNavEntries(
            backStack = backStack(tab),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider,
        )
    }
    return listOf(TopLevelDestinations.Journal, selectedTab)
        .distinct()
        .flatMap { decoratedByTab.getValue(it) }
}
