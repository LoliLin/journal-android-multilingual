package com.isaakhanimann.journal.ui.widgets

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.statsDataStore by preferencesDataStore(name = "widget_stats")

private object Keys {
    val ingestionCount = intPreferencesKey("ingestion_count")
    val experienceCount = intPreferencesKey("experience_count")
    val substanceCount = intPreferencesKey("substance_count")
}

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
        context.statsDataStore.edit { prefs ->
            prefs[Keys.ingestionCount] = summary.ingestionCount
            prefs[Keys.experienceCount] = summary.experienceCount
            prefs[Keys.substanceCount] = summary.substanceCount
        }
    }

    suspend fun read(context: Context): StatsWidgetSummary {
        val prefs = context.statsDataStore.data.first()
        return StatsWidgetSummary(
            ingestionCount = prefs[Keys.ingestionCount] ?: 0,
            experienceCount = prefs[Keys.experienceCount] ?: 0,
            substanceCount = prefs[Keys.substanceCount] ?: 0
        )
    }

    /** Synchronous read for the Glance composable (runs on a worker thread). */
    fun readFromPreferences(context: Context): StatsWidgetSummary =
        runBlocking { read(context) }
}
