package com.isaakhanimann.journal.ui.tabs.stats

import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithCompanionAndCustomUnit
import com.isaakhanimann.journal.data.substances.classes.roa.DoseClass
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.toReadableString
import java.time.LocalDate
import java.time.ZoneId
import java.util.TreeMap
import kotlin.math.floor

/** Incrementally builds the same analysis model without retaining every matching ingestion. */
internal class StatsAnalysisAccumulator(
    private val selectedSubstances: Set<String>,
    private val selectedConsumer: ConsumerSelection,
    private val selectedPreset: AnalysisPeriodPreset,
    private val startDate: LocalDate?,
    private val endDate: LocalDate?,
    private val effectiveStart: LocalDate?,
    private val effectiveEnd: LocalDate?,
    private val zone: ZoneId,
    private val relativeDoseOf: (IngestionWithCompanionAndCustomUnit) -> Double?,
    private val doseClassOf: (IngestionWithCompanionAndCustomUnit) -> DoseClass?
) {
    private data class TotalKey(val substanceName: String, val units: String)

    private class TotalAggregate(first: IngestionWithCompanionAndCustomUnit) {
        val color: AdaptiveColor? = first.substanceCompanion?.color
        val customColorArgb: Int? = first.substanceCompanion?.customColorArgb
        var absoluteTotal: Double = 0.0
        var relativeTotal: Double = 0.0
        var hasRelativeData: Boolean = false
    }

    private class SubstanceAggregate(first: IngestionWithCompanionAndCustomUnit) {
        val color: AdaptiveColor? = first.substanceCompanion?.color
        val customColorArgb: Int? = first.substanceCompanion?.customColorArgb
        val relativeBuckets = mutableMapOf<Double, Int>()
        val absoluteBuckets = mutableMapOf<Double, Int>()
        val doseClassCounts = mutableMapOf<DoseClass, Int>()
        val relativeByDay = TreeMap<LocalDate, Double>()
        var absoluteTotal: Double = 0.0
        var relativeTotal: Double = 0.0
        var hasRelativeData: Boolean = false
        var units: String? = null
        var unknownDoseCount: Int = 0
    }

    private val totalAggregates = linkedMapOf<TotalKey, TotalAggregate>()
    private val substanceAggregates = linkedMapOf<String, SubstanceAggregate>()
    private val preview = ArrayList<IngestionWithCompanionAndCustomUnit>(PREVIEW_LIMIT)

    var ingestionCount: Int = 0
        private set

    fun addPage(page: List<IngestionWithCompanionAndCustomUnit>) {
        page.forEach(::add)
    }

    private fun add(item: IngestionWithCompanionAndCustomUnit) {
        val ingestion = item.ingestion
        if (ingestion.substanceName !in selectedSubstances) return
        if (!matchesConsumer(ingestion.consumerName)) return

        val date = ingestion.time.atZone(zone).toLocalDate()
        if (effectiveStart != null && date.isBefore(effectiveStart)) return
        if (effectiveEnd != null && date.isAfter(effectiveEnd)) return

        ingestionCount += 1
        if (preview.size < PREVIEW_LIMIT) preview += item

        val pureDose = item.pureDose
        val relativeDose = relativeDoseOf(item)
        val totalKey = TotalKey(ingestion.substanceName, ingestion.units ?: "")
        val total = totalAggregates.getOrPut(totalKey) { TotalAggregate(item) }
        total.absoluteTotal += pureDose ?: 0.0
        if (relativeDose != null) {
            total.relativeTotal += relativeDose
            total.hasRelativeData = true
        }

        val substance = substanceAggregates.getOrPut(ingestion.substanceName) {
            SubstanceAggregate(item)
        }
        substance.absoluteTotal += pureDose ?: 0.0
        if (pureDose == null) {
            substance.unknownDoseCount += 1
        } else {
            increment(substance.absoluteBuckets, bucket(pureDose))
        }
        if (substance.units == null) substance.units = item.originalUnit
        doseClassOf(item)?.let { increment(substance.doseClassCounts, it) }
        if (relativeDose != null) {
            increment(substance.relativeBuckets, bucket(relativeDose))
            substance.relativeTotal += relativeDose
            substance.hasRelativeData = true
            substance.relativeByDay[date] = (substance.relativeByDay[date] ?: 0.0) + relativeDose
        }
    }

    private fun matchesConsumer(consumerName: String?): Boolean = when (selectedConsumer) {
        ConsumerSelection.All -> true
        ConsumerSelection.Owner -> consumerName == null
        is ConsumerSelection.Specific -> consumerName == selectedConsumer.name
    }

    fun build(): StatsAnalysisModel = StatsAnalysisModel(
        selectedSubstances = selectedSubstances,
        selectedConsumer = selectedConsumer,
        selectedPreset = selectedPreset,
        startDate = startDate,
        endDate = endDate,
        effectiveStart = effectiveStart,
        effectiveEnd = effectiveEnd,
        ingestionCount = ingestionCount,
        totalDoseBySubstance = totalAggregates.map { (key, value) ->
            TotalDoseLine(
                substanceName = key.substanceName,
                units = key.units,
                color = value.color,
                customColorArgb = value.customColorArgb,
                absoluteTotal = value.absoluteTotal,
                relativeTotal = value.relativeTotal.takeIf { value.hasRelativeData }
            )
        }.sortedBy { it.substanceName },
        perSubstanceCharts = substanceAggregates.map { (name, value) ->
            val doseBuckets = (if (value.hasRelativeData) {
                value.relativeBuckets
            } else {
                value.absoluteBuckets
            }).map { (dose, count) -> dose to count }.sortedBy { it.first }
            var runningTotal = 0.0
            val perDayCumulativeRelative = value.relativeByDay.map { (date, relativeDose) ->
                runningTotal += relativeDose
                date to runningTotal
            }
            SubstanceChartData(
                substanceName = name,
                color = value.color,
                customColorArgb = value.customColorArgb,
                doseBuckets = doseBuckets,
                bucketLabel = if (value.hasRelativeData) {
                    { dose -> "${dose.toReadableString()}×" }
                } else {
                    { dose -> dose.toReadableString() }
                },
                doseClassCounts = value.doseClassCounts.map { (doseClass, count) ->
                    doseClass to count
                }.sortedBy { it.first.ordinal },
                perDayCumulativeRelative = perDayCumulativeRelative,
                absoluteTotal = value.absoluteTotal,
                relativeTotal = value.relativeTotal.takeIf { value.hasRelativeData },
                units = value.units,
                unknownDoseCount = value.unknownDoseCount
            )
        }.sortedBy { it.substanceName },
        ingestions = preview.toList()
    )

    private fun bucket(dose: Double): Double = floor(dose * 2) / 2

    private fun <K> increment(counts: MutableMap<K, Int>, key: K) {
        counts[key] = (counts[key] ?: 0) + 1
    }

    private companion object {
        const val PREVIEW_LIMIT = 50
    }
}
