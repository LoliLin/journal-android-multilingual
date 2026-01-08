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

package com.isaakhanimann.journal.data.substances.classes.roa

enum class DurationUnits(val text: String) {
    SECONDS("seconds") {
        override val inSecondsMultiplier = 1
        override val shortKey = "duration_seconds_short"
        override val fallbackShortText = "s"
    },
    MINUTES("minutes") {
        override val inSecondsMultiplier = 60
        override val shortKey = "duration_minutes_short"
        override val fallbackShortText = "m"
    },
    HOURS("hours") {
        override val inSecondsMultiplier = 3600
        override val shortKey = "duration_hours_short"
        override val fallbackShortText = "h"
    },
    DAYS("days") {
        override val inSecondsMultiplier = 86400
        override val shortKey = "duration_days_short"
        override val fallbackShortText = "d"
    };

    abstract val inSecondsMultiplier: Int
    abstract val shortKey: String
    abstract val fallbackShortText: String
}
