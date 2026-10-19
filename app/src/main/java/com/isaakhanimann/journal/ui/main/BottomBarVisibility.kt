package com.isaakhanimann.journal.ui.main

/**
 * Root tabs account for the bottom bar's overlay inset, but nested screens do not.
 * Pinning only disables hide-on-scroll; it must not expose the bar on nested screens.
 */
internal fun shouldShowBottomBar(
    isAtTabRoot: Boolean,
    isKeyboardOpen: Boolean
): Boolean = isAtTabRoot && !isKeyboardOpen
