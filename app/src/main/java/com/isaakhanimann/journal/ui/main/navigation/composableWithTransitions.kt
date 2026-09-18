/*
 * Copyright (c) 2022. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * PsychonautWiki Journal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PsychonautWiki Journal.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.ui.main.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/** Duration of the app-wide navigation cross-fade. */
@PublishedApi
internal const val NAVIGATION_FADE_MS = 150

@PublishedApi
internal val navigationEnterTransition: EnterTransition = fadeIn(tween(NAVIGATION_FADE_MS))

@PublishedApi
internal val navigationExitTransition: ExitTransition = fadeOut(tween(NAVIGATION_FADE_MS))

/**
 * Registers [T] as a destination with the app-wide transition.
 *
 * All navigation uses one minimal cross-fade with no directional movement: pushing into a tab,
 * going back, switching bottom-bar tabs, and the system predictive-back preview. Keeping all four
 * transition slots on the same animation is what makes the back-gesture preview look identical
 * whether the user is leaving a detail screen inside the current tab or crossing to another tab.
 *
 * The transitions must stay non-null: predictive back only animates its preview when the
 * destination defines enter/exit transitions.
 */
inline fun <reified T : Any> NavGraphBuilder.composableWithTransitions(
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable<T>(
        deepLinks = deepLinks,
        enterTransition = { navigationEnterTransition },
        exitTransition = { navigationExitTransition },
        popEnterTransition = { navigationEnterTransition },
        popExitTransition = { navigationExitTransition },
        content = content
    )
}
