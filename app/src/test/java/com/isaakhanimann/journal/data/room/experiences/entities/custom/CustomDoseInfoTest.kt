package com.isaakhanimann.journal.data.room.experiences.entities.custom

import com.isaakhanimann.journal.data.substances.AdministrationRoute
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomDoseInfoTest {
    @Test
    fun acceptsSparseAscendingThresholdsWithUnits() {
        val doseInfo = CustomDoseInfo(
            units = "mg",
            lightMin = 5.0,
            strongMin = 20.0
        )

        assertTrue(doseInfo.hasValidThresholds())
        assertNotNull(CustomRoaInfo(AdministrationRoute.ORAL, doseInfo).toRoaDose())
    }

    @Test
    fun rejectsMissingUnitsNonPositiveAndNonFiniteThresholds() {
        assertFalse(CustomDoseInfo(lightMin = 5.0).hasValidThresholds())
        assertFalse(CustomDoseInfo("mg", lightMin = 0.0).hasValidThresholds())
        assertFalse(CustomDoseInfo("mg", lightMin = Double.NaN).hasValidThresholds())
        assertFalse(CustomDoseInfo("mg", heavyMin = Double.POSITIVE_INFINITY).hasValidThresholds())
        assertNull(CustomRoaInfo(AdministrationRoute.ORAL, CustomDoseInfo("mg", lightMin = -1.0)).toRoaDose())
    }

    @Test
    fun acceptsEqualThresholdsToSkipDoseClasses() {
        val doseInfo = CustomDoseInfo(
            units = "mg",
            lightMin = 5.0,
            commonMin = 5.0,
            strongMin = 20.0
        )

        assertTrue(doseInfo.hasValidThresholds())
        assertNotNull(CustomRoaInfo(AdministrationRoute.ORAL, doseInfo).toRoaDose())
    }

    @Test
    fun rejectsThresholdsOutOfDoseClassOrder() {
        val doseInfo = CustomDoseInfo(
            units = "mg",
            lightMin = 10.0,
            commonMin = 8.0
        )

        assertFalse(doseInfo.hasValidThresholds())
    }
}
