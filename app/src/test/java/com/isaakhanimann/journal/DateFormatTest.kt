package com.isaakhanimann.journal

import com.isaakhanimann.journal.ui.utils.DateFormat
import com.isaakhanimann.journal.ui.utils.DateLocaleOption
import com.isaakhanimann.journal.ui.utils.getDateWithWeekdayText
import com.isaakhanimann.journal.ui.utils.getInstant
import com.isaakhanimann.journal.ui.utils.getMediumDateText
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

class DateFormatTest {

    private val previousOption = DateFormat.currentOption()
    private val previousLocale = Locale.getDefault()

    @Before
    fun setUp() {
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        DateFormat.setOption(previousOption)
        Locale.setDefault(previousLocale)
    }

    @Test
    fun englishWeekdayDateDoesNotZeroPadDay() {
        DateFormat.setOption(DateLocaleOption.EN_US)
        val instant = getInstant(year = 2026, month = 8, day = 8, hourOfDay = 12, minute = 0)!!
        val text = instant.getDateWithWeekdayText()
        assertFalse(text.contains("08"))
        assertTrue(text.contains("Aug") || text.contains("August"))
        assertTrue(text.contains("2026"))
        assertTrue(text.contains("Sat"))
    }

    @Test
    fun simplifiedChineseUsesYearMonthDayOrder() {
        DateFormat.setOption(DateLocaleOption.ZH_CN)
        val instant = getInstant(year = 2026, month = 8, day = 8, hourOfDay = 12, minute = 0)!!
        val text = instant.getDateWithWeekdayText()
        assertTrue(text.contains("2026"))
        assertTrue(text.contains("8"))
        assertTrue(text.contains("月") || text.matches(Regex(".*8.*")))
        assertFalse("English Sat leftover: $text", text.contains("Sat"))
    }

    @Test
    fun mediumDateFollowsPinnedLocale() {
        DateFormat.setOption(DateLocaleOption.EN_US)
        val instant = getInstant(year = 2026, month = 8, day = 8, hourOfDay = 12, minute = 0)!!
        assertEquals("8 Aug 2026", instant.getMediumDateText())
    }

    @Test
    fun followSystemUsesDefaultLocale() {
        Locale.setDefault(Locale.GERMANY)
        DateFormat.setOption(DateLocaleOption.FOLLOW_SYSTEM)
        assertEquals(Locale.GERMANY, DateFormat.locale())
    }
}
