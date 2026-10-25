package com.isaakhanimann.journal.ui.tabs.journal.experience.components.ingestion

import com.isaakhanimann.journal.ui.tabs.journal.experience.components.TimeDisplayOption
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IngestionTimeBetweenTest {
    private val first = Instant.parse("2026-10-25T08:00:00Z")

    @Test
    fun firstIngestionExplicitlyUsesRegularClockTime() {
        assertNull(getTimeBetweenIngestionsText(null, first))
        assertEquals(
            TimeDisplayOption.REGULAR,
            resolveIngestionTimeDisplayOption(TimeDisplayOption.TIME_BETWEEN, null)
        )
    }

    @Test
    fun displayedNeighborsUseImmediatelyPreviousIngestion() {
        val displayed = listOf(first, first.plusSeconds(30 * 60), first.plusSeconds(75 * 60))
        val intervals = displayed.mapIndexed { index, time ->
            getTimeBetweenIngestionsText(displayed.getOrNull(index - 1), time)
        }

        assertEquals(listOf(null, "+ 30 min", "+ 45 min"), intervals)
    }

    @Test
    fun eachConsumerUsesItsOwnDisplayedNeighbors() {
        val myDisplayed = listOf(first, first.plusSeconds(60 * 60))
        val otherDisplayed = listOf(first.plusSeconds(15 * 60), first.plusSeconds(45 * 60))

        assertEquals(
            "+ 1 hr",
            getTimeBetweenIngestionsText(myDisplayed[0], myDisplayed[1])
        )
        assertNull(getTimeBetweenIngestionsText(otherDisplayed.getOrNull(-1), otherDisplayed[0]))
        assertEquals(
            "+ 30 min",
            getTimeBetweenIngestionsText(otherDisplayed[0], otherDisplayed[1])
        )
    }

    @Test
    fun timeDisplaySelectionPreservesAllOtherModes() {
        for (option in TimeDisplayOption.entries.filter { it != TimeDisplayOption.TIME_BETWEEN }) {
            assertEquals(option, resolveIngestionTimeDisplayOption(option, null))
        }
        assertEquals(
            TimeDisplayOption.TIME_BETWEEN,
            resolveIngestionTimeDisplayOption(TimeDisplayOption.TIME_BETWEEN, "+ 30 min")
        )
    }

    @Test
    fun unsortedTimesFallBackToRegularClockTimeInsteadOfFalsePositiveElapsedTime() {
        val earlier = first.minusSeconds(5 * 60)
        val interval = getTimeBetweenIngestionsText(first, earlier)

        assertNull(interval)
        assertEquals(
            TimeDisplayOption.REGULAR,
            resolveIngestionTimeDisplayOption(TimeDisplayOption.TIME_BETWEEN, interval)
        )
    }

    @Test
    fun elapsedTimeAcrossMidnightIsNotClockTime() {
        val before = Instant.parse("2026-10-25T23:45:00Z")
        val after = Instant.parse("2026-10-26T00:15:00Z")

        assertEquals("+ 30 min", getTimeBetweenIngestionsText(before, after))
    }

    @Test
    fun simultaneousIngestionsShowZeroInterval() {
        assertEquals("+ 0 s", getTimeBetweenIngestionsText(first, first))
    }
}
