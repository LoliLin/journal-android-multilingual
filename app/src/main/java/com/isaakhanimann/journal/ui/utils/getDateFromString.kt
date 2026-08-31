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

package com.isaakhanimann.journal.ui.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun getInstant(year: Int, month: Int, day: Int, hourOfDay: Int, minute: Int): Instant? {
    val dateTime = LocalDateTime.of(year, month, day, hourOfDay, minute)
    return dateTime.atZone(ZoneId.systemDefault()).toInstant()
}

fun Instant.getStringOfPattern(pattern: String): String {
    val dateTime = LocalDateTime.ofInstant(this, ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern(pattern, DateFormat.locale())
    return dateTime.format(formatter)
}

fun Instant.getDateWithWeekdayText(): String = DateFormat.format(
    LocalDateTime.ofInstant(this, ZoneId.systemDefault()),
    "EEEEMMMdyyyy"
)

fun Instant.getShortWeekdayText(): String = DateFormat.format(
    LocalDateTime.ofInstant(this, ZoneId.systemDefault()),
    "EEE"
)

fun Instant.getShortTimeWithWeekdayText(): String {
    return getShortWeekdayText() + " " + getShortTimeText()
}

fun Instant.getShortTimeText(): String = getStringOfPattern(TimeFormat.timePattern)

/** Clock time only, honouring the 12/24 hour preference. */
fun Instant.getTimeText(): String = getStringOfPattern(TimeFormat.timePattern)

/** Weekday plus clock time, honouring the 12/24 hour preference. */
fun Instant.getWeekdayTimeText(): String =
    getShortWeekdayText() + " " + getTimeText()

fun Instant.getMediumDateText(): String = DateFormat.format(
    LocalDateTime.ofInstant(this, ZoneId.systemDefault()),
    "MMMdyyyy"
)

fun Instant.getLongDateText(): String = DateFormat.format(
    LocalDateTime.ofInstant(this, ZoneId.systemDefault()),
    "MMMMdyyyy"
)

fun LocalDateTime.getStringOfPattern(pattern: String): String {
    val formatter = DateTimeFormatter.ofPattern(pattern, DateFormat.locale())
    return this.format(formatter)
}

fun LocalDateTime.getDateWithWeekdayText(): String =
    DateFormat.format(this, "EEEEMMMdyyyy")

fun LocalDateTime.getShortTimeText(): String = getStringOfPattern(TimeFormat.timePattern)

/** Clock time only, honouring the 12/24 hour preference. */
fun LocalDateTime.getTimeText(): String = getStringOfPattern(TimeFormat.timePattern)

/** Weekday plus clock time, honouring the 12/24 hour preference. */
fun LocalDateTime.getWeekdayTimeText(): String =
    DateFormat.format(this, "EEE") + " " + getTimeText()

fun LocalDateTime.getMediumDateText(): String = DateFormat.format(this, "MMMdyyyy")

fun LocalDateTime.getLongDateText(): String = DateFormat.format(this, "MMMMdyyyy")

fun Instant.getLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(this, ZoneId.systemDefault())

fun LocalDateTime.getInstant(): Instant = this.atZone(ZoneId.systemDefault()).toInstant()

fun getLocalizedPatternString(yearMonth: YearMonth): String {
    val locale = DateFormat.locale()
    val pattern = DateFormat.bestPattern("MMMyyyy", locale)
    return yearMonth.format(DateTimeFormatter.ofPattern(pattern, locale))
}
