package com.isaakhanimann.journal.ui.main

/** The pinned bar stays visible on every destination, except while the keyboard is open. */
internal fun shouldShowBottomBar(
    isAtTabRoot: Boolean,
    isKeyboardOpen: Boolean,
    isPinned: Boolean = false
): Boolean = (isAtTabRoot || isPinned) && !isKeyboardOpen
