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

import kotlin.math.floor
import kotlin.math.roundToInt

data class RoaDose(
    val units: String,
    val lightMin: Double?,
    val commonMin: Double?,
    val strongMin: Double?,
    val heavyMin: Double?
) {
    fun getDoseClass(ingestionDose: Double?, ingestionUnits: String? = units): DoseClass? {
        val dose = ingestionDose?.takeIf { it.isFinite() && it >= 0.0 } ?: return null
        if (ingestionUnits != units) return null
        if (!hasValidThresholds()) return null
        return if (lightMin != null && dose < lightMin) {
            DoseClass.THRESHOLD
        } else if (lightMin != null &&
            commonMin != null &&
            lightMin <= dose &&
            dose < commonMin
        ) {
            DoseClass.LIGHT
        } else if (commonMin != null &&
            strongMin != null &&
            commonMin <= dose &&
            dose < strongMin
        ) {
            DoseClass.COMMON
        } else if (strongMin != null &&
            heavyMin != null &&
            strongMin <= dose &&
            dose < heavyMin
        ) {
            DoseClass.STRONG
        } else if (heavyMin != null && heavyMin <= dose) {
            DoseClass.HEAVY
        } else {
            null
        }
    }

    fun getNumDots(ingestionDose: Double?, ingestionUnits: String? = units): Int? {
        val dose = ingestionDose?.takeIf { it.isFinite() && it >= 0.0 } ?: return null
        if (ingestionUnits != units) return null
        if (!hasValidThresholds()) return null
        if (lightMin != null && dose < lightMin) {
            return 0
        } else if (lightMin != null &&
            commonMin != null &&
            lightMin <= dose &&
            dose < commonMin
        ) {
            return 1
        } else if (commonMin != null &&
            strongMin != null &&
            commonMin <= dose &&
            dose < strongMin
        ) {
            return 2
        } else if (strongMin != null &&
            heavyMin != null &&
            strongMin <= dose &&
            dose < heavyMin
        ) {
            return 3
        } else if (heavyMin != null) {
            return if (heavyMin <= dose) {
                val fullHeavyDots = (floor(dose.div(heavyMin)) * 4.0)
                    .coerceAtMost(Int.MAX_VALUE.toDouble())
                    .toInt()
                val restDots = getNumDotsUpTo4(dose = dose.rem(heavyMin))
                (fullHeavyDots.toLong() + restDots)
                    .coerceAtMost(Int.MAX_VALUE.toLong())
                    .toInt()
            } else {
                floor(dose / heavyMin).roundToInt()
            }
        } else {
            return null
        }
    }

    private fun hasValidThresholds(): Boolean {
        val thresholds = listOf(lightMin, commonMin, strongMin, heavyMin)
        if (thresholds.any { it != null && (!it.isFinite() || it <= 0.0) }) return false
        return thresholds.filterNotNull().zipWithNext().all { (lower, upper) -> lower <= upper }
    }

    private fun getNumDotsUpTo4(dose: Double): Int = if (lightMin != null && dose < lightMin) {
        0
    } else if (lightMin != null && commonMin != null && lightMin <= dose && dose < commonMin) {
        1
    } else if (commonMin != null &&
        strongMin != null &&
        commonMin <= dose &&
        dose < strongMin
    ) {
        2
    } else if (strongMin != null && heavyMin != null && strongMin <= dose && dose < heavyMin) {
        3
    } else if (heavyMin != null) {
        floor(dose / heavyMin).roundToInt()
    } else {
        0
    }

    val shouldUseVolumetricDosing: Boolean
        get() {
            if (units == "µg") return true
            return if (units == "mg") {
                val sample = commonMin ?: strongMin
                sample != null && sample < 15
            } else {
                false
            }
        }
}
