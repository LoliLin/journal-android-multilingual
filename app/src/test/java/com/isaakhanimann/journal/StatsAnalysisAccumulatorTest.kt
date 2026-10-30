package com.isaakhanimann.journal

import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithCompanionAndCustomUnit
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.classes.roa.DoseClass
import com.isaakhanimann.journal.ui.tabs.stats.AnalysisPeriodPreset
import com.isaakhanimann.journal.ui.tabs.stats.ConsumerSelection
import com.isaakhanimann.journal.ui.tabs.stats.StatsAnalysisAccumulator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatsAnalysisAccumulatorTest {
    @Test
    fun aggregatesMoreThanOneHundredThousandRowsAcrossBoundedPages() {
        val totalRows = 100_001
        val accumulator = accumulator()
        val pageSize = 500

        for (pageStart in 0 until totalRows step pageSize) {
            val pageEnd = minOf(pageStart + pageSize, totalRows)
            val page = (pageStart until pageEnd).map { id ->
                ingestion(
                    id = id,
                    creationDate = Instant.ofEpochSecond((totalRows - id).toLong())
                )
            }
            accumulator.addPage(page)
        }

        val model = accumulator.build()
        assertEquals(totalRows, model.ingestionCount)
        assertEquals(50, model.ingestions.size)
        assertEquals(0, model.ingestions.first().ingestion.id)
        assertEquals(49, model.ingestions.last().ingestion.id)
        assertEquals(totalRows.toDouble(), model.totalDoseBySubstance.single().absoluteTotal, 0.0)
        assertEquals(totalRows, model.perSubstanceCharts.single().doseBuckets.single().second)
        assertEquals(0, model.perSubstanceCharts.single().unknownDoseCount)
    }

    @Test
    fun filtersInclusiveLocalDatesSubstanceAndOwnerWhileAggregatingRelativeDose() {
        val start = LocalDate.of(2026, 10, 1)
        val end = LocalDate.of(2026, 10, 2)
        val accumulator = accumulator(
            selectedSubstances = setOf("A"),
            consumer = ConsumerSelection.Owner,
            startDate = start,
            endDate = end,
            zone = ZoneId.of("UTC"),
            relativeDoseOf = { it.pureDose?.div(2.0) },
            doseClassOf = {
                when (it.pureDose) {
                    2.0 -> DoseClass.LIGHT
                    3.0 -> DoseClass.COMMON
                    else -> null
                }
            }
        )

        accumulator.addPage(
            listOf(
                ingestion(1, time = Instant.parse("2026-10-01T00:00:00Z"), dose = 2.0),
                ingestion(2, time = Instant.parse("2026-10-02T23:59:59Z"), dose = 3.0),
                ingestion(3, time = Instant.parse("2026-09-30T23:59:59Z"), dose = 20.0),
                ingestion(4, substanceName = "B", time = Instant.parse("2026-10-01T12:00:00Z"), dose = 10.0),
                ingestion(
                    5,
                    time = Instant.parse("2026-10-01T12:00:00Z"),
                    dose = 8.0,
                    consumerName = "Alice"
                ),
                ingestion(6, time = Instant.parse("2026-10-02T12:00:00Z"), dose = null)
            )
        )

        val model = accumulator.build()
        assertEquals(3, model.ingestionCount)
        assertEquals(listOf(1, 2, 6), model.ingestions.map { it.ingestion.id })
        assertEquals(5.0, model.totalDoseBySubstance.single().absoluteTotal, 0.0)
        assertEquals(2.5, model.totalDoseBySubstance.single().relativeTotal!!, 0.0)

        val chart = model.perSubstanceCharts.single()
        assertEquals(listOf(1.0 to 1, 1.5 to 1), chart.doseBuckets)
        assertEquals(listOf(DoseClass.LIGHT to 1, DoseClass.COMMON to 1), chart.doseClassCounts)
        assertEquals(
            listOf(start to 1.0, end to 2.5),
            chart.perDayCumulativeRelative
        )
        assertEquals(1, chart.unknownDoseCount)
        assertEquals(5.0, chart.absoluteTotal, 0.0)
        assertEquals(2.5, chart.relativeTotal!!, 0.0)
        assertEquals("mg", chart.units)
        assertNull(model.totalDoseBySubstance.single().color)
    }

    private fun accumulator(
        selectedSubstances: Set<String> = setOf("A"),
        consumer: ConsumerSelection = ConsumerSelection.All,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        zone: ZoneId = ZoneId.of("UTC"),
        relativeDoseOf: (IngestionWithCompanionAndCustomUnit) -> Double? = { null },
        doseClassOf: (IngestionWithCompanionAndCustomUnit) -> DoseClass? = { null }
    ) = StatsAnalysisAccumulator(
        selectedSubstances = selectedSubstances,
        selectedConsumer = consumer,
        selectedPreset = if (startDate == null && endDate == null) {
            AnalysisPeriodPreset.ALL_TIME
        } else {
            AnalysisPeriodPreset.CUSTOM
        },
        startDate = startDate,
        endDate = endDate,
        effectiveStart = startDate,
        effectiveEnd = endDate,
        zone = zone,
        relativeDoseOf = relativeDoseOf,
        doseClassOf = doseClassOf
    )

    private fun ingestion(
        id: Int,
        substanceName: String = "A",
        time: Instant = Instant.parse("2026-10-30T12:00:00Z"),
        creationDate: Instant = Instant.ofEpochSecond(1_000_000L - id),
        dose: Double? = 1.0,
        consumerName: String? = null
    ) = IngestionWithCompanionAndCustomUnit(
        ingestion = Ingestion(
            id = id,
            substanceName = substanceName,
            time = time,
            creationDate = creationDate,
            administrationRoute = AdministrationRoute.ORAL,
            dose = dose,
            isDoseAnEstimate = false,
            estimatedDoseStandardDeviation = null,
            units = "mg",
            experienceId = id,
            notes = null,
            stomachFullness = null,
            consumerName = consumerName,
            customUnitId = null
        ),
        substanceCompanion = null,
        customUnit = null
    )
}
