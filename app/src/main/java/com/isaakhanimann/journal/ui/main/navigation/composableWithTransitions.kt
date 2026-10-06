/*
 * Copyright (c) 2022-2023. Isaak Hanimann.
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

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

private const val NAVIGATION_TRANSITION_MS = 280
private const val REDUCED_TRANSITION_MS = 140
private const val NAVIGATION_START_SCALE = 0.98f

private fun AnimatedContentTransitionScope<Scene<NavKey>>.directionalTransition(
    direction: Int,
): ContentTransform {
    val horizontalDirection = if (direction < 0) -1 else 1
    val spatialSpring = spring<Float>(
        dampingRatio = 0.9f,
        stiffness = Spring.StiffnessMediumLow
    )
    val offsetSpring = spring<IntOffset>(
        dampingRatio = 0.9f,
        stiffness = Spring.StiffnessMediumLow
    )
    return (slideInHorizontally(
        offsetSpring,
        initialOffsetX = { width -> width * 38 / 100 * horizontalDirection }
    ) + fadeIn(tween(NAVIGATION_TRANSITION_MS)) +
        scaleIn(initialScale = NAVIGATION_START_SCALE, animationSpec = spatialSpring)) togetherWith
        (slideOutHorizontally(
            offsetSpring,
            targetOffsetX = { width -> -width * 12 / 100 * horizontalDirection }
        ) + fadeOut(tween(NAVIGATION_TRANSITION_MS)) +
            scaleOut(targetScale = NAVIGATION_START_SCALE, animationSpec = spatialSpring))
}

fun navTransitionSpecForDirection(
    direction: Int,
    agoraStyle: Boolean = true
): AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
    if (agoraStyle) {
        directionalTransition(direction)
    } else {
        fadeIn(tween(REDUCED_TRANSITION_MS)) togetherWith fadeOut(tween(REDUCED_TRANSITION_MS))
    }
}

/** Directional navigation for pushing a destination onto the current tab stack. */
val minimalNavTransitionSpec = navTransitionSpecForDirection(1)

fun popNavTransitionSpecForDirection(
    direction: Int,
    agoraStyle: Boolean = true,
): AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
    if (agoraStyle) directionalTransition(direction)
    else fadeIn(tween(REDUCED_TRANSITION_MS)) togetherWith fadeOut(tween(REDUCED_TRANSITION_MS))
}

fun predictivePopTransitionSpecForDirection(
    direction: Int,
    agoraStyle: Boolean = true,
): AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform = {
    if (agoraStyle) directionalTransition(direction)
    else fadeIn(tween(REDUCED_TRANSITION_MS)) togetherWith fadeOut(tween(REDUCED_TRANSITION_MS))
}
