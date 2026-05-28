/*
 * Copyright (c) 2024. 洛铃.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 */

package com.isaakhanimann.journal.ui.tabs.settings

import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.localization.i18n

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickerScreen(navigateBack: () -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager
    val classicAlias = ComponentName(context, "${context.packageName}.MainActivity_Classic")
    val springwindAlias = ComponentName(context, "${context.packageName}.MainActivity_SpringWind")

    val isSpringWindEnabled = pm.getComponentEnabledSetting(springwindAlias) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    var selectedIcon by remember { mutableStateOf(if (isSpringWindEnabled) "springwind" else "classic") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(i18n("settings_icon_title")) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconOption(
                label = i18n("settings_icon_springwind"),
                isSelected = selectedIcon == "springwind",
                color = Color(0xFFFF8C94),
                monogram = "S",
                onClick = {
                    switchIcon(pm, classicAlias, springwindAlias, enableSpringWind = true)
                    selectedIcon = "springwind"
                }
            )

            IconOption(
                label = i18n("settings_icon_classic"),
                isSelected = selectedIcon == "classic",
                color = Color(0xFF2196F3),
                monogram = "S",
                onClick = {
                    switchIcon(pm, classicAlias, springwindAlias, enableSpringWind = false)
                    selectedIcon = "classic"
                }
            )

            Text(
                text = "当前图标: ${if (selectedIcon == "springwind") "SpringWind" else "Classic"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun IconOption(
    label: String,
    isSelected: Boolean,
    color: Color,
    monogram: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected)
            CardDefaults.outlinedCardBorder().copy(width = 2.dp)
        else
            null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
            ) {
                Text(
                    text = monogram,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

private fun switchIcon(
    pm: PackageManager,
    classicAlias: ComponentName,
    springwindAlias: ComponentName,
    enableSpringWind: Boolean
) {
    val enable = if (enableSpringWind) springwindAlias else classicAlias
    val disable = if (enableSpringWind) classicAlias else springwindAlias
    pm.setComponentEnabledSetting(
        enable,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP
    )
    pm.setComponentEnabledSetting(
        disable,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP
    )
}
