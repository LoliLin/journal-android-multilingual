package com.isaakhanimann.journal.ui.tabs.stats

import java.time.Instant

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
    nowEndOfDay: Instant
): List<List<T>> {
    var remaining = itemsNewestFirst
    val bucketsNewestFirst = ArrayList<List<T>>(option.bucketCount)
    var startInstant = nowEndOfDay
    repeat(option.bucketCount) {
        startInstant = startInstant.minus(option.oneBucketSize)
        val forBucket = remaining.takeWhile { instantOf(it) > startInstant }
        bucketsNewestFirst.add(forBucket)
        remaining = remaining.drop(forBucket.size)
    }
    return bucketsNewestFirst.asReversed()
}
