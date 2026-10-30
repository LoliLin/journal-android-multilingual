package com.isaakhanimann.journal.data.substances.classes.roa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoaDoseInputValidationTest {
    private val doseInfo = RoaDose(
        units = "mg",
        lightMin = 10.0,
        commonMin = 20.0,
        strongMin = 50.0,
        heavyMin = 100.0
    )

    @Test
    fun invalidIngestionDosesDoNotProduceClassesOrDots() {
        listOf(
            -1.0,
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY
        ).forEach { invalidDose ->
            assertNull(doseInfo.getDoseClass(invalidDose))
            assertNull(doseInfo.getNumDots(invalidDose))
        }
    }

    @Test
    fun zeroDoseRemainsAValidThresholdDose() {
        assertEquals(DoseClass.THRESHOLD, doseInfo.getDoseClass(0.0))
        assertEquals(0, doseInfo.getNumDots(0.0))
    }

    @Test
    fun descendingThresholdsDoNotProduceDoseClassesOrDots() {
        val invalidDoseInfo = RoaDose(
            units = "mg",
            lightMin = 10.0,
            commonMin = 20.0,
            strongMin = 50.0,
            heavyMin = 40.0
        )

        assertNull(invalidDoseInfo.getDoseClass(60.0))
        assertNull(invalidDoseInfo.getNumDots(60.0))
    }

    @Test
    fun equalThresholdsRemainValidForSkippedDoseClasses() {
        val doseInfo = RoaDose(
            units = "mg",
            lightMin = 10.0,
            commonMin = 20.0,
            strongMin = 50.0,
            heavyMin = 50.0
        )

        assertEquals(DoseClass.HEAVY, doseInfo.getDoseClass(50.0))
        assertEquals(4, doseInfo.getNumDots(50.0))
    }

    @Test
    fun hugeFiniteDoseSaturatesDotCountInsteadOfOverflowing() {
        val doseInfo = RoaDose(
            units = "mg",
            lightMin = null,
            commonMin = null,
            strongMin = null,
            heavyMin = 1.0
        )

        assertEquals(Int.MAX_VALUE, doseInfo.getNumDots(Double.MAX_VALUE))
    }
}
