package com.isaakhanimann.journal

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.isaakhanimann.journal.ui.main.navigation.Nav3TabManager
import com.isaakhanimann.journal.ui.main.navigation.routes.AddIngestionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseRouteOfAddIngestionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ExperienceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.FAQRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.JournalRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.StatsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceCompanionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.TopLevelDestination
import com.isaakhanimann.journal.ui.main.navigation.routes.TopLevelDestinations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TestNav3TabManager {

    private fun manager(): Nav3TabManager = Nav3TabManager(
        backStacks = TopLevelDestinations.all.associateWith { tab ->
            NavBackStack<NavKey>(tab.startRoute)
        },
        initialTab = TopLevelDestinations.Journal
    )

    private fun Nav3TabManager.top(tab: TopLevelDestination) = backStack(tab).last()

    @Test
    fun switchingTabsKeepsIndependentStacks() {
        val manager = manager()

        manager.navigate(ExperienceRoute(42))
        manager.switchToTab(TopLevelDestinations.Substances)
        manager.navigate(SubstanceRoute("Caffeine"))
        assertEquals(SubstanceRoute("Caffeine"), manager.top(TopLevelDestinations.Substances))

        manager.switchToTab(TopLevelDestinations.Journal)
        assertEquals(ExperienceRoute(42), manager.currentBackStack.last())

        manager.switchToTab(TopLevelDestinations.Substances)
        assertEquals(SubstanceRoute("Caffeine"), manager.currentBackStack.last())
    }

    @Test
    fun navigatingToARouteSelectsTheTabThatOwnsIt() {
        val manager = manager()

        manager.navigate(StatsRoute)
        assertEquals(TopLevelDestinations.Stats, manager.selectedTab)

        manager.navigate(SubstanceCompanionRoute("LSD", null))
        assertEquals(TopLevelDestinations.Stats, manager.selectedTab)
        assertEquals(1, manager.backStack(TopLevelDestinations.Journal).size)

        manager.navigate(FAQRoute)
        assertEquals(TopLevelDestinations.Settings, manager.selectedTab)

        manager.navigate(AddIngestionRoute)
        assertEquals(TopLevelDestinations.Journal, manager.selectedTab)
    }

    @Test
    fun popRemovesTopEntryThenLeavesTab() {
        val manager = manager()
        manager.navigate(AddIngestionRoute)

        assertTrue(manager.pop())
        assertEquals(JournalRoute, manager.currentBackStack.last())
        assertTrue(manager.currentBackStack.size == 1)

        assertFalse(manager.pop())
    }

    @Test
    fun popOnAnotherTabsRootReturnsToJournal() {
        val manager = manager()
        manager.switchToTab(TopLevelDestinations.Settings)

        assertTrue(manager.pop())
        assertEquals(TopLevelDestinations.Journal, manager.selectedTab)
        assertFalse(manager.pop())
    }

    @Test
    fun popToRootRemovesOnlyThatTabsDetails() {
        val manager = manager()
        manager.navigate(ExperienceRoute(7))
        manager.switchToTab(TopLevelDestinations.Substances)
        manager.navigate(SubstanceRoute("LSD"))

        manager.popToRoot(TopLevelDestinations.Journal)

        assertEquals(listOf(JournalRoute), manager.backStack(TopLevelDestinations.Journal).toList())
        assertEquals(
            listOf(TopLevelDestinations.Substances.startRoute, SubstanceRoute("LSD")),
            manager.backStack(TopLevelDestinations.Substances).toList()
        )
    }

    @Test
    fun dismissFlowRemovesFlowAndAboveButKeepsTabRoot() {
        val manager = manager()
        manager.navigate(AddIngestionRoute)
        manager.navigate(ChooseRouteOfAddIngestionRoute("DMT"))

        assertTrue(manager.dismissFlow(AddIngestionRoute))

        assertEquals(listOf(JournalRoute), manager.currentBackStack.toList())
        assertFalse(manager.dismissFlow(AddIngestionRoute))
    }
}
