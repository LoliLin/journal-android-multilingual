/*
 * Copyright (c) 2023. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.tabs.journal.experience.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.localization.i18nOrDefault
import com.isaakhanimann.journal.ui.tabs.journal.experience.models.CumulativeDose
import com.isaakhanimann.journal.ui.tabs.journal.experience.models.CumulativeRouteAndDose
import com.isaakhanimann.journal.ui.utils.administrationRouteKey

@Composable
fun CumulativeDoseRow(
    cumulativeDose: CumulativeDose,
    areDosageDotsHidden: Boolean,
    getSubstanceDisplayName: (String) -> String,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = getSubstanceDisplayName(cumulativeDose.substanceName),
            style = MaterialTheme.typography.titleMedium
        )
        cumulativeDose.cumulativeRouteAndDose.forEach { cumulativeRouteAndDose ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val routeName = i18nOrDefault(
                    administrationRouteKey(cumulativeRouteAndDose.route),
                    cumulativeRouteAndDose.route.displayText
                ).lowercase()
                val text = buildAnnotatedString {
                    append(cumulativeRouteAndDose.doseDescription)
                    withStyle(style = SpanStyle(color = Color.Gray)) {
                        append(" $routeName")
                    }
                }
                Text(text = text, style = MaterialTheme.typography.titleSmall)
                val numDots = cumulativeRouteAndDose.numDots
                if (numDots != null && !areDosageDotsHidden) {
                    DotRows(numDots = numDots)
                }
            }
        }
    }
}
