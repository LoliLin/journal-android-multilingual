package com.isaakhanimann.journal.ui.tabs.journal.experience.editingestion

import java.time.LocalDateTime
import java.time.ZoneId
import com.isaakhanimann.journal.ui.utils.parseNonNegativeFiniteNumber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EditIngestionValidationTest {
    @Test
    fun doseParserAcceptsFiniteNonNegativeValuesOnly() {
        assertEquals(0.0, parseNonNegativeFiniteNumber("0")!!, 0.0)
        assertEquals(2.5, parseNonNegativeFiniteNumber("2.5")!!, 0.0)
        assertNull(parseNonNegativeFiniteNumber(""))
        assertNull(parseNonNegativeFiniteNumber("dose"))
        assertNull(parseNonNegativeFiniteNumber("-1"))
        assertNull(parseNonNegativeFiniteNumber("NaN"))
        assertNull(parseNonNegativeFiniteNumber("Infinity"))
    }

    @Test
    fun unknownDoseAndBlankEstimatedDeviationRemainValid() {
        assertTrue(isDoseInputValid(isKnown = false, dose = ""))
        assertFalse(isDoseInputValid(isKnown = true, dose = ""))
        assertTrue(
            isEstimatedDeviationInputValid(
                isKnown = true,
                isEstimate = true,
                deviation = " "
            )
        )
        assertFalse(
            isEstimatedDeviationInputValid(
                isKnown = true,
                isEstimate = true,
                deviation = "invalid"
            )
        )
        assertTrue(
            isEstimatedDeviationInputValid(
                isKnown = false,
                isEstimate = true,
                deviation = "invalid"
            )
        )
    }

    @Test
    fun timeRangeAllowsEqualOrLaterEndAndRejectsEarlierEnd() {
        val zone = ZoneId.of("UTC")
        val start = LocalDateTime.of(2026, 10, 30, 12, 0)
        assertTrue(isTimeRangeInputValid(start, start, zone))
        assertTrue(isTimeRangeInputValid(start, start.plusMinutes(30), zone))
        assertFalse(isTimeRangeInputValid(start, start.minusMinutes(1), zone))
    }
}
