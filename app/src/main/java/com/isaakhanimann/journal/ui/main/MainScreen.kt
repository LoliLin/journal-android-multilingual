package com.isaakhanimann.journal.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.isaakhanimann.journal.localization.I18n
import com.isaakhanimann.journal.ui.main.navigation.Nav3TabManager
import com.isaakhanimann.journal.ui.main.navigation.minimalNavTransitionSpec
import com.isaakhanimann.journal.ui.main.navigation.nav3EntryProvider
import com.isaakhanimann.journal.ui.main.navigation.predictivePopTransitionSpec
import com.isaakhanimann.journal.ui.main.navigation.routes.AddIngestionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.CategoryRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ChooseRouteOfAddIngestionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.ExperienceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.QuickTimedNoteRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceCompanionRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.SubstanceRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.TimeCapsuleRoute
import com.isaakhanimann.journal.ui.main.navigation.routes.TopLevelDestinations
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
    LaunchedEffect(selectedLanguageKey) { I18n.setPreferredLanguageKey(selectedLanguageKey) }
    val isAccepted = viewModel.isAcceptedFlow.collectAsState().value
    val pendingIntent = rememberPendingNavigationIntent()
    if (isAccepted == null) {
        Box(modifier = Modifier.fillMaxSize())
    } else if (!isAccepted) {
        AcceptConditionsScreen(onTapAccept = viewModel::accept)
    } else if (viewModel.isAppLockEnabledFlow.collectAsState().value &&
        !viewModel.isUnlockedFlow.collectAsState().value
    ) {
        AppLockScreen(onUnlocked = viewModel::markUnlocked)
    } else {
        MainScreenContent(viewModel, pendingIntent)
    }
}

@Composable
private fun rememberPendingNavigationIntent(): MutableState<Intent?> {
    val pending = remember { mutableStateOf<Intent?>(null) }
    val activity = LocalContext.current as? ComponentActivity
    DisposableEffect(activity) {
        val listener = Consumer<Intent> { pending.value = it }
        activity?.addOnNewIntentListener(listener)
        onDispose { activity?.removeOnNewIntentListener(listener) }
    }
    LaunchedEffect(activity) { activity?.intent?.let { pending.value = it } }
    return pending
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenContent(viewModel: MainScreenViewModel, pendingIntent: MutableState<Intent?>) {
    val isBottomBarPinned = viewModel.isBottomBarPinnedFlow.collectAsState().value
    val manager = remember { Nav3TabManager() }
    val entryProvider = remember(manager) { nav3EntryProvider(manager) }
    val selectedDestination = manager.selectedTab
    val isOnMainTabRoot = manager.isAtRoot()
    val activity = LocalContext.current as? Activity

    val pendingIntentValue = pendingIntent.value
    LaunchedEffect(pendingIntentValue) {
        pendingIntentValue?.let {
            handleNavigationIntent(manager, it)
            pendingIntent.value = null
        }
    }

    val isKeyboardOpenNow = isKeyboardOpen().value
    val isBottomBarShown = isOnMainTabRoot && !isKeyboardOpenNow
    val bottomBarScrollBehavior = rememberBottomBarScrollBehavior {
        isOnMainTabRoot && !isKeyboardOpenNow
    }
    val nestedScrollConnection = if (isBottomBarPinned) null else remember(bottomBarScrollBehavior) {
        bottomBarNestedScrollConnection(bottomBarScrollBehavior)
    }
    LaunchedEffect(isOnMainTabRoot, isKeyboardOpenNow, isBottomBarPinned) {
        if (!isOnMainTabRoot || isKeyboardOpenNow || isBottomBarPinned) {
            bottomBarScrollBehavior.state.heightOffset = 0f
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val barHeightPx = remember { mutableIntStateOf(0) }
        val heightOffset = if (isBottomBarPinned) 0f else bottomBarScrollBehavior.state.heightOffset
        val visibleBarPx = if (isBottomBarShown) {
            (barHeightPx.intValue + heightOffset.toInt()).coerceAtLeast(0)
        } else {
            0
        }
        CompositionLocalProvider(
            LocalBottomBarNestedScrollConnection provides nestedScrollConnection,
            LocalBottomBarOverlayInsetPx provides visibleBarPx
        ) {
            NavDisplay(
                backStack = manager.currentBackStack,
                onBack = { if (!manager.pop()) activity?.finish() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                transitionSpec = minimalNavTransitionSpec,
                popTransitionSpec = minimalNavTransitionSpec,
                predictivePopTransitionSpec = predictivePopTransitionSpec,
                entryProvider = entryProvider,
                modifier = Modifier.fillMaxSize()
            )
        }
        BottomNavigationBar(
            visible = isBottomBarShown,
            selectedDestination = selectedDestination,
            scrollBehavior = bottomBarScrollBehavior,
            isPinned = isBottomBarPinned,
            onTabSelected = { destination ->
                if (destination == selectedDestination) manager.popToRoot(destination)
                else manager.switchToTab(destination)
            },
            onMeasuredHeightChanged = { barHeightPx.intValue = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

private fun handleNavigationIntent(manager: Nav3TabManager, intent: Intent) {
    if (handleDeepLink(manager, intent.data)) return
    val target = intent.getStringExtra(EXTRA_NAVIGATE_TO) ?: return
    val experienceId = intent.getIntExtra(EXTRA_EXPERIENCE_ID, -1)
    val substanceName = intent.getStringExtra(EXTRA_SUBSTANCE_NAME)
    when (target) {
        NAV_QUICK_NOTE -> if (experienceId > 0) manager.navigate(QuickTimedNoteRoute(experienceId))
        NAV_TIME_CAPSULE -> manager.navigate(TimeCapsuleRoute)
        NAV_ADD_INGESTION -> manager.navigate(AddIngestionRoute)
        NAV_STATS -> manager.switchToTab(TopLevelDestinations.Stats)
        NAV_SUBSTANCE -> if (!substanceName.isNullOrBlank()) manager.navigate(SubstanceRoute(substanceName))
        NAV_SUBSTANCE_COMPANION -> if (!substanceName.isNullOrBlank()) {
            manager.navigate(SubstanceCompanionRoute(substanceName, null))
        }
        NAV_CHOOSE_ROUTE -> if (!substanceName.isNullOrBlank()) {
            manager.navigate(ChooseRouteOfAddIngestionRoute(substanceName))
        }
    }
}

private fun handleDeepLink(manager: Nav3TabManager, uri: Uri?): Boolean {
    if (uri?.scheme != "journal" || uri.host != "open") return false
    val segments = uri.pathSegments
    val route = segments.firstOrNull() ?: return false
    fun segment(index: Int): String? = segments.getOrNull(index)
    when (route) {
        "journal" -> manager.switchToTab(TopLevelDestinations.Journal)
        "stats" -> manager.switchToTab(TopLevelDestinations.Stats)
        "substances" -> manager.switchToTab(TopLevelDestinations.Substances)
        "safer" -> manager.switchToTab(TopLevelDestinations.Safer)
        "settings" -> manager.switchToTab(TopLevelDestinations.Settings)
        "substance" -> segment(1)?.let { manager.navigate(SubstanceRoute(it)) }
        "category" -> segment(1)?.let { manager.navigate(CategoryRoute(it)) }
        "experience" -> segment(1)?.toIntOrNull()?.let { manager.navigate(ExperienceRoute(it)) }
        "quick-note" -> segment(1)?.toIntOrNull()?.let { manager.navigate(QuickTimedNoteRoute(it)) }
        "time-capsule" -> manager.navigate(TimeCapsuleRoute)
        "add-ingestion" -> manager.navigate(AddIngestionRoute)
        "choose-route" -> segment(1)?.let { manager.navigate(ChooseRouteOfAddIngestionRoute(it)) }
        "substance-companion" -> segment(1)?.let {
            manager.navigate(SubstanceCompanionRoute(it, uri.getQueryParameter("consumerName")))
        }
        else -> {
            Log.w(TAG, "Ignoring unknown deep link: $uri")
            return false
        }
    }
    return true
}
