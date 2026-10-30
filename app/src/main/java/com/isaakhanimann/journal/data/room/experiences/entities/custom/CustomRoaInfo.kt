/*
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

package com.isaakhanimann.journal.data.room.experiences.entities.custom

import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDose
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDuration
import kotlinx.serialization.Serializable

/**
 * Dose and duration knowledge attached to a user-defined substance for one route.
 *
 * Kept as a serialized column on [com.isaakhanimann.journal.data.room.experiences.entities.CustomSubstance]
 * rather than its own table: the data is small, always read together with its substance and
 * never queried across substances.
 */
@Serializable
data class CustomRoaInfo(
    val administrationRoute: AdministrationRoute,
    val doseInfo: CustomDoseInfo? = null,
    val durationInfo: CustomDurationInfo? = null
)

/**
 * Returns the dose thresholds as the same shape the wiki substances use, so custom substances
 * can render dose dots and dose classes with the existing components. Null when no unit was
 * configured, because a threshold without a unit cannot be compared to an ingestion.
 */
fun CustomRoaInfo.toRoaDose(): RoaDose? {
    val validDoseInfo = doseInfo?.takeIf { it.hasValidThresholds() } ?: return null
    val units = validDoseInfo.units?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return RoaDose(
        units = units,
        lightMin = validDoseInfo.lightMin,
        commonMin = validDoseInfo.commonMin,
        strongMin = validDoseInfo.strongMin,
        heavyMin = validDoseInfo.heavyMin
    )
}

fun CustomRoaInfo.toRoaDuration(): RoaDuration? = durationInfo?.toRoaDuration()
