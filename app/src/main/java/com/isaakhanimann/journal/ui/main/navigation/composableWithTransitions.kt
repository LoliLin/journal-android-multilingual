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

private const val NAVIGATION_TRANSITION_MS = 300
private const val NAVIGATION_START_SCALE = 0.94f

private fun AnimatedContentTransitionScope<Scene<NavKey>>.directionalTransition(
    enteringFrom: (Int) -> Int,
    exitingTo: (Int) -> Int,
): ContentTransform {
    val spatialSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
    val offsetSpring = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
    return (slideInHorizontally(offsetSpring, initialOffsetX = enteringFrom) +
        fadeIn(tween(NAVIGATION_TRANSITION_MS)) +
        scaleIn(initialScale = NAVIGATION_START_SCALE, animationSpec = spatialSpring)) togetherWith
        (slideOutHorizontally(offsetSpring, targetOffsetX = exitingTo) +
            fadeOut(tween(NAVIGATION_TRANSITION_MS)) +
            scaleOut(targetScale = NAVIGATION_START_SCALE, animationSpec = spatialSpring))
}

/** Directional navigation for pushing a destination onto the current tab stack. */
val minimalNavTransitionSpec:
    AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
    directionalTransition(
        enteringFrom = { width -> width * 3 / 4 },
        exitingTo = { width -> -width / 4 },
    )
}

/** Reverse directional navigation when popping a destination from the current tab stack. */
val popNavTransitionSpec:
    AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
    directionalTransition(
        enteringFrom = { width -> -width * 3 / 4 },
        exitingTo = { width -> width / 4 },
    )
}

val predictivePopTransitionSpec:
    AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform = {
        directionalTransition(
            enteringFrom = { width -> -width * 3 / 4 },
            exitingTo = { width -> width / 4 },
        )
    }
