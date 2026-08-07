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

package com.isaakhanimann.journal.ui.tabs.journal.addingestion.route

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.localization.i18nOrDefault
import com.isaakhanimann.journal.ui.utils.administrationRouteDescriptionKey
import com.isaakhanimann.journal.ui.utils.administrationRouteKey

@Composable
fun ChooseRouteScreen(
    navigateToChooseDose: (administrationRoute: AdministrationRoute) -> Unit,
    navigateToURL: (url: String) -> Unit,
    navigateToRouteExplanationScreen: () -> Unit,
    viewModel: ChooseRouteViewModel = hiltViewModel()
) {
    ChooseRouteScreen(
        showOtherRoutes = viewModel.showOtherRoutes,
        onChangeOfShowOtherRoutes = {
            viewModel.showOtherRoutes = it
        },
        pwRoutes = viewModel.pwRoutes,
        otherRoutesChunked = viewModel.otherRoutesChunked,
        onRouteTapped = { route ->
            if (route.isInjectionMethod) {
                viewModel.currentRoute = route
                viewModel.isShowingInjectionDialog = true
            } else {
                navigateToChooseDose(route)
            }
        },
        navigateToRouteExplanationScreen = navigateToRouteExplanationScreen,
        navigateToURL = navigateToURL,
        isShowingInjectionDialog = viewModel.isShowingInjectionDialog,
        navigateWithCurrentRoute = {
            navigateToChooseDose(viewModel.currentRoute)
        },
        dismissInjectionDialog = {
            viewModel.isShowingInjectionDialog = false
        },
        substanceName = viewModel.substanceName,
        getSubstanceDisplayName = viewModel.substanceRepo::getDisplayName
    )
}

@Preview
@Composable
fun ChooseRouteScreenPreview() {
    val pwRoutes = listOf(AdministrationRoute.INSUFFLATED, AdministrationRoute.ORAL)
    val otherRoutes = AdministrationRoute.entries.filter { route ->
        !pwRoutes.contains(route)
    }
    val otherRoutesChunked = otherRoutes.chunked(2)
    ChooseRouteScreen(
        showOtherRoutes = false,
        onChangeOfShowOtherRoutes = {},
        pwRoutes = pwRoutes,
        otherRoutesChunked = otherRoutesChunked,
        onRouteTapped = {},
        navigateToRouteExplanationScreen = {},
        navigateToURL = {},
        isShowingInjectionDialog = false,
        navigateWithCurrentRoute = {},
        dismissInjectionDialog = {},
        substanceName = "LSD",
        getSubstanceDisplayName = { it }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseRouteScreen(
    showOtherRoutes: Boolean,
    onChangeOfShowOtherRoutes: (Boolean) -> Unit,
    pwRoutes: List<AdministrationRoute>,
    otherRoutesChunked: List<List<AdministrationRoute>>,
    onRouteTapped: (route: AdministrationRoute) -> Unit,
    navigateToRouteExplanationScreen: () -> Unit,
    navigateToURL: (url: String) -> Unit,
    isShowingInjectionDialog: Boolean,
    navigateWithCurrentRoute: () -> Unit,
    dismissInjectionDialog: () -> Unit,
    substanceName: String,
    getSubstanceDisplayName: (substance: String) -> String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        i18n(
                            "substances_route",
                            mapOf(
                                "substance" to getSubstanceDisplayName(substanceName)
                            )
                        )
                    )
                },
                navigationIcon = {
                    if (showOtherRoutes && pwRoutes.isNotEmpty()) {
                        IconButton(onClick = { onChangeOfShowOtherRoutes(false) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = i18n("back")
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = navigateToRouteExplanationScreen) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = i18n("administration_routes_info")
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            LinearProgressIndicator(
                progress = { 0.5f },
                modifier = Modifier.fillMaxWidth()
            )
            AnimatedVisibility(visible = isShowingInjectionDialog) {
                InjectionDialog(
                    navigateToNext = navigateWithCurrentRoute,
                    navigateToURL = navigateToURL,
                    dismiss = dismissInjectionDialog
                )
            }
            AdministrationRoutePicker(
                showOtherRoutes = showOtherRoutes,
                onChangeOfShowOtherRoutes = onChangeOfShowOtherRoutes,
                pwRoutes = pwRoutes,
                otherRoutesChunked = otherRoutesChunked,
                onRouteTapped = onRouteTapped
            )
        }
    }
}

@Preview
@Composable
fun InjectionDialogPreview() {
    InjectionDialog(
        navigateToNext = {},
        navigateToURL = {},
        dismiss = {}
    )
}

@Composable
fun InjectionDialog(
    navigateToNext: () -> Unit,
    navigateToURL: (url: String) -> Unit,
    dismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = i18n("interaction_warning")
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(text = i18n("safer_injection"), style = MaterialTheme.typography.headlineSmall)
            }
        },
        text = {
            Column {
                Text(i18n("safer_injection_guide.t1"))
                Text(i18n("safer_injection_guide.t2"))
                SaferInjectionLink(navigateToURL)
                Text(i18n("safer_injection_guide.t3"))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    dismiss()
                    navigateToNext()
                }
            ) {
                Text(i18n("continue"))
            }
        },
        dismissButton = {
            TextButton(
                onClick = dismiss
            ) {
                Text(i18n("common_cancel"))
            }
        }
    )
}

@Composable
fun RouteBox(route: AdministrationRoute, titleStyle: TextStyle) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenHeightPx = with(LocalDensity.current) { screenHeight.toPx() }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = i18nOrDefault(administrationRouteKey(route), route.displayText),
                textAlign = TextAlign.Center,
                style = titleStyle
            )
            if (screenHeightPx > 500) {
                Text(
                    text = i18nOrDefault(
                        administrationRouteDescriptionKey(route),
                        route.description
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun SaferInjectionLink(navigateToURL: (url: String) -> Unit) {
    TextButton(onClick = {
        navigateToURL(AdministrationRoute.SAFER_INJECTION_ARTICLE_URL)
    }) {
        Icon(
            Icons.Outlined.Newspaper,
            contentDescription = i18n("interaction_open_link"),
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(i18n("safer_injection_guide"))
    }
}
