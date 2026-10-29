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

import com.isaakhanimann.journal.data.substances.classes.roa.DurationUnits
import kotlinx.serialization.Serializable

/**
 * A duration range in a serializable form.
 *
 * [DurationUnits] itself is a plain enum owned by the substance model; this mirrors it so the
 * stored JSON does not depend on that enum's declaration order or future members.
 */
@Serializable
data class SerializableDurationRange(
    val min: Float? = null,
    val max: Float? = null,
    val units: SerializableDurationUnits? = null
)

fun SerializableDurationRange.toDurationRange() = com.isaakhanimann.journal.data.substances.classes.roa.DurationRange(
    min = min,
    max = max,
    units = units?.toDurationUnits()
)

@Serializable
enum class SerializableDurationUnits {
    SECONDS,
    MINUTES,
    HOURS,
    DAYS;

    fun toDurationUnits(): DurationUnits = when (this) {
        SECONDS -> DurationUnits.SECONDS
        MINUTES -> DurationUnits.MINUTES
        HOURS -> DurationUnits.HOURS
        DAYS -> DurationUnits.DAYS
    }
}

fun DurationUnits.toSerializableDurationUnits(): SerializableDurationUnits = when (this) {
    DurationUnits.SECONDS -> SerializableDurationUnits.SECONDS
    DurationUnits.MINUTES -> SerializableDurationUnits.MINUTES
    DurationUnits.HOURS -> SerializableDurationUnits.HOURS
    DurationUnits.DAYS -> SerializableDurationUnits.DAYS
}
