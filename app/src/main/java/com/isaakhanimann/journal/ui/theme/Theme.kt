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

package com.isaakhanimann.journal.ui.theme

import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

private val JournalShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val JournalTypography = Typography().copy(
    headlineSmall = Typography().headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Typography().titleMedium.copy(fontWeight = FontWeight.Medium),
    labelLarge = Typography().labelLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp)
)

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim
)

private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim
)

val horizontalPadding = 10.dp
val verticalPaddingCards = 4.dp
val minimumTouchTargetHeight = 48.dp

private fun seedColorScheme(seed: Color, darkTheme: Boolean): ColorScheme {
    val hsl = FloatArray(3)
    androidx.core.graphics.ColorUtils.colorToHSL(seed.toArgb(), hsl)
    fun tone(lightness: Float, hueOffset: Float = 0f, saturationScale: Float = 1f): Color {
        val color = androidx.core.graphics.ColorUtils.HSLToColor(
            floatArrayOf((hsl[0] + hueOffset + 360f) % 360f, (hsl[1] * saturationScale).coerceIn(0f, 1f), lightness)
        )
        return Color(color)
    }
    return if (darkTheme) {
        androidx.compose.material3.darkColorScheme(
            primary = tone(0.80f), onPrimary = tone(0.20f), primaryContainer = tone(0.30f), onPrimaryContainer = tone(0.90f),
            secondary = tone(0.80f, 25f, 0.65f), onSecondary = tone(0.20f), secondaryContainer = tone(0.30f, 25f, 0.65f), onSecondaryContainer = tone(0.90f, 25f, 0.65f),
            tertiary = tone(0.80f, -25f, 0.7f), onTertiary = tone(0.20f), tertiaryContainer = tone(0.30f, -25f, 0.7f), onTertiaryContainer = tone(0.90f, -25f, 0.7f)
        )
    } else {
        androidx.compose.material3.lightColorScheme(
            primary = tone(0.40f), onPrimary = tone(1f, saturationScale = 0f), primaryContainer = tone(0.90f), onPrimaryContainer = tone(0.10f),
            secondary = tone(0.40f, 25f, 0.65f), onSecondary = tone(1f, saturationScale = 0f), secondaryContainer = tone(0.90f, 25f, 0.65f), onSecondaryContainer = tone(0.10f, 25f, 0.65f),
            tertiary = tone(0.40f, -25f, 0.7f), onTertiary = tone(1f, saturationScale = 0f), tertiaryContainer = tone(0.90f, -25f, 0.7f), onTertiaryContainer = tone(0.10f, -25f, 0.7f)
        )
    }
}

@Composable
private fun animateColorScheme(target: ColorScheme, enabled: Boolean): ColorScheme {
    val durationMillis = if (enabled) 300 else 0
    @Composable
    fun animate(color: androidx.compose.ui.graphics.Color) =
        animateColorAsState(color, tween(durationMillis), label = "theme color").value

    return target.copy(
        primary = animate(target.primary),
        onPrimary = animate(target.onPrimary),
        primaryContainer = animate(target.primaryContainer),
        onPrimaryContainer = animate(target.onPrimaryContainer),
        inversePrimary = animate(target.inversePrimary),
        secondary = animate(target.secondary),
        onSecondary = animate(target.onSecondary),
        secondaryContainer = animate(target.secondaryContainer),
        onSecondaryContainer = animate(target.onSecondaryContainer),
        tertiary = animate(target.tertiary),
        onTertiary = animate(target.onTertiary),
        tertiaryContainer = animate(target.tertiaryContainer),
        onTertiaryContainer = animate(target.onTertiaryContainer),
        background = animate(target.background),
        onBackground = animate(target.onBackground),
        surface = animate(target.surface),
        onSurface = animate(target.onSurface),
        surfaceVariant = animate(target.surfaceVariant),
        onSurfaceVariant = animate(target.onSurfaceVariant),
        surfaceTint = animate(target.surfaceTint),
        inverseSurface = animate(target.inverseSurface),
        inverseOnSurface = animate(target.inverseOnSurface),
        error = animate(target.error),
        onError = animate(target.onError),
        errorContainer = animate(target.errorContainer),
        onErrorContainer = animate(target.onErrorContainer),
        outline = animate(target.outline),
        outlineVariant = animate(target.outlineVariant),
        scrim = animate(target.scrim)
    )
}

@Composable
fun JournalTheme(themePalette: String? = null, content: @Composable () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    val targetColorScheme = if (themePalette != null) {
        val seed = runCatching { Color(android.graphics.Color.parseColor(themePalette)) }
            .getOrDefault(md_theme_light_primary)
        seedColorScheme(seed, isDarkTheme)
    } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (isDarkTheme) DarkColors else LightColors
    val animatorScale = runCatching {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
    }.getOrDefault(1f)
    val colorScheme = animateColorScheme(targetColorScheme, animatorScale > 0f)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = JournalTypography,
        shapes = JournalShapes,
        content = content
    )
}
