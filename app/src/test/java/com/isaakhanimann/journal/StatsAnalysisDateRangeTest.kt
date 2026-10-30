package com.isaakhanimann.journal

import com.isaakhanimann.journal.ui.tabs.stats.trailingCalendarDateRange
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsAnalysisDateRangeTest {
    @Test
    fun trailingDateRangesContainExactlyTheRequestedNumberOfDays() {
        val today = LocalDate.of(2026, 10, 30)

        for (dayCount in listOf(30L, 90L)) {
            val (start, end) = trailingCalendarDateRange(today, dayCount)

            assertEquals(today, end)
            assertEquals(dayCount, ChronoUnit.DAYS.between(start, end) + 1)
        }
    }
}
