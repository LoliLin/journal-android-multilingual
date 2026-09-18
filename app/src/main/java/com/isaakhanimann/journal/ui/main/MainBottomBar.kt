@file:OptIn(ExperimentalMaterial3Api::class)

package com.isaakhanimann.journal.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.main.navigation.routes.TopLevelDestination
import com.isaakhanimann.journal.ui.main.navigation.routes.TopLevelDestinations

private const val BAR_ANIMATION_MS = 250

/**
 * Hide-on-scroll behavior for the bottom bar, using the official Material3 implementation.
 */
@Composable
fun rememberBottomBarScrollBehavior(canScroll: () -> Boolean): BottomAppBarScrollBehavior =
    BottomAppBarDefaults.exitAlwaysScrollBehavior(canScroll = canScroll)

/**
 * The nested-scroll connection that drives [behavior].
 *
 * On top of the official connection an `onPreScroll` hook is added so an upward swipe at the top of
 * a list (`available.y > 0` while the bar is still collapsed) reveals the bar again instead of being
 * swallowed by the list.
 */
fun bottomBarNestedScrollConnection(
    behavior: BottomAppBarScrollBehavior
): NestedScrollConnection {
    val official = behavior.nestedScrollConnection
    return object : NestedScrollConnection {
        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            val state = behavior.state
            if (available.y > 0f && state.heightOffset < 0f) {
                val next = (state.heightOffset + available.y)
                    .coerceIn(state.heightOffsetLimit, 0f)
                val consumedY = next - state.heightOffset
                state.heightOffset = next
                return Offset(0f, consumedY)
            }
            return Offset.Zero
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset = official.onPostScroll(consumed, available, source)
    }
}

/**
 * Bottom navigation bar of the five top-level tabs.
 *
 * The bar is an overlay: the page background stays full-screen and tab content adds
 * [bottomBarOverlayPadding] so lists are not covered.
 */
@Composable
fun BottomNavigationBar(
    visible: Boolean,
    selectedDestination: TopLevelDestination?,
    scrollBehavior: BottomAppBarScrollBehavior,
    isPinned: Boolean,
    onTabSelected: (TopLevelDestination) -> Unit,
    onMeasuredHeightChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(tween(durationMillis = BAR_ANIMATION_MS)) { it } +
            expandVertically(
                tween(durationMillis = BAR_ANIMATION_MS),
                expandFrom = Alignment.Bottom
            ),
        exit = slideOutVertically(tween(durationMillis = BAR_ANIMATION_MS)) { it } +
            shrinkVertically(
                tween(durationMillis = BAR_ANIMATION_MS),
                shrinkTowards = Alignment.Bottom
            )
    ) {
        NavigationBar(
            modifier = Modifier
                .onSizeChanged { size ->
                    onMeasuredHeightChanged(size.height)
                    scrollBehavior.state.heightOffsetLimit = -size.height.toFloat()
                }
                .offset {
                    IntOffset(
                        x = 0,
                        y = if (isPinned) 0 else -scrollBehavior.state.heightOffset.toInt()
                    )
                }
        ) {
            TopLevelDestinations.all.forEach { destination ->
                val isSelected = destination == selectedDestination
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = if (isSelected) {
                                destination.iconSelected
                            } else {
                                destination.icon
                            },
                            contentDescription = null
                        )
                    },
                    label = { Text(i18n(destination.labelKey)) },
                    selected = isSelected,
                    onClick = { onTabSelected(destination) }
                )
            }
        }
    }
}
