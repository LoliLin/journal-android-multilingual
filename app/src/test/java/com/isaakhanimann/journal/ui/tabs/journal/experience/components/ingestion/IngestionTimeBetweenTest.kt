package com.isaakhanimann.journal.ui.tabs.journal.experience.components.ingestion

import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithCompanionAndCustomUnit
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.ui.tabs.journal.experience.models.IngestionElement
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IngestionTimeBetweenTest {
    private fun ingestionElement(time: String) = IngestionElement(
        ingestionWithCompanionAndCustomUnit = IngestionWithCompanionAndCustomUnit(
            ingestion = Ingestion(
                substanceName = "Test",
                time = Instant.parse(time),
                administrationRoute = AdministrationRoute.ORAL,
                dose = null,
                isDoseAnEstimate = false,
                estimatedDoseStandardDeviation = null,
                units = null,
                experienceId = 1,
                notes = null,
                stomachFullness = null,
                consumerName = null,
                customUnitId = null
            ),
            substanceCompanion = null,
            customUnit = null
        ),
        roaDuration = null,
        numDots = null
    )

    @Test
    fun firstIngestionKeepsAbsoluteClockTime() {
        val records = listOf(ingestionElement("2026-10-25T08:00:00Z"))
        val previous = getPreviousIngestionTime(records, 0)

        assertNull(previous)
        assertNull(getTimeBetweenIngestionsText(previous, records.first().ingestionWithCompanionAndCustomUnit.ingestion.time))
    }

    @Test
    fun eachIngestionUsesImmediatelyPreviousIngestion() {
        val records = listOf(
            ingestionElement("2026-10-25T08:00:00Z"),
            ingestionElement("2026-10-25T08:30:00Z"),
            ingestionElement("2026-10-25T09:15:00Z")
        )

        assertEquals(
            "+ 30 min",
            getTimeBetweenIngestionsText(
                getPreviousIngestionTime(records, 1),
                records[1].ingestionWithCompanionAndCustomUnit.ingestion.time
            )
        )
        assertEquals(
            "+ 45 min",
            getTimeBetweenIngestionsText(
                getPreviousIngestionTime(records, 2),
                records[2].ingestionWithCompanionAndCustomUnit.ingestion.time
            )
        )
    }

    @Test
    fun timeBetweenDoesNotCrossConsumerGroups() {
        val myRecords = listOf(
            ingestionElement("2026-10-25T08:00:00Z"),
            ingestionElement("2026-10-25T09:00:00Z")
        )
        val otherConsumerRecords = listOf(
            ingestionElement("2026-10-25T08:15:00Z"),
            ingestionElement("2026-10-25T08:45:00Z")
        )

        assertEquals(
            Instant.parse("2026-10-25T08:00:00Z"),
            getPreviousIngestionTime(myRecords, 1)
        )
        assertNull(getPreviousIngestionTime(otherConsumerRecords, 0))
        assertEquals(
            "+ 30 min",
            getTimeBetweenIngestionsText(
                getPreviousIngestionTime(otherConsumerRecords, 1),
                otherConsumerRecords[1].ingestionWithCompanionAndCustomUnit.ingestion.time
            )
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
        val time = Instant.parse("2026-10-25T08:00:00Z")

        assertEquals("+ 0 s", getTimeBetweenIngestionsText(time, time))
    }
}
