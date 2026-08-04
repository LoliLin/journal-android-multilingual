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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
        val listener = androidx.activity.OnNewIntentListener { intent ->
            parseNavIntent(intent)?.let { pendingNav = it }
            true
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
        Scaffold(
            bottomBar = {
                val isShowingBottomBar = isKeyboardOpen().value.not()
                if (isShowingBottomBar) {
                    NavigationBar {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        val tabs = listOf(
                            TabRouter.Statistics,
                            TabRouter.Journal,
                            TabRouter.Substances,
                            TabRouter.SaferUse,
                            TabRouter.Settings
                        )
                        tabs.forEach { tab ->
                            val isSelected =
                                currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                icon = {
                                    if (isSelected) {
                                        Icon(tab.iconSelected, contentDescription = null)
                                    } else {
                                        Icon(tab.icon, contentDescription = null)
                                    }
                                },
                                label = { Text(i18n(tab.labelKey)) },
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        val isAlreadyOnTopOfTab = tabs.any {
                                            it.childRoute ==
                                                currentDestination.route
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
        ) { innerPadding ->
            NavHost(
                navController,
                startDestination = TabRouter.Journal.route,
                modifier = Modifier
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                journalGraph(navController)
                statsGraph(navController)
                searchGraph(navController)
                saferGraph(navController)
                settingsGraph(navController)
            }
        }
    }
}
