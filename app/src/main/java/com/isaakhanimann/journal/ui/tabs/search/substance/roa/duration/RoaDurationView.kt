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

package com.isaakhanimann.journal.ui.tabs.search.substance.roa.duration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.data.substances.classes.roa.DurationRange
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDuration
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.localization.i18nOrDefault
import com.isaakhanimann.journal.ui.theme.JournalTheme

@Preview(showBackground = true)
@Composable
fun RoaDurationPreview(
    @PreviewParameter(RoaDurationPreviewProvider::class) roaDuration: RoaDuration
) {
    JournalTheme {
        RoaDurationView(roaDuration = roaDuration)
    }
}

@Composable
fun RoaDurationView(roaDuration: RoaDuration) {
    Column {
        val total = roaDuration.total
        val afterglow = roaDuration.afterglow
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val onset = roaDuration.onset
            val comeup = roaDuration.comeup
            val peak = roaDuration.peak
            val offset = roaDuration.offset
            TimeSurface(durationRange = onset, labelKey = "duration_onset")
            TimeSurface(durationRange = comeup, labelKey = "duration_comeup")
            TimeSurface(durationRange = peak, labelKey = "duration_peak")
            TimeSurface(durationRange = offset, labelKey = "duration_offset")
        }
        if (total != null || afterglow != null) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (total != null) {
                    Text("${i18n("duration_total")}: ${formatDurationRange(total)}")
                }
                if (afterglow != null) {
                    Text("${i18n("duration_after_effects")}: ${formatDurationRange(afterglow)}")
                }
            }
        }
    }
}

@Composable
fun TimeSurface(durationRange: DurationRange?, labelKey: String) {
    if (durationRange != null) {
        Surface(shape = RoundedCornerShape(5.dp), tonalElevation = 12.dp) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(
                    horizontal = 7.dp,
                    vertical = 2.dp
                )
            ) {
                Text(formatDurationRange(durationRange))
                Text(i18n(labelKey))
            }
        }
    }
}

@Composable
private fun formatDurationRange(durationRange: DurationRange): String {
    val minText = durationRange.min?.toString()?.removeSuffix(".0") ?: ".."
    val maxText = durationRange.max?.toString()?.removeSuffix(".0") ?: ".."
    val unitKey = durationRange.units?.shortKey ?: ""
    val unitFallback = durationRange.units?.fallbackShortText ?: ""
    val unitText = if (unitKey.isNotEmpty()) {
        i18nOrDefault(unitKey, unitFallback)
    } else {
        ""
    }
    return "$minText-$maxText$unitText"
}
