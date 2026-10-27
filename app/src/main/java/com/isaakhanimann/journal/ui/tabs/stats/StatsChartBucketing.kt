package com.isaakhanimann.journal.ui.tabs.stats

import java.time.Instant
import java.time.ZoneId

internal data class StatsWindow(
    val option: TimePickerOption,
    val zone: ZoneId,
    val startExclusive: Instant,
    val endInclusive: Instant
)

internal fun statsWindow(now: Instant, option: TimePickerOption, zone: ZoneId): StatsWindow {
    val end = now.getEndOfDay(zone)
    return StatsWindow(option, zone, statsWindowStart(end, option, zone), end)
}

internal fun statsWindowStart(
    nowEndOfDay: Instant,
    option: TimePickerOption,
    zone: ZoneId = ZoneId.systemDefault()
): Instant = nowEndOfDay.atZone(zone).toLocalDate()
    .plusDays(1).minus(option.allBucketSizes).atStartOfDay(zone).toInstant().minusNanos(1)

/**
 * Items newest-first, matching [StatsViewModel]'s experience query order.
 * Drops timestamps after [nowEndOfDay], then keeps those after [startExclusive].
 */
internal fun <T> takeItemsInStatsWindow(
    itemsNewestFirst: List<T>,
    instantOf: (T) -> Instant,
    nowEndOfDay: Instant,
    startExclusive: Instant
): List<T> = itemsNewestFirst
    .dropWhile { instantOf(it) > nowEndOfDay }
    .takeWhile { instantOf(it) > startExclusive }

/**
 * Split newest-first items into [TimePickerOption.bucketCount] buckets walking
 * backward from [nowEndOfDay]. Returns oldest-first so the bar chart reads left-to-right.
 */
internal fun <T> bucketNewestFirst(
    itemsNewestFirst: List<T>,
    instantOf: (T) -> Instant,
    option: TimePickerOption,
    nowEndOfDay: Instant,
    zone: ZoneId = ZoneId.systemDefault()
): List<List<T>> {
    var remaining = itemsNewestFirst
    val bucketsNewestFirst = ArrayList<List<T>>(option.bucketCount)
    var boundaryDate = nowEndOfDay.atZone(zone).toLocalDate().plusDays(1)
    repeat(option.bucketCount) {
        boundaryDate = boundaryDate.minus(option.oneBucketSize)
        val startInstant = boundaryDate.atStartOfDay(zone).toInstant().minusNanos(1)
        val forBucket = remaining.takeWhile { instantOf(it) > startInstant }
        bucketsNewestFirst.add(forBucket)
        remaining = remaining.drop(forBucket.size)
    }
    return bucketsNewestFirst.asReversed()
}
