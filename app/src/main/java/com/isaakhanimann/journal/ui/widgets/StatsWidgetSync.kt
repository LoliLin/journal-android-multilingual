package com.isaakhanimann.journal.ui.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.JournalDataEvents
import com.isaakhanimann.journal.localization.I18n
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

private const val TAG = "StatsWidgetSync"

/** How long journal writes must settle before a refresh; absorbs imports and edit bursts. */
private const val JOURNAL_SETTLE_MS = 500L

/**
 * Single owner of the stats-widget refresh chain.
 *
 * Every reason a widget can go stale funnels into [requestRefresh]: journal writes, the app
 * language, extension-pack strings, the app returning to the foreground, and the system's own
 * update broadcast. Requests are conflated and drained by one long-lived worker, which means:
 *
 * - a burst of writes (an import, several edits) collapses into a single refresh instead of one
 *   render per row;
 * - [AppWidgetManager.updateAppWidget], `RemoteViews` building and the `I18n` /
 *   `SubstanceRepository` lookups that go with them never run on the main thread, so a refresh
 *   cannot stall recomposition or a navigation animation;
 * - nothing is queried at all while no widget is placed.
 *
 * The worker is dispatched to [Dispatchers.IO] rather than the application's main-thread scope.
 * Refreshes are serialized because a conflated [Channel] has exactly one consumer coroutine, not
 * because of the dispatcher: a coroutine is never resumed concurrently with itself.
 */
object StatsWidgetSync {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val requests = Channel<Unit>(Channel.CONFLATED)

    private val completionLock = Any()
    private val completions = mutableListOf<() -> Unit>()

    private var started = false

    /**
     * Starts the worker and wires every trigger. Called once from
     * [com.isaakhanimann.journal.di.JournalApplication]; later calls are ignored.
     */
    @OptIn(FlowPreview::class)
    fun start(
        appContext: Context,
        experienceRepository: ExperienceRepository,
        languageChanges: Flow<Unit>
    ) {
        val context = appContext.applicationContext
        synchronized(completionLock) {
            if (started) return
            started = true
        }
        scope.launch {
            for (ignored in requests) {
                // Claim the completions that belong to this pass. Anything registered while
                // the pass is running belongs to the next one, which is already queued.
                val pending = claimCompletions()
                try {
                    refreshAll(context, experienceRepository)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // A widget refresh must never take the whole app process down.
                    Log.w(TAG, "Stats widget refresh failed", e)
                }
                pending.forEach { callback ->
                    try {
                        callback()
                    } catch (e: Exception) {
                        Log.w(TAG, "Stats widget refresh completion failed", e)
                    }
                }
            }
        }
        scope.launch {
            JournalDataEvents.journalChangeSignal
                .debounce(JOURNAL_SETTLE_MS)
                .collect { requestRefresh() }
        }
        scope.launch { languageChanges.collect { requestRefresh() } }
        scope.launch { I18n.stringsChanged.collect { requestRefresh() } }
        // First pass: the process may have been started by a broadcast, or the widgets
        // may be stale from a previous run.
        requestRefresh()
    }

    /**
     * Schedules a refresh. Safe from any thread and never blocking; a refresh already queued
     * absorbs this request. [onComplete] runs after the pass that covers this request finishes,
     * which is how a broadcast's `goAsync` pending result is closed.
     */
    fun requestRefresh(onComplete: (() -> Unit)? = null) {
        if (onComplete != null) {
            synchronized(completionLock) { completions.add(onComplete) }
        }
        requests.trySend(Unit)
    }

    private fun claimCompletions(): List<() -> Unit> = synchronized(completionLock) {
        if (completions.isEmpty()) {
            emptyList()
        } else {
            completions.toList().also { completions.clear() }
        }
    }

    private suspend fun refreshAll(context: Context, experienceRepository: ExperienceRepository) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            ComponentName(context, StatsWidgetProvider::class.java)
        )
        // No widget placed: nothing to compute. This is what keeps the chain free when
        // the widget is not in use.
        if (ids.isEmpty()) return
        ids.forEach { appWidgetId ->
            StatsWidgetData.refresh(context, appWidgetId, experienceRepository)
            manager.updateAppWidget(appWidgetId, StatsWidgetProvider.render(context, appWidgetId))
        }
    }
}
