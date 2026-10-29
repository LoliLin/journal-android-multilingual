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

package com.isaakhanimann.journal

import com.isaakhanimann.journal.data.room.experiences.entities.custom.CustomDoseInfo
import com.isaakhanimann.journal.data.room.experiences.entities.custom.CustomRoaInfo
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.ui.tabs.search.custom.withDoseInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Merging an edited dose table must touch only the edited route. A regression here writes one
 * route's thresholds onto another, which is invisible in the UI until the dose dots are wrong.
 */
class TestCustomRoaInfoMerge {

    private val oralDose = CustomDoseInfo(
        units = "mg",
        lightMin = 10.0,
        commonMin = 20.0,
        strongMin = 40.0,
        heavyMin = 60.0
    )
    private val insufflatedDose = CustomDoseInfo(units = "mg", lightMin = 5.0, commonMin = 10.0)

    @Test
    fun addsRouteWhenAbsent() {
        val result = emptyList<CustomRoaInfo>()
            .withDoseInfo(AdministrationRoute.ORAL, oralDose)

        assertEquals(1, result.size)
        assertEquals(AdministrationRoute.ORAL, result.single().administrationRoute)
        assertEquals(oralDose, result.single().doseInfo)
    }

    @Test
    fun replacesOnlyTheEditedRoute() {
        val existing = listOf(
            CustomRoaInfo(administrationRoute = AdministrationRoute.ORAL, doseInfo = oralDose),
            CustomRoaInfo(
                administrationRoute = AdministrationRoute.INSUFFLATED,
                doseInfo = insufflatedDose
            )
        )
        val newOral = oralDose.copy(commonMin = 25.0)

        val result = existing.withDoseInfo(AdministrationRoute.ORAL, newOral)

        assertEquals(2, result.size)
        assertEquals(newOral, result.single { it.administrationRoute == AdministrationRoute.ORAL }.doseInfo)
        assertEquals(
            "the other route's table must survive untouched",
            insufflatedDose,
            result.single { it.administrationRoute == AdministrationRoute.INSUFFLATED }.doseInfo
        )
    }

    @Test
    fun applyingTwoRoutesKeepsBoth() {
        var result = emptyList<CustomRoaInfo>()
        result = result.withDoseInfo(AdministrationRoute.ORAL, oralDose)
        result = result.withDoseInfo(AdministrationRoute.INSUFFLATED, insufflatedDose)

        assertEquals(2, result.size)
        assertEquals(
            oralDose,
            result.single { it.administrationRoute == AdministrationRoute.ORAL }.doseInfo
        )
        assertEquals(
            insufflatedDose,
            result.single { it.administrationRoute == AdministrationRoute.INSUFFLATED }.doseInfo
        )
    }

    @Test
    fun durationInfoSurvivesADoseEdit() {
        // A route may already carry duration data; replacing the dose must not drop it.
        val withDuration = CustomRoaInfo(
            administrationRoute = AdministrationRoute.ORAL,
            doseInfo = oralDose
        )
        val result = listOf(withDuration).withDoseInfo(AdministrationRoute.ORAL, insufflatedDose)

        assertEquals(insufflatedDose, result.single().doseInfo)
        assertNull(result.single().durationInfo)
    }

    @Test
    fun emptyThresholdsArePreservedAsUnknown() {
        val sparse = CustomDoseInfo(units = "mg", lightMin = 3.0)
        val result = listOf(CustomRoaInfo(administrationRoute = AdministrationRoute.ORAL))
            .withDoseInfo(AdministrationRoute.ORAL, sparse)

        val stored = result.single().doseInfo!!
        assertEquals(3.0, stored.lightMin!!, 0.0)
        assertNull(stored.commonMin)
        assertNull(stored.strongMin)
        assertNull(stored.heavyMin)
    }
}
