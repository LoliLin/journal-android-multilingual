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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.R
import kotlinx.coroutines.launch

data class IconOption(
    val key: String,
    val label: String,
    val backgroundColor: Color,
    val iconRes: Int,
    val aliasName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickerScreen(navigateBack: () -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val iconOptions = listOf(
            IconOption(
                key = "classic",
                label = i18n("settings_icon_classic"),
                backgroundColor = Color(0xFF2196F3),
                iconRes = R.mipmap.ic_launcher,
                aliasName = ".MainActivity_Classic"
            ),
            IconOption(
                key = "springwind",
                label = i18n("settings_icon_springwind"),
                backgroundColor = Color(0xFFFF8C94),
                iconRes = R.drawable.ic_springwind_foreground,
                aliasName = ".MainActivity_SpringWind"
            )
        )

    val aliases = remember {
        iconOptions.associate { it.key to ComponentName(context, "${context.packageName}${it.aliasName}") }
    }

    var selectedKey by remember {
        mutableStateOf(getCurrentIconKey(pm, aliases))
    }
    var isSwitching by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(i18n("settings_icon_title")) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            iconOptions.forEach { option ->
                IconOptionCard(
                    option = option,
                    isSelected = selectedKey == option.key,
                    onClick = {
                        if (!isSwitching && selectedKey != option.key) {
                            isSwitching = true
                            val success = switchIcon(pm, aliases, enableKey = option.key)
                            if (success) {
                                selectedKey = option.key
                                scope.launch {
                                    snackbarHostState.showSnackbar("图标已切换")
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("图标切换失败")
                                }
                            }
                            isSwitching = false
                        }
                    }
                )
            }

            Text(
                text = "当前: $selectedKey",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getCurrentIconKey(
    pm: PackageManager,
    aliases: Map<String, ComponentName>
): String {
    aliases.forEach { (key, component) ->
        try {
            val state = pm.getComponentEnabledSetting(component)
            if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                return key
            }
        } catch (_: Exception) { }
    }
    return aliases.keys.first()
}

private fun switchIcon(
    pm: PackageManager,
    aliases: Map<String, ComponentName>,
    enableKey: String
): Boolean {
    return try {
        aliases.values.forEach { component ->
            pm.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                0
            )
        }
        val target = aliases[enableKey] ?: return false
        pm.setComponentEnabledSetting(
            target,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            0
        )
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@Composable
private fun IconOptionCard(
    option: IconOption,
    isSelected: Boolean,
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
            BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
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
                    .background(option.backgroundColor)
            ) {
                Image(
                    painter = painterResource(id = option.iconRes),
                    contentDescription = option.label,
                    modifier = Modifier.size(72.dp)
                )
            }
            Text(
                text = option.label,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
