package com.isaakhanimann.journal

import com.isaakhanimann.journal.ui.main.shouldShowBottomBar
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TestBottomBarVisibility {
    @Test
    fun detailScreensNeverShowTheBottomBar() {
        // This remains false regardless of the user's pinning preference.
        assertFalse(shouldShowBottomBar(isAtTabRoot = false, isKeyboardOpen = false))
        assertFalse(shouldShowBottomBar(isAtTabRoot = false, isKeyboardOpen = true))
    }

    @Test
    fun rootTabsShowTheBottomBarOnlyWhenKeyboardIsClosed() {
        assertTrue(shouldShowBottomBar(isAtTabRoot = true, isKeyboardOpen = false))
        assertFalse(shouldShowBottomBar(isAtTabRoot = true, isKeyboardOpen = true))
    }
}
