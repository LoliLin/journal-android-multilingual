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
 * Everything one refresh pass has to cover, claimed atomically so the pending-result callbacks
 * always belong to the pass that actually ran.
 */
private class RefreshTargets(
    val allWidgets: Boolean,
    val widgetIds: List<Int>,
    val completions: List<() -> Unit>
)

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
 *
 * Pending work lives in fields rather than in the channel payload, so a request arriving while a
 * pass is running is guaranteed to be picked up by the next pass instead of depending on channel
 * buffering.
 */
object StatsWidgetSync {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val requests = Channel<Unit>(Channel.CONFLATED)

    /** Guards every field below. */
    private val lock = Any()
    private var started = false
    private var allWidgetsRequested = false
    private val explicitWidgetIds = mutableSetOf<Int>()
    private val completions = mutableListOf<() -> Unit>()

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
        synchronized(lock) {
            if (started) return
            started = true
        }
        scope.launch {
            for (ignored in requests) {
                val targets = claimTargets()
                try {
                    refreshAll(context, experienceRepository, targets)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // A widget refresh must never take the whole app process down.
                    Log.w(TAG, "Stats widget refresh failed", e)
                } finally {
                    // Must run even if the pass was cancelled: a broadcast receiver that never
                    // calls PendingResult.finish() trips the system's ANR watchdog.
                    invokeCompletions(targets.completions)
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
     * Schedules a refresh. Safe from any thread and never blocking; a pass already queued absorbs
     * this request.
     *
     * [appWidgetIds] narrows the pass to the set the system asked for in an update broadcast.
     * Passing null means "every placed widget", which is what the data, language and foreground
     * triggers mean. [onComplete] runs after the pass that covers this request finishes, which is
     * how a broadcast's `goAsync` pending result is closed.
     *
     * Calling this before [start] is fine: the request is buffered until the worker launches.
     */
    fun requestRefresh(
        appWidgetIds: IntArray? = null,
        onComplete: (() -> Unit)? = null
    ) {
        synchronized(lock) {
            if (appWidgetIds == null) {
                // Data, language and foreground changes can affect what every widget shows.
                allWidgetsRequested = true
            } else {
                explicitWidgetIds.addAll(appWidgetIds.toList())
            }
            if (onComplete != null) completions.add(onComplete)
        }
        requests.trySend(Unit)
    }

    /** Takes ownership of all pending work; anything registered after this belongs to the next pass. */
    private fun claimTargets(): RefreshTargets = synchronized(lock) {
        val claimed = RefreshTargets(
            allWidgets = allWidgetsRequested,
            widgetIds = explicitWidgetIds.toList(),
            completions = completions.toList()
        )
        allWidgetsRequested = false
        explicitWidgetIds.clear()
        completions.clear()
        claimed
    }

    private fun invokeCompletions(pending: List<() -> Unit>) {
        pending.forEach { callback ->
            try {
                callback()
            } catch (e: Exception) {
                Log.w(TAG, "Stats widget refresh completion failed", e)
            }
        }
    }

    private suspend fun refreshAll(
        context: Context,
        experienceRepository: ExperienceRepository,
        targets: RefreshTargets
    ) {
        val ids = if (targets.allWidgets) {
            placedWidgetIds(context)
        } else {
            // Prefer the ids the system handed us in the update broadcast: getAppWidgetIds can
            // still be missing a widget that was just placed, which would drop that update.
            targets.widgetIds
        }
        // No widget placed: nothing to compute. This is what keeps the chain free when the
        // widget is not in use.
        if (ids.isEmpty()) return
        val manager = AppWidgetManager.getInstance(context)
        ids.forEach { appWidgetId ->
            val summary = StatsWidgetData.refresh(context, appWidgetId, experienceRepository)
            manager.updateAppWidget(appWidgetId, StatsWidgetProvider.render(context, appWidgetId, summary))
        }
    }

    private fun placedWidgetIds(context: Context): List<Int> =
        AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, StatsWidgetProvider::class.java))
            .toList()
}
