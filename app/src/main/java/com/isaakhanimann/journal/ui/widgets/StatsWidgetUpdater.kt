package com.isaakhanimann.journal.ui.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

/**
 * Pushes fresh summaries into every placed stats widget. Called on data
 * changes (ingestion table), app-language changes, and app start, so the
 * desktop never waits for the hourly updatePeriod.
 */
object StatsWidgetUpdater {

    suspend fun refreshAll(context: Context, experienceRepository: ExperienceRepository) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            ComponentName(context, StatsWidgetProvider::class.java)
        )
        ids.forEach { id ->
            StatsWidgetData.refresh(context, id, experienceRepository)
            manager.updateAppWidget(id, StatsWidgetProvider.render(context, id))
        }
    }

    /**
     * Recomputes whenever the ingestion table changes (insert/edit/delete).
     * Debounced so an import or a burst of edits triggers one refresh.
     */
    @OptIn(FlowPreview::class)
    fun observeDataChanges(
        context: Context,
        experienceRepository: ExperienceRepository,
        scope: CoroutineScope
    ) {
        scope.launch {
            experienceRepository.getSortedIngestionsFlow()
                .debounce(500)
                .collect {
                    try {
                        refreshAll(context, experienceRepository)
                    } catch (_: Exception) {
                        // Widget refresh must never crash the app process.
                    }
                }
        }
    }
}
