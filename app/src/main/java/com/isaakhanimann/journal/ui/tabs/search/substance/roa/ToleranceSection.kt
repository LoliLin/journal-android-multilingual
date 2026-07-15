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

package com.isaakhanimann.journal.ui.tabs.search.substance.roa

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.data.substances.classes.Tolerance
import com.isaakhanimann.journal.localization.i18n

@Composable
fun ToleranceSection(
    tolerance: Tolerance?,
    crossTolerances: List<String>,
    modifier: Modifier = Modifier,
    isSubstance: ((String) -> Boolean),
    getSubstanceDisplayName: ((String) -> String),
    navToSubstance: ((String) -> Unit) = {},
    isCategory: ((String) -> Boolean),
    getCategoryDisplayName: ((String) -> String),
    navToCategory: ((String) -> Unit) = {}
) {
    val descriptor: (String) -> Pair<String, (String) -> Unit> = { name ->
        when {
            isSubstance(name) -> getSubstanceDisplayName(name) to { navToSubstance(name) }
            isCategory(name) -> getCategoryDisplayName(name) to { navToCategory(name) }
            else -> name to {}
        }
    }

    ToleranceSection(
        tolerance = tolerance,
        crossTolerances = crossTolerances,
        modifier = modifier,
        crossToleranceDescriptor = descriptor
    )
}

@Composable
fun ToleranceSection(
    tolerance: Tolerance?,
    crossTolerances: List<String>,
    modifier: Modifier = Modifier,
    crossToleranceDescriptor: ((String) -> Pair<String, (String) -> Unit>)
) {
    if (tolerance != null || crossTolerances.isNotEmpty()) {
        Column(modifier) {
            tolerance?.let { tol ->
                listOf(
                    i18n("tolerance_full_label") to tol.full,
                    i18n("tolerance_half_label") to tol.half,
                    i18n("tolerance_zero_label") to tol.zero
                ).filter { it.second != null }.forEach { (label, value) ->
                    ToleranceItem(label = label, value = value!!)
                }
                if (tol.zero != null) {
                    Text(
                        text = i18n("tolerance_zero_explanation"),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (crossTolerances.isNotEmpty()) {
                val topPadding = if (tolerance != null) 8.dp else 0.dp

                Text(
                    text = i18n("tolerance_cross_with_title"),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = topPadding)
                )

                Column(
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    crossTolerances.forEachIndexed { index, originalName ->
                        val (displayName, clickAction) = crossToleranceDescriptor(originalName)
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { clickAction(originalName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToleranceItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
