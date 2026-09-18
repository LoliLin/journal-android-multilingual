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

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.isaakhanimann.journal.localization.I18n
import com.isaakhanimann.journal.ui.main.navigation.graphs.journalGraph
import com.isaakhanimann.journal.ui.main.navigation.graphs.saferGraph
import com.isaakhanimann.journal.ui.main.navigation.graphs.searchGraph
import com.isaakhanimann.journal.ui.main.navigation.graphs.settingsGraph
import com.isaakhanimann.journal.ui.main.navigation.graphs.statsGraph
import com.isaakhanimann.journal.ui.main.navigation.routes.JournalTab
import com.isaakhanimann.journal.ui.main.navigation.routes.TopLevelDestinations
import com.isaakhanimann.journal.ui.main.navigation.routes.isTopLevelDestinationRoot
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToAddIngestion
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToChooseRouteOfAddIngestion
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToQuickTimedNote
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToSubstanceCompanionScreen
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToSubstanceScreen
import com.isaakhanimann.journal.ui.main.navigation.routes.navigateToTimeCapsule
import com.isaakhanimann.journal.ui.main.navigation.routes.popToTopLevelDestinationRoot
import com.isaakhanimann.journal.ui.main.navigation.routes.switchToTopLevelDestination
import com.isaakhanimann.journal.ui.main.navigation.routes.topLevelDestinationOrNull
import com.isaakhanimann.journal.ui.notifications.EXTRA_EXPERIENCE_ID
import com.isaakhanimann.journal.ui.notifications.EXTRA_NAVIGATE_TO
import com.isaakhanimann.journal.ui.notifications.EXTRA_SUBSTANCE_NAME
import com.isaakhanimann.journal.ui.notifications.NAV_ADD_INGESTION
import com.isaakhanimann.journal.ui.notifications.NAV_CHOOSE_ROUTE
import com.isaakhanimann.journal.ui.notifications.NAV_QUICK_NOTE
import com.isaakhanimann.journal.ui.notifications.NAV_STATS
import com.isaakhanimann.journal.ui.notifications.NAV_SUBSTANCE
import com.isaakhanimann.journal.ui.notifications.NAV_SUBSTANCE_COMPANION
import com.isaakhanimann.journal.ui.notifications.NAV_TIME_CAPSULE
import com.isaakhanimann.journal.ui.utils.keyboard.isKeyboardOpen

private const val TAG = "MainScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainScreenViewModel = hiltViewModel()) {
    val selectedLanguageKey by viewModel.selectedLanguageFlow.collectAsState()
    LaunchedEffect(selectedLanguageKey) {
        I18n.setPreferredLanguageKey(selectedLanguageKey)
    }
    val isAccepted = viewModel.isAcceptedFlow.collectAsState().value

    // Notification taps and journal:// deep links steer the app to a screen. Tracked above the gate
    // so the intent survives the accept-conditions and app-lock screens and is consumed by the
    // content branch once the navigation graph exists.
    val pendingIntent = rememberPendingNavigationIntent()

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
        MainScreenContent(viewModel = viewModel, pendingIntent = pendingIntent)
    }
}

/**
 * The activity intent that should steer navigation, delivered both for the launching intent and
 * for later taps while the activity already exists.
 *
 * `MainActivity` is `singleTop`, so a notification or widget tap reaches an existing instance
 * through `onNewIntent` instead of recreating it and dropping the whole navigation stack.
 */
@Composable
private fun rememberPendingNavigationIntent(): MutableState<Intent?> {
    val pending = remember { mutableStateOf<Intent?>(null) }
    val activity = LocalContext.current as? ComponentActivity
    DisposableEffect(activity) {
        val listener = Consumer<Intent> { intent -> pending.value = intent }
        activity?.addOnNewIntentListener(listener)
        onDispose { activity?.removeOnNewIntentListener(listener) }
    }
    LaunchedEffect(activity) {
        activity?.intent?.let { pending.value = it }
    }
    return pending
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenContent(
    viewModel: MainScreenViewModel,
    pendingIntent: MutableState<Intent?>
) {
    val isBottomBarPinned = viewModel.isBottomBarPinnedFlow.collectAsState().value
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    // Both lookups compare the destination against the five tab routes, so they are memoised per
    // destination instead of re-resolving the route classes on every recomposition.
    val selectedDestination = remember(currentDestination) {
        currentDestination?.topLevelDestinationOrNull()
    }
    val isOnMainTabRoot = remember(currentDestination) {
        currentDestination?.isTopLevelDestinationRoot() == true
    }

    val pendingIntentValue = pendingIntent.value
    LaunchedEffect(pendingIntentValue) {
        pendingIntentValue?.let { intent ->
            handleNavigationIntent(navController, intent)
            pendingIntent.value = null
        }
    }

    val isKeyboardOpenNow = isKeyboardOpen().value
    val isBottomBarShown = isOnMainTabRoot && !isKeyboardOpenNow
    // When the user pins the bar (issue #144) no scroll connection is provided at all: lists scroll
    // normally and the bar never moves.
    val bottomBarScrollBehavior = rememberBottomBarScrollBehavior(
        canScroll = { isOnMainTabRoot && !isKeyboardOpenNow }
    )
    val nestedScrollConnection = if (isBottomBarPinned) {
        null
    } else {
        remember(bottomBarScrollBehavior) {
            bottomBarNestedScrollConnection(bottomBarScrollBehavior)
        }
    }
    LaunchedEffect(isOnMainTabRoot, isKeyboardOpenNow, isBottomBarPinned) {
        if (!isOnMainTabRoot || isKeyboardOpenNow || isBottomBarPinned) {
            bottomBarScrollBehavior.state.heightOffset = 0f
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val barHeightPx = remember { mutableIntStateOf(0) }
        val heightOffset = if (isBottomBarPinned) {
            0f
        } else {
            bottomBarScrollBehavior.state.heightOffset
        }
        val visibleBarPx = if (isBottomBarShown) {
            (barHeightPx.intValue + heightOffset.toInt()).coerceAtLeast(0)
        } else {
            0
        }
        CompositionLocalProvider(
            LocalBottomBarNestedScrollConnection provides nestedScrollConnection,
            LocalBottomBarOverlayInsetPx provides visibleBarPx
        ) {
            NavHost(
                navController,
                startDestination = JournalTab,
                modifier = Modifier.fillMaxSize()
            ) {
                journalGraph(navController)
                statsGraph(navController)
                searchGraph(navController)
                saferGraph(navController)
                settingsGraph(navController)
            }
        }
        BottomNavigationBar(
            visible = isBottomBarShown,
            selectedDestination = selectedDestination,
            scrollBehavior = bottomBarScrollBehavior,
            isPinned = isBottomBarPinned,
            onTabSelected = { destination ->
                if (destination == selectedDestination) {
                    // Re-tapping the selected tab returns to its root instead of popping a single
                    // screen, which is what the platform convention expects.
                    if (!isOnMainTabRoot) {
                        navController.popToTopLevelDestinationRoot(destination)
                    }
                } else {
                    navController.switchToTopLevelDestination(destination)
                }
            },
            onMeasuredHeightChanged = { barHeightPx.intValue = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Routes an incoming intent, preferring the registered `journal://` deep links and falling back to
 * the `EXTRA_NAVIGATE_TO` contract used by notifications and the stats widget.
 */
private fun handleNavigationIntent(navController: NavController, intent: Intent) {
    val wasHandledByDeepLink = try {
        navController.handleDeepLink(intent)
    } catch (e: IllegalStateException) {
        // Thrown when a deep link cannot be reached from the current destination; the app should
        // stay where it is rather than crash.
        Log.w(TAG, "Deep link could not be handled from the current destination", e)
        false
    }
    if (wasHandledByDeepLink) return
    handleLegacyNavigationIntent(navController, intent)
}

private fun handleLegacyNavigationIntent(navController: NavController, intent: Intent) {
    val target = intent.getStringExtra(EXTRA_NAVIGATE_TO) ?: return
    val experienceId = intent.getIntExtra(EXTRA_EXPERIENCE_ID, -1)
    val substanceName = intent.getStringExtra(EXTRA_SUBSTANCE_NAME)
    when (target) {
        NAV_QUICK_NOTE -> if (experienceId > 0) {
            navController.navigateToQuickTimedNote(experienceId)
        }
        NAV_TIME_CAPSULE -> navController.navigateToTimeCapsule()
        NAV_ADD_INGESTION -> navController.navigateToAddIngestion()
        NAV_STATS -> navController.switchToTopLevelDestination(TopLevelDestinations.Stats)
        NAV_SUBSTANCE -> if (!substanceName.isNullOrBlank()) {
            navController.navigateToSubstanceScreen(substanceName)
        }
        NAV_SUBSTANCE_COMPANION -> if (!substanceName.isNullOrBlank()) {
            navController.navigateToSubstanceCompanionScreen(substanceName, null)
        }
        NAV_CHOOSE_ROUTE -> if (!substanceName.isNullOrBlank()) {
            navController.navigateToChooseRouteOfAddIngestion(substanceName)
        }
    }
}
