package com.isaakhanimann.journal.ui.widgets

import android.content.Context
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

private const val PREFS_NAME = "widget_stats"
private const val KEY_INGESTION = "ingestion_count"
private const val KEY_EXPERIENCE = "experience_count"
private const val KEY_SUBSTANCE = "substance_count"

/** Summary shown by [StatsWidgetProvider]: counts over the rolling window. */
data class StatsWidgetSummary(
    val ingestionCount: Int,
    val experienceCount: Int,
    val substanceCount: Int
)

/** Rolling 30-day summary persisted for the widget composable. */
object StatsWidgetData {

    suspend fun refresh(context: Context, experienceRepository: ExperienceRepository) {
        val from = Instant.now().minus(30, ChronoUnit.DAYS)
        val ingestions =
            experienceRepository.getIngestionsWithCompanions(from, Instant.now())
        val summary = StatsWidgetSummary(
            ingestionCount = ingestions.size,
            experienceCount = ingestions.map { it.ingestion.experienceId }.distinct().size,
            substanceCount = ingestions.map { it.ingestion.substanceName }.distinct().size
        )
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_INGESTION, summary.ingestionCount)
            .putInt(KEY_EXPERIENCE, summary.experienceCount)
            .putInt(KEY_SUBSTANCE, summary.substanceCount)
            .apply()
    }

    /** Synchronous read for RemoteViews; safe on any thread. */
    fun readFromPreferences(context: Context): StatsWidgetSummary {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return StatsWidgetSummary(
            ingestionCount = prefs.getInt(KEY_INGESTION, 0),
            experienceCount = prefs.getInt(KEY_EXPERIENCE, 0),
            substanceCount = prefs.getInt(KEY_SUBSTANCE, 0)
        )
    }
}
