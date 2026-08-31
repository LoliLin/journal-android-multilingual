package com.isaakhanimann.journal.ui.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

/**
 * Opens [url] according to the "Open Link in Browser" preference:
 * - on: a real browser activity (Chrome etc.), not a Custom Tab
 * - off: Custom Tabs (in-app browser window)
 */
internal fun openLink(
    context: Context,
    url: String,
    openInBrowser: Boolean,
    toolbarColor: Int? = null,
    failedMessage: String
) {
    if (url.isBlank()) return
    val uri = Uri.parse(url)
    val opened = if (openInBrowser) {
        openInExternalBrowser(context, uri)
    } else {
        openInCustomTab(context, uri, toolbarColor) || openInExternalBrowser(context, uri)
    }
    if (!opened) {
        Toast.makeText(context, failedMessage, Toast.LENGTH_SHORT).show()
    }
}

private fun openInCustomTab(context: Context, uri: Uri, toolbarColor: Int?): Boolean = try {
    val builder = CustomTabsIntent.Builder()
    if (toolbarColor != null) {
        builder.setDefaultColorSchemeParams(
            CustomTabColorSchemeParams.Builder()
                .setToolbarColor(toolbarColor)
                .build()
        )
    }
    val customTabsIntent = builder.build()
    customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    customTabsIntent.launchUrl(context, uri)
    true
} catch (_: Exception) {
    false
}

private fun openInExternalBrowser(context: Context, uri: Uri): Boolean {
    val viewIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
    }
    val browserActivity = context.packageManager
        .queryIntentActivities(viewIntent, PackageManager.MATCH_DEFAULT_ONLY)
        .asSequence()
        .map { it.activityInfo }
        .firstOrNull { info ->
            info.exported && !info.name.contains("customtab", ignoreCase = true)
        }
    val intent = if (browserActivity != null) {
        Intent(viewIntent).apply {
            setClassName(browserActivity.packageName, browserActivity.name)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    } else {
        Intent(Intent.ACTION_VIEW, uri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    return try {
        context.startActivity(intent)
        true
    } catch (_: Exception) {
        false
    }
}

@HiltViewModel
class OpenLinkViewModel @Inject constructor(
    userPreferences: UserPreferences
) : ViewModel() {
    val isOpenLinkInBrowserFlow = userPreferences.isOpenLinkInBrowserFlow.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )
}

@Composable
fun rememberOpenLink(): (String) -> Unit {
    if (LocalInspectionMode.current) {
        return {}
    }
    val viewModel: OpenLinkViewModel = hiltViewModel()
    val openInBrowser by viewModel.isOpenLinkInBrowserFlow.collectAsState()
    val context = LocalContext.current
    val toolbarColor = MaterialTheme.colorScheme.surface.toArgb()
    val failedMessage = i18n("open_link_failed")
    return remember(openInBrowser, context, toolbarColor, failedMessage) {
        { url ->
            openLink(context, url, openInBrowser, toolbarColor, failedMessage)
        }
    }
}
