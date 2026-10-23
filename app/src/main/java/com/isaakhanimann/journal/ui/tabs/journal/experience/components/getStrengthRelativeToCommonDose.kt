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

import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithCompanionAndCustomUnit
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDose
import kotlin.math.max

/**
 * Height for the SHARED-scale mode ("are substance heights independent" = off).
 *
 * Scales the ingestion height relative to the common dose of its substance
 * (midpoint between commonMin and strongMin), so heights stay comparable
 * ACROSS different substances: a curve above 1.0 means above the common
 * range. Falls back to the average of the recorded doses of the same
 * substance when the substance data has no common/strong range, and to 1.0
 * when the dose itself is unknown.
 *
 * Restores the upstream semantics of commit 391efaec
 * ("Scale height of timeline relative to strength by default").
 */
fun getStrengthRelativeToCommonDose(
    ingestion: IngestionWithCompanionAndCustomUnit,
    allIngestions: List<IngestionWithCompanionAndCustomUnit>,
    roaDose: RoaDose?,
): Float {
    val allKnownDoses = allIngestions
        .filter { it.ingestion.substanceName == ingestion.ingestion.substanceName }
        .mapNotNull { it.pureDose }
    val sumDose = allKnownDoses.reduceOrNull { acc, d -> acc + d } ?: 0.0
    val averageDose = sumDose / max(1.0, allKnownDoses.size.toDouble())
    var commonDose = averageDose
    val commonMin = roaDose?.commonMin
    val commonMax = roaDose?.strongMin
    if (commonMin != null && commonMax != null) {
        commonDose = (commonMin + commonMax) / 2
    }

    val strength = ingestion.pureDose?.let { doseSnap ->
        doseSnap / commonDose
    } ?: 1.0
    return strength.coerceAtLeast(0.01).toFloat()
}

/**
 * Height for the INDEPENDENT-scale mode ("are substance heights independent" = on).
 *
 * Normalizes the dose against the strongest ingestion OF THE SAME SUBSTANCE,
 * so every substance's tallest ingestion reaches full height regardless of
 * other substances on the timeline.
 */
fun getHeightBetween0And1(
    ingestion: IngestionWithCompanionAndCustomUnit,
    allIngestions: List<IngestionWithCompanionAndCustomUnit>
): Float {
    val max = allIngestions
        .filter { it.ingestion.substanceName == ingestion.ingestion.substanceName }
        .mapNotNull { it.pureDose }
        .maxOrNull()
    return ingestion.pureDose.let { doseSnap ->
        if (doseSnap == 0.0) {
            0.01f
        } else if (max == null || max == 0.0 || doseSnap == null) {
            1f
        } else {
            doseSnap.div(max).toFloat()
        }
    }
}
