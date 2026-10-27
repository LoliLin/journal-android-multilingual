package com.isaakhanimann.journal

import com.isaakhanimann.journal.ui.tabs.stats.TimePickerOption
import com.isaakhanimann.journal.ui.tabs.stats.bucketNewestFirst
import com.isaakhanimann.journal.ui.tabs.stats.getEndOfDay
import com.isaakhanimann.journal.ui.tabs.stats.statsWindow
import com.isaakhanimann.journal.ui.tabs.stats.statsWindowStart
import com.isaakhanimann.journal.ui.tabs.stats.takeItemsInStatsWindow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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
        val buckets = bucketNewestFirst(inWindow, { it }, TimePickerOption.DAYS_30, nowEndOfDay, ZoneOffset.UTC)
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
            nowEndOfDay,
            ZoneOffset.UTC
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

    @Test
    fun bucketsAreOldestFirstAfterReverse() {
        val newest = atDay(10)
        val oldest = atDay(8)
        val buckets = bucketNewestFirst(
            listOf(newest, oldest),
            { it },
            TimePickerOption.DAYS_7,
            nowEndOfDay,
            ZoneOffset.UTC
        )
        assertEquals(7, buckets.size)
        val occupied = buckets.filter { it.isNotEmpty() }
        assertEquals(listOf(oldest), occupied.first())
        assertEquals(listOf(newest), occupied.last())
    }

    @Test
    fun localDayIncludesLastNanosecondAndExcludesNextMidnight() {
        val zone = ZoneId.of("Asia/Shanghai")
        val end = Instant.parse("2026-03-10T02:00:00Z").getEndOfDay(zone)
        assertEquals(Instant.parse("2026-03-10T15:59:59.999999999Z"), end)
        val start = statsWindowStart(end, TimePickerOption.DAYS_7, zone)
        val first = Instant.parse("2026-03-03T16:00:00Z")
        val items = listOf(end.plusNanos(1), end, first, first.minusNanos(1))
        val inWindow = takeItemsInStatsWindow(items, { it }, end, start)
        assertEquals(listOf(end, first), inWindow)
        val buckets = bucketNewestFirst(inWindow, { it }, TimePickerOption.DAYS_7, end, zone)
        assertEquals(listOf(first), buckets.first())
        assertEquals(listOf(end), buckets.last())
    }

    @Test
    fun localBucketsFollowBothDstTransitionsForEveryOption() {
        val zone = ZoneId.of("America/New_York")
        for (date in listOf(LocalDate.of(2026, 3, 9), LocalDate.of(2026, 11, 2))) {
            val end = date.atStartOfDay(zone).toInstant().getEndOfDay(zone)
            for (option in TimePickerOption.values()) {
                val firstDate = date.plusDays(1).minus(option.allBucketSizes)
                val first = firstDate.atStartOfDay(zone).toInstant()
                val yesterday = date.minusDays(1).atStartOfDay(zone).toInstant()
                val items = listOf(end, yesterday, first).distinct().sortedDescending()
                val start = statsWindowStart(end, option, zone)
                assertEquals(first.minusNanos(1), start)
                val inWindow = takeItemsInStatsWindow(items, { it }, end, start)
                val buckets = bucketNewestFirst(inWindow, { it }, option, end, zone)
                assertEquals(option.bucketCount, buckets.size)
                assertEquals(items.size, buckets.sumOf { it.size })
                assertEquals(true, buckets.first().contains(first))
                assertEquals(true, buckets.last().contains(end))
                assertEquals(true, buckets.last().contains(yesterday) == (option != TimePickerOption.DAYS_7 && option != TimePickerOption.DAYS_30))
            }
        }
    }

    @Test
    fun windowMovesTogetherAcrossMidnightAndZoneChanges() {
        val before = Instant.parse("2026-03-10T23:59:59Z")
        val after = before.plusSeconds(1)
        for (option in TimePickerOption.values()) {
            val old = statsWindow(before, option, ZoneOffset.UTC)
            val refreshed = statsWindow(after, option, ZoneOffset.UTC)
            val changedZone = statsWindow(before, option, ZoneId.of("Asia/Shanghai"))
            assertEquals(old.endInclusive.plusSeconds(86400), refreshed.endInclusive)
            assertEquals(old.startExclusive.plusSeconds(86400), refreshed.startExclusive)
            assertEquals(refreshed.startExclusive, changedZone.startExclusive.plusSeconds(8 * 3600))
            assertEquals(refreshed.endInclusive, changedZone.endInclusive.plusSeconds(8 * 3600))
            val first = refreshed.startExclusive.plusNanos(1)
            val items = listOf(refreshed.endInclusive, first, first.minusNanos(1))
            val inWindow = takeItemsInStatsWindow(items, { it }, refreshed.endInclusive, refreshed.startExclusive)
            val buckets = bucketNewestFirst(inWindow, { it }, option, refreshed.endInclusive, refreshed.zone)
            assertEquals(inWindow, buckets.flatten().sortedDescending())
            assertEquals(first.atZone(refreshed.zone).toLocalDate(),
                refreshed.endInclusive.atZone(refreshed.zone).toLocalDate().plusDays(1).minus(option.allBucketSizes))
        }
    }

    @Test
    fun emptyNewestFirstListYieldsEmptyBuckets() {
        val buckets = bucketNewestFirst(
            emptyList<Instant>(),
            { it },
            TimePickerOption.DAYS_7,
            nowEndOfDay,
            ZoneOffset.UTC
        )
        assertEquals(7, buckets.size)
        assertEquals(true, buckets.all { it.isEmpty() })
    }
}
