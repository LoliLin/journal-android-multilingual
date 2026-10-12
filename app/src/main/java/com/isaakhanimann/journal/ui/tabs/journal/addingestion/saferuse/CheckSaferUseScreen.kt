/*
 * Copyright (c) 2023. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 */

package com.isaakhanimann.journal.ui.tabs.journal.addingestion.saferuse

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.NextFAB
import com.isaakhanimann.journal.ui.tabs.search.substance.BulletPoints
import com.isaakhanimann.journal.ui.theme.horizontalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckSaferUseScreen(
    navigateToNext: () -> Unit,
    viewModel: SaferUseViewModel
) {
    CheckSaferUseScreenContent(
        substanceName = viewModel.substance.displayName,
        saferUse = viewModel.substance.saferUse,
        navigateToNext = navigateToNext
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckSaferUseScreenContent(
    substanceName: String,
    saferUse: List<String>,
    navigateToNext: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("$substanceName${i18n("safer_use_title")}") })
        },
        floatingActionButton = { NextFAB(navigateToNext) }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            BulletPoints(
                points = saferUse,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckSaferUseScreenPreview() {
    CheckSaferUseScreenContent(
        substanceName = "MDMA",
        saferUse = listOf(
            "Start with a low dose and avoid redosing.",
            "Stay cool, take breaks, and sip water regularly."
        ),
        navigateToNext = {}
    )
}
