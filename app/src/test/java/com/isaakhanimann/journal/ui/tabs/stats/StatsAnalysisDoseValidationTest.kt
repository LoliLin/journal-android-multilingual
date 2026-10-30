package com.isaakhanimann.journal.ui.tabs.stats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsAnalysisDoseValidationTest {
    @Test
    fun referenceDoseUsesStableMidpointForLargeFiniteThresholds() {
        val lower = Double.MAX_VALUE * 0.75
        val upper = Double.MAX_VALUE
        val reference = commonDoseReference(lower, upper) ?: error("Expected a valid reference")

        assertTrue(reference.isFinite())
        assertTrue(reference > lower)
        assertTrue(reference < upper)
    }

    @Test
    fun rejectsInvalidDoseThresholdRangesButAllowsSkippedClasses() {
        assertNull(commonDoseReference(null, 20.0))
        assertNull(commonDoseReference(0.0, 20.0))
        assertNull(commonDoseReference(Double.NaN, 20.0))
        assertNull(commonDoseReference(20.0, 10.0))
        assertEquals(10.0, commonDoseReference(10.0, 10.0)!!, 0.0)
        assertEquals(10.0, commonDoseReference(10.0, null)!!, 0.0)
    }
}
