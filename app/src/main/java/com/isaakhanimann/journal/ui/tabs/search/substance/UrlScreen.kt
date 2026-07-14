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

package com.isaakhanimann.journal.ui.tabs.search.substance

import android.content.Intent
import androidx.core.content.ContextCompat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import com.isaakhanimann.journal.ui.tabs.search.substance.UrlViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun UrlScreen(
    viewModel: UrlViewModel = hiltViewModel(),
    url: String,
    onHandled: () -> Unit,
) {
    val isOpenLinkInBrowser by viewModel.isOpenLinkInBrowserFlow.collectAsState()
    UrlScreen(isOpenLinkInBrowser = isOpenLinkInBrowser, url = url, onHandled = onHandled)
}

@Composable
fun UrlScreen(
    isOpenLinkInBrowser: Boolean,
    url: String,
    onHandled: () -> Unit,
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (isOpenLinkInBrowser) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } else {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
                .build()
            customTabsIntent.launchUrl(context, Uri.parse(url))
        }
        onHandled()
    }
}