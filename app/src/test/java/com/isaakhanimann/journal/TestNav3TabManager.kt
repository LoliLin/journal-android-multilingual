package com.isaakhanimann.journal

import com.isaakhanimann.journal.ui.main.navigation.Nav3TabManager
import com.isaakhanimann.journal.ui.main.navigation.TopLevelDestinations
import com.isaakhanimann.journal.ui.main.navigation.routes.ExperienceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.StatsRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceCompanionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TestNav3TabManager {
    @Test
    fun switchingTabsPreservesIndependentStacks() {
        val manager = Nav3TabManager()

        manager.navigate(ExperienceRoute(42))
        manager.switchToTab(TopLevelDestinations.Substances)
        manager.navigate(SubstanceRoute("Caffeine"))

        assertEquals(SubstanceRoute("Caffeine"), manager.currentBackStack.last())
        manager.switchToTab(TopLevelDestinations.Journal)
        assertEquals(ExperienceRoute(42), manager.currentBackStack.last())
        manager.switchToTab(TopLevelDestinations.Substances)
        assertEquals(SubstanceRoute("Caffeine"), manager.currentBackStack.last())
    }

    @Test
    fun popRemovesDetailsThenUnwindsToJournal() {
        val manager = Nav3TabManager()
        manager.navigate(StatsRoute)
        manager.navigate(SubstanceCompanionRoute("LSD", null))

        assertTrue(manager.pop())
        assertEquals(StatsRoute, manager.currentBackStack.last())
        assertTrue(manager.pop())
        assertEquals(TopLevelDestinations.Journal, manager.selectedTab)
        assertFalse(manager.pop())
    }

    @Test
    fun popToRootRemovesOnlyCurrentTabDetails() {
        val manager = Nav3TabManager()
        manager.navigate(ExperienceRoute(7))
        manager.popToRoot()

        assertEquals(1, manager.currentBackStack.size)
        assertEquals(TopLevelDestinations.Journal.startRoute, manager.currentBackStack.single())
    }
}
