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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

private const val NAVIGATION_FADE_MS = 90

/**
 * The transition every navigation uses.
 *
 * Deliberately uniform — one cross-fade with no directional movement — so that pushing, popping,
 * switching tabs and the predictive-back gesture preview all look the same.
 */
val minimalNavTransitionSpec:
    AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
    fadeIn(tween(NAVIGATION_FADE_MS)) togetherWith fadeOut(tween(NAVIGATION_FADE_MS))
}

val predictivePopTransitionSpec:
    AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform = {
    fadeIn(tween(NAVIGATION_FADE_MS)) togetherWith fadeOut(tween(NAVIGATION_FADE_MS))
}
