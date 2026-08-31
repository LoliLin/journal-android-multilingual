package com.isaakhanimann.journal.ui.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity

/**
 * Nested-scroll connection owned by [MainScreen] so the bottom bar can hide/show
 * with tab-root content. Each tab Scaffold must attach this; a connection on
 * [androidx.navigation.compose.NavHost] does not receive descendant LazyColumn
 * / verticalScroll events through Navigation Compose.
 */
val LocalBottomBarNestedScrollConnection =
    compositionLocalOf<NestedScrollConnection?> { null }

/**
 * Remaining visible height of the overlay NavigationBar, in px.
 * Tab lists/FABs use this as content padding so items aren't covered, while
 * the page background fills the screen (no reserved empty slot).
 */
val LocalBottomBarOverlayInsetPx = staticCompositionLocalOf { 0 }

@Composable
fun Modifier.bottomBarNestedScroll(): Modifier {
    val connection = LocalBottomBarNestedScrollConnection.current
    return if (connection != null) nestedScroll(connection) else this
}

@Composable
fun bottomBarOverlayPadding(): PaddingValues {
    val px = LocalBottomBarOverlayInsetPx.current
    return PaddingValues(bottom = with(LocalDensity.current) { px.toDp() })
}

@Composable
fun bottomBarOverlayDp() = with(LocalDensity.current) {
    LocalBottomBarOverlayInsetPx.current.toDp()
}
