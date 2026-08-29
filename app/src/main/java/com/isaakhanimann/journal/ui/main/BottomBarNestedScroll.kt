package com.isaakhanimann.journal.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll

/**
 * Nested-scroll connection owned by [MainScreen] so the bottom bar can hide/show
 * with tab-root content. Each tab Scaffold must attach this; a connection on
 * [androidx.navigation.compose.NavHost] does not receive descendant LazyColumn
 * / verticalScroll events through Navigation Compose.
 */
val LocalBottomBarNestedScrollConnection =
    compositionLocalOf<NestedScrollConnection?> { null }

@Composable
fun Modifier.bottomBarNestedScroll(): Modifier {
    val connection = LocalBottomBarNestedScrollConnection.current
    return if (connection != null) nestedScroll(connection) else this
}
