package com.isaakhanimann.journal.ui.main.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.navigation3.scene.Scene

const val NAVIGATION_FADE_MS = 90

val minimalNavTransitionSpec:
    AnimatedContentTransitionScope<Scene<Any>>.() -> ContentTransform = {
    fadeIn(tween(NAVIGATION_FADE_MS)) togetherWith fadeOut(tween(NAVIGATION_FADE_MS))
}

val predictivePopTransitionSpec:
    AnimatedContentTransitionScope<Scene<Any>>.(Int) -> ContentTransform = {
    fadeIn(tween(NAVIGATION_FADE_MS)) togetherWith fadeOut(tween(NAVIGATION_FADE_MS))
}
