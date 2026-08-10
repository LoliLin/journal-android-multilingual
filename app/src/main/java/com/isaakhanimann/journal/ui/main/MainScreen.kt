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

package com.isaakhanimann.journal.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.isaakhanimann.journal.localization.I18n
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.main.navigation.graphs.journalGraph
import com.isaakhanimann.journal.ui.main.navigation.routers.navigateToQuickTimedNote
import com.isaakhanimann.journal.ui.main.navigation.routers.navigateToTimeCapsule
import com.isaakhanimann.journal.ui.notifications.EXTRA_EXPERIENCE_ID
import com.isaakhanimann.journal.ui.notifications.EXTRA_NAVIGATE_TO
import com.isaakhanimann.journal.ui.notifications.NAV_QUICK_NOTE
import com.isaakhanimann.journal.ui.notifications.NAV_TIME_CAPSULE
import com.isaakhanimann.journal.ui.main.navigation.graphs.saferGraph
import com.isaakhanimann.journal.ui.main.navigation.graphs.searchGraph
import com.isaakhanimann.journal.ui.main.navigation.graphs.settingsGraph
import com.isaakhanimann.journal.ui.main.navigation.graphs.statsGraph
import com.isaakhanimann.journal.ui.main.navigation.routers.TabRouter
import com.isaakhanimann.journal.ui.utils.keyboard.isKeyboardOpen

private fun parseNavIntent(intent: android.content.Intent?): Pair<String, Int>? {
    val target = intent?.getStringExtra(EXTRA_NAVIGATE_TO) ?: return null
    return target to intent.getIntExtra(EXTRA_EXPERIENCE_ID, -1)
}

@Composable
fun MainScreen(viewModel: MainScreenViewModel = hiltViewModel()) {
    val selectedLanguageKey by viewModel.selectedLanguageFlow.collectAsState()
    LaunchedEffect(selectedLanguageKey) {
        I18n.setPreferredLanguageKey(selectedLanguageKey)
    }
    val isAccepted = viewModel.isAcceptedFlow.collectAsState().value

    // Notification taps can steer the app to a target screen (quick note / time capsule).
    // Tracked above the gate so the intent survives the accept/lock screens and is
    // consumed by the content branch once it composes.
    var pendingNav by remember { mutableStateOf<Pair<String, Int>?>(null) }
    val activity = LocalContext.current as? androidx.activity.ComponentActivity
    DisposableEffect(activity) {
        val listener = { intent: android.content.Intent ->
            parseNavIntent(intent)?.let { pendingNav = it }
            Unit
        }
        activity?.addOnNewIntentListener(listener)
        onDispose { activity?.removeOnNewIntentListener(listener) }
    }
    LaunchedEffect(Unit) {
        parseNavIntent(activity?.intent)?.let { pendingNav = it }
    }

    if (isAccepted == null) {
        // DataStore value not read yet: show nothing instead of flashing content.
        Box(modifier = Modifier.fillMaxSize())
    } else if (!isAccepted) {
        AcceptConditionsScreen(onTapAccept = viewModel::accept)
    } else if (viewModel.isAppLockEnabledFlow.collectAsState().value &&
        !viewModel.isUnlockedFlow.collectAsState().value
    ) {
        AppLockScreen(onUnlocked = viewModel::markUnlocked)
    } else {
        val navController = rememberNavController()
        LaunchedEffect(pendingNav) {
            pendingNav?.let { (target, experienceId) ->
                when (target) {
                    NAV_QUICK_NOTE -> if (experienceId > 0) {
                        navController.navigateToQuickTimedNote(experienceId)
                    }
                    NAV_TIME_CAPSULE -> navController.navigateToTimeCapsule()
                }
                pendingNav = null
            }
        }
        // Floating capsule navigation: the capsule overlays the content and
        // stays clear of the system gesture area via navigationBarsPadding.
        Box(modifier = Modifier.fillMaxSize()) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val tabs = listOf(
                TabRouter.Statistics,
                TabRouter.Journal,
                TabRouter.Substances,
                TabRouter.SaferUse,
                TabRouter.Settings
            )
            NavHost(
                navController,
                startDestination = TabRouter.Journal.route,
                modifier = Modifier.fillMaxSize()
            ) {
                journalGraph(navController)
                statsGraph(navController)
                searchGraph(navController)
                saferGraph(navController)
                settingsGraph(navController)
            }
            val isShowingBottomBar = isKeyboardOpen().value.not()
            if (isShowingBottomBar) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 10.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEach { tab ->
                            val isSelected =
                                currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            FloatingNavItem(
                                icon = if (isSelected) tab.iconSelected else tab.icon,
                                label = i18n(tab.labelKey),
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        val isAlreadyOnTopOfTab = tabs.any {
                                            it.childRoute == currentDestination.route
                                        }
                                        if (!isAlreadyOnTopOfTab) {
                                            navController.popBackStack()
                                        }
                                    } else {
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            Color.Transparent
        },
        modifier = Modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
