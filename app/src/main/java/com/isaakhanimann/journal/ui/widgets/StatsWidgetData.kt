package com.isaakhanimann.journal.ui.widgets

import android.content.Context
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

private const val PREFS_NAME = "widget_stats"
private const val KEY_PREFIX = "summary_"

/** Widget configuration, one entry per placed widget. */
data class StatsWidgetConfig(
    /** Null = all substances. */
    val substanceName: String?,
    val days: Int
)

private fun configKey(appWidgetId: Int, suffix: String) = "${KEY_PREFIX}${appWidgetId}_$suffix"

/** Cached numbers one widget renders. */
data class StatsWidgetSummary(
    val substanceName: String?,
    val days: Int,
    val ingestionCount: Int,
    val experienceCount: Int,
    val substanceCount: Int
)

/** Per-widget configuration and the cached numbers it renders. */
object StatsWidgetData {

    fun readConfig(context: Context, appWidgetId: Int): StatsWidgetConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return StatsWidgetConfig(
            substanceName = prefs.getString(configKey(appWidgetId, "substance"), null),
            days = prefs.getInt(configKey(appWidgetId, "days"), 30)
        )
    }

    fun writeConfig(context: Context, appWidgetId: Int, config: StatsWidgetConfig) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(configKey(appWidgetId, "substance"), config.substanceName)
            .putInt(configKey(appWidgetId, "days"), config.days)
            .apply()
    }

    suspend fun readConfiguredSubstanceNames(
        context: Context,
        experienceRepository: ExperienceRepository
    ): List<String> =
        experienceRepository.getAllSubstanceCompanions()
            .map { it.substanceName }
            .sorted()

    /** Recomputes the summary for one widget and stores it. */

    fun deleteConfig(context: Context, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().also { editor ->
            appWidgetIds.forEach { id ->
                listOf("substance", "days", "sum_ingestions", "sum_experiences", "sum_substances")
                    .forEach { suffix -> editor.remove(configKey(id, suffix)) }
            }
        }.apply()
    }
    suspend fun refresh(
        context: Context,
        appWidgetId: Int,
        experienceRepository: ExperienceRepository
    ) {
        val config = readConfig(context, appWidgetId)
        val from = Instant.now().minus(config.days.toLong(), ChronoUnit.DAYS)
        val ingestions =
            experienceRepository.getIngestionsWithCompanions(from, Instant.now())
        val filtered = if (config.substanceName == null) {
            ingestions
        } else {
            ingestions.filter { it.ingestion.substanceName == config.substanceName }
        }
        val summary = StatsWidgetSummary(
            substanceName = config.substanceName,
            days = config.days,
            ingestionCount = filtered.size,
            experienceCount = filtered.map { it.ingestion.experienceId }.distinct().size,
            substanceCount = filtered.map { it.ingestion.substanceName }.distinct().size
        )
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(configKey(appWidgetId, "sum_ingestions"), summary.ingestionCount)
            .putInt(configKey(appWidgetId, "sum_experiences"), summary.experienceCount)
            .putInt(configKey(appWidgetId, "sum_substances"), summary.substanceCount)
            .apply()
    }

    fun readSummary(context: Context, appWidgetId: Int): StatsWidgetSummary {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return StatsWidgetSummary(
            substanceName = prefs.getString(configKey(appWidgetId, "substance"), null),
            days = prefs.getInt(configKey(appWidgetId, "days"), 30),
            ingestionCount = prefs.getInt(configKey(appWidgetId, "sum_ingestions"), 0),
            experienceCount = prefs.getInt(configKey(appWidgetId, "sum_experiences"), 0),
            substanceCount = prefs.getInt(configKey(appWidgetId, "sum_substances"), 0)
        )
    }
}
