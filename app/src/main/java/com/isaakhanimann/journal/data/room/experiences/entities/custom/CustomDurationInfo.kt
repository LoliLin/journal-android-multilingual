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

import com.isaakhanimann.journal.data.substances.classes.roa.RoaDuration
import kotlinx.serialization.Serializable

/**
 * The duration phases of a custom substance for one route. Every phase is optional because a
 * user often only knows e.g. the total duration of their own substance.
 */
@Serializable
data class CustomDurationInfo(
    val onset: SerializableDurationRange? = null,
    val comeup: SerializableDurationRange? = null,
    val peak: SerializableDurationRange? = null,
    val offset: SerializableDurationRange? = null,
    val total: SerializableDurationRange? = null,
    val afterglow: SerializableDurationRange? = null
)

fun CustomDurationInfo.toRoaDuration() = RoaDuration(
    onset = onset?.toDurationRange(),
    comeup = comeup?.toDurationRange(),
    peak = peak?.toDurationRange(),
    offset = offset?.toDurationRange(),
    total = total?.toDurationRange(),
    afterglow = afterglow?.toDurationRange()
)
