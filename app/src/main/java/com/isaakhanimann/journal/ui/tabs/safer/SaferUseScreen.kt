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

package com.isaakhanimann.journal.ui.tabs.safer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.main.bottomBarNestedScroll
import com.isaakhanimann.journal.ui.main.bottomBarOverlayDp
import com.isaakhanimann.journal.ui.tabs.search.substance.SectionWithTitle
import com.isaakhanimann.journal.ui.tabs.search.substance.VerticalSpace
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import com.isaakhanimann.journal.ui.utils.rememberOpenLink
import com.isaakhanimann.journal.ui.theme.verticalPaddingCards

@Preview
@Composable
fun SaferUsePreview() {
    SaferUseScreen(
        navigateToDrugTestingScreen = {},
        navigateToSaferHallucinogensScreen = {},
        navigateToVolumetricDosingScreen = {},
        navigateToDosageGuideScreen = {},
        navigateToDosageClassificationScreen = {},
        navigateToRouteExplanationScreen = {},
        navigateToURL = {},
        navigateToReagentTestingScreen = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaferUseScreen(
    navigateToDrugTestingScreen: () -> Unit,
    navigateToSaferHallucinogensScreen: () -> Unit,
    navigateToVolumetricDosingScreen: () -> Unit,
    navigateToDosageGuideScreen: () -> Unit,
    navigateToDosageClassificationScreen: () -> Unit,
    navigateToRouteExplanationScreen: () -> Unit,
    navigateToURL: (url: String) -> Unit,
    navigateToReagentTestingScreen: () -> Unit
) {
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier
            .bottomBarNestedScroll()
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                scrollBehavior = topBarScrollBehavior,
                title = { Text(i18n("safer_use_title")) }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(bottom = bottomBarOverlayDp())
        ) {
            SectionWithTitle(title = i18n("safer_research_title")) {
                SaferText(text = i18n("safer_research_body"))
            }
            val openLink = rememberOpenLink()
            SectionWithTitle(title = i18n("safer_testing_title")) {
                SaferText(text = i18n("safer_testing_body"))
                Button(
                    onClick = navigateToDrugTestingScreen,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                ) {
                    Icon(
                        Icons.Outlined.Biotech,
                        contentDescription = i18n("common_open_link"),
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(i18n("safer_drug_testing_services"))
                }
                Button(
                    onClick = navigateToReagentTestingScreen,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                ) {
                    Icon(
                        Icons.Outlined.Science,
                        contentDescription = i18n("common_open_link"),
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(i18n("safer_reagent_testing"))
                }
                VerticalSpace()
            }
            SectionWithTitle(title = i18n("safer_dosage_title")) {
                SaferText(text = i18n("safer_dosage_body"))
                Button(
                    onClick = navigateToDosageGuideScreen,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                ) {
                    Text(i18n("safer_dosage_guide"))
                }
                Button(
                    onClick = navigateToDosageClassificationScreen,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                ) {
                    Text(i18n("safer_dosage_classification"))
                }
                Button(
                    onClick = navigateToVolumetricDosingScreen,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                ) {
                    Text(i18n("safer_volumetric_dosing"))
                }
                VerticalSpace()
            }
            SectionWithTitle(title = i18n("safer_set_setting_title")) {
                SaferText(text = i18n("safer_set_setting_body"))
                Button(
                    onClick = navigateToSaferHallucinogensScreen,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                ) {
                    Text(i18n("safer_hallucinogen_guide"))
                }
                VerticalSpace()
            }
            SectionWithTitle(title = i18n("safer_combinations_title")) {
                SaferText(text = i18n("safer_combinations_body"))
                Button(
                    onClick = { openLink("https://combi-checker.ch") },
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                ) {
                    Icon(
                        Icons.Default.OpenInBrowser,
                        contentDescription = i18n("common_open_link"),
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(i18n("safer_swiss_combo_checker"))
                }
                Button(
                    onClick = { openLink("https://combo.tripsit.me") },
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                ) {
                    Icon(
                        Icons.Default.OpenInBrowser,
                        contentDescription = i18n("common_open_link"),
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(i18n("safer_tripsit_combo_checker"))
                }
                VerticalSpace()
            }
            SectionWithTitle(title = i18n("safer_administration_routes_title")) {
                SaferText(text = i18n("safer_administration_routes_body"))
                Button(
                    onClick = {
                        openLink(
                            "https://www.youtube.com/watch?v=31fuvYXxeV0&list=PLkC348-BeCu6Ut-iJy8xp9_LLKXoMMroR"
                        )
                    },
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = i18n("common_open_link"),
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(i18n("safer_snorting_video"))
                }
                Button(
                    onClick = {
                        openLink(
                            "https://www.youtube.com/watch?v=lBlS2e46CV0&list=PLkC348-BeCu6Ut-iJy8xp9_LLKXoMMroR"
                        )
                    },
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = i18n("common_open_link"),
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(i18n("safer_smoking_video"))
                }
                Button(
                    onClick = {
                        openLink(
                            "https://www.youtube.com/watch?v=N7HjCPz4A7Y&list=PLkC348-BeCu6Ut-iJy8xp9_LLKXoMMroR"
                        )
                    },
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = i18n("common_open_link"),
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(i18n("safer_injecting_video"))
                }
                Button(
                    onClick = navigateToRouteExplanationScreen,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = i18n("substance_info"),
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(i18n("safer_administration_routes_info"))
                }
                VerticalSpace()
            }
            SectionWithTitle(title = i18n("safer_allergy_tests_title")) {
                SaferText(text = i18n("safer_allergy_tests_body"))
            }
            SectionWithTitle(title = i18n("safer_reflection_title")) {
                SaferText(text = i18n("safer_reflection_body"))
            }
            SectionWithTitle(title = i18n("safer_safety_of_others_title")) {
                SaferText(text = i18n("safer_safety_of_others_body"))
            }
            SectionWithTitle(title = i18n("safer_recovery_position_title")) {
                SaferText(text = i18n("safer_recovery_position_body"))
                Button(
                    onClick = { openLink("https://www.youtube.com/watch?v=dv3agW-DZ5I") },
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = i18n("common_open_link"),
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(i18n("safer_recovery_position_video"))
                }
                VerticalSpace()
            }
            ElevatedCard(
                modifier = Modifier.padding(
                    horizontal = horizontalPadding,
                    vertical = verticalPaddingCards
                )
            ) {
                TextButton(
                    onClick = {
                        navigateToURL("https://psychonautwiki.org/wiki/Responsible_drug_use")
                    },
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                ) {
                    Icon(
                        Icons.Outlined.HealthAndSafety,
                        contentDescription = i18n("safer_responsible_drug_use"),
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(i18n("safer_responsible_drug_use_article"))
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun SaferText(text: String) {
    Text(
        text = text,
        textAlign = TextAlign.Left,
        modifier = Modifier
            .padding(horizontal = horizontalPadding)
            .padding(bottom = 10.dp)
    )
}
