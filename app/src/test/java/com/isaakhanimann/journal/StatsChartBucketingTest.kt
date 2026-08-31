package com.isaakhanimann.journal

import com.isaakhanimann.journal.ui.tabs.stats.TimePickerOption
import com.isaakhanimann.journal.ui.tabs.stats.bucketNewestFirst
import com.isaakhanimann.journal.ui.tabs.stats.takeItemsInStatsWindow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsChartBucketingTest {
    private val nowEndOfDay: Instant = LocalDate.of(2026, 3, 10)
        .atTime(LocalTime.of(23, 59, 59))
        .toInstant(ZoneOffset.UTC)

    private fun atDay(dayOfMarch: Int, hour: Int = 12): Instant =
        LocalDate.of(2026, 3, dayOfMarch)
            .atTime(LocalTime.of(hour, 0))
            .toInstant(ZoneOffset.UTC)

    @Test
    fun twoIngestionTimesAWeekApartLandInDifferentDayBuckets() {
        val first = atDay(1)
        val second = atDay(8)
        val items = listOf(first, second).sortedByDescending { it }
        val startExclusive = nowEndOfDay.minus(TimePickerOption.DAYS_30.allBucketSizes)
        val inWindow = takeItemsInStatsWindow(items, { it }, nowEndOfDay, startExclusive)
        val buckets = bucketNewestFirst(inWindow, { it }, TimePickerOption.DAYS_30, nowEndOfDay)
        assertEquals(30, buckets.size)
        val occupied = buckets.mapIndexedNotNull { index, bucket ->
            if (bucket.isEmpty()) null else index to bucket
        }
        assertEquals(2, occupied.size)
        assertEquals(listOf(first), occupied[0].second)
        assertEquals(listOf(second), occupied[1].second)
    }

    @Test
    fun ingestionTimesSpreadAcrossBuckets() {
        val ingestionsNewestFirst = listOf(atDay(10), atDay(9), atDay(8), atDay(3))
        val startExclusive = nowEndOfDay.minus(TimePickerOption.DAYS_7.allBucketSizes)
        val inWindow = takeItemsInStatsWindow(
            ingestionsNewestFirst,
            { it },
            nowEndOfDay,
            startExclusive
        )
        val buckets = bucketNewestFirst(
            inWindow,
            { it },
            TimePickerOption.DAYS_7,
            nowEndOfDay
        )
        assertEquals(7, buckets.size)
        assertEquals(listOf(0, 0, 0, 0, 1, 1, 1), buckets.map { it.size })
    }

    @Test
    fun windowDropsFutureAndOldItems() {
        val future = nowEndOfDay.plusSeconds(60)
        val inRange = atDay(8)
        val tooOld = atDay(1)
        val startExclusive = nowEndOfDay.minus(TimePickerOption.DAYS_7.allBucketSizes)
        val inWindow = takeItemsInStatsWindow(
            listOf(future, inRange, tooOld),
            { it },
            nowEndOfDay,
            startExclusive
        )
        assertEquals(listOf(inRange), inWindow)
    }
}
