package com.isaakhanimann.journal.ui.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberInputValidationTest {
    @Test
    fun nonNegativeParserAcceptsZeroAndLocaleDecimalSeparator() {
        assertEquals(0.0, parseNonNegativeFiniteNumber("0")!!, 0.0)
        assertEquals(2.5, parseNonNegativeFiniteNumber(" 2,5 ")!!, 0.0)
    }

    @Test
    fun nonNegativeParserRejectsNegativeAndNonFiniteValues() {
        assertNull(parseNonNegativeFiniteNumber("-1"))
        assertNull(parseNonNegativeFiniteNumber("NaN"))
        assertNull(parseNonNegativeFiniteNumber("Infinity"))
        assertNull(parseNonNegativeFiniteNumber(""))
    }

    @Test
    fun positiveParserRejectsZeroAndNonFiniteValues() {
        assertTrue(parsePositiveFiniteNumber("0.1")!! > 0.0)
        assertNull(parsePositiveFiniteNumber("0"))
        assertNull(parsePositiveFiniteNumber("-1"))
        assertNull(parsePositiveFiniteNumber("NaN"))
        assertNull(parsePositiveFiniteNumber("Infinity"))
    }

    @Test
    fun scalingRequiresKnownFiniteValuesAndRejectsOverflow() {
        assertEquals(80.0, scaleDoseByUnit(2.0, 40.0)!!, 0.0)
        assertNull(scaleDoseByUnit(2.0, null))
        assertNull(scaleDoseByUnit(2.0, 0.0))
        assertNull(scaleDoseByUnit(-2.0, 40.0))
        assertNull(scaleDoseByUnit(Double.MAX_VALUE, 2.0))
    }
}
