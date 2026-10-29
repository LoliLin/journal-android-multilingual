package com.isaakhanimann.journal.data.room.experiences.entities

import androidx.compose.ui.graphics.Color

/** The stored custom ARGB color takes precedence over the legacy adaptive palette. */
fun SubstanceCompanion.getComposeColor(isDarkTheme: Boolean): Color =
    color.getComposeColor(customColorArgb, isDarkTheme)

/** Resolves a custom companion color while retaining the adaptive fallback for old records. */
fun AdaptiveColor.getComposeColor(customColorArgb: Int?, isDarkTheme: Boolean): Color =
    customColorArgb?.let(::Color) ?: getComposeColor(isDarkTheme)
