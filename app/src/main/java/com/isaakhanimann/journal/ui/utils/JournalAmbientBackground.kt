package com.isaakhanimann.journal.ui.utils

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun JournalAmbientBackground(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val highContrastEnabled = runCatching {
        Settings.Secure.getInt(
            context.contentResolver,
            "high_text_contrast_enabled",
            0
        ) == 1
    }.getOrDefault(false)
    val isDarkTheme = isSystemInDarkTheme()
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier.fillMaxSize()) {
        if (highContrastEnabled || isDarkTheme) return@Canvas
        drawSoftGlow(
            center = Offset(size.width * 0.12f, size.height * 0.18f),
            radius = size.minDimension * 0.62f,
            color = primary.copy(alpha = 0.045f)
        )
        drawSoftGlow(
            center = Offset(size.width * 0.92f, size.height * 0.68f),
            radius = size.minDimension * 0.48f,
            color = tertiary.copy(alpha = 0.025f)
        )
    }
}

private fun DrawScope.drawSoftGlow(center: Offset, radius: Float, color: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = 0f)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}
