/*
 * Fork feature: keeps the "effects in progress" notification's timeline image
 * current. Android notifications cannot host live canvases, so the picture is
 * re-rendered on triggers:
 *  1. Data events (rating / timed note / ingestion changed) - marked dirty and
 *     flushed on the next chance a view tree is available.
 *  2. Screen-on - lazy refresh if the last render is older than
 *     min(15 min, total-duration/30); zero cost while the screen is off.
 */
package com.isaakhanimann.journal.ui.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.View
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.renderTimelineBitmapForNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

object EffectNotificationRefresher {

    private const val TAG = "EffectNotifRefresh"
    private const val MIN_REFRESH_INTERVAL_MINUTES = 15L
    private const val MAX_REFRESH_INTERVAL_MINUTES = 60L
    private const val INTERVAL_DIVISOR = 30f
    /** The experience whose notification currently shows a timeline. */
    private var activeExperienceId: Int? = null
    private var activeIngestionTime: Instant? = null
    private var activeSubstanceName: String? = null
    private var activeTotalDuration: Duration? = null
    private var lastRender: Instant? = null
    private var dirty = false

    fun register(context: Context, scope: CoroutineScope) {
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(ScreenOnReceiver(), filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(ScreenOnReceiver(), filter)
        }
        // Event-driven trigger: any journal write marks the picture stale; the
        // re-render itself waits for the next available view tree (foreground).
        scope.launch {
            com.isaakhanimann.journal.data.room.experiences.JournalDataEvents
                .journalChangeSignal.collect { markDirty() }
        }
    }

    /**
     * Called whenever the notification is (re)shown with a fresh render - e.g.
     * right after an ingestion is saved.
     */
    fun onNotificationRendered(experienceId: Int, ingestionTime: Instant, substanceName: String, totalDuration: Duration?) {
        activeExperienceId = experienceId
        activeIngestionTime = ingestionTime
        activeSubstanceName = substanceName
        activeTotalDuration = totalDuration
        lastRender = Instant.now()
        dirty = false
    }

    /** Data changed (rating/note/ingestion edited); picture is stale now. */
    fun markDirty() {
        dirty = true
    }

    fun clear(experienceId: Int) {
        if (activeExperienceId == experienceId) {
            activeExperienceId = null
            activeIngestionTime = null
            activeSubstanceName = null
            activeTotalDuration = null
            lastRender = null
            dirty = false
        }
    }

    private class ScreenOnReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != Intent.ACTION_SCREEN_ON) return
            // Render needs a view tree; the activity re-attaches after screen-on,
            // so give it a moment. flushIfDue no-ops without an activity view.
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                EffectNotificationRefresher.flushIfDue(
                    context.applicationContext,
                    null,
                    force = false
                )
            }, 2000L)
        }
    }

    /**
     * Re-renders the timeline picture when due. [view] supplies the Compose
     * view tree; callers without one (screen-on broadcast) pass null and the
     * call is skipped until the app is foregrounded again.
     */
    fun flushIfDue(context: Context, view: View?, force: Boolean) {
        val experienceId = activeExperienceId ?: return
        val ingestionTime = activeIngestionTime ?: return
        val substanceName = activeSubstanceName ?: return
        if (view == null) return
        val now = Instant.now()
        val last = lastRender
        val total = activeTotalDuration
        // Refresh cadence: total effect duration / 30, clamped to [15min, 60min]
        // so short windows still update and long ones do not spam re-renders.
        val interval = total?.let {
            Duration.ofSeconds((it.seconds / INTERVAL_DIVISOR).toLong())
        } ?: Duration.ofMinutes(MIN_REFRESH_INTERVAL_MINUTES)
        val cadence = interval.coerceIn(
            Duration.ofMinutes(MIN_REFRESH_INTERVAL_MINUTES),
            Duration.ofMinutes(MAX_REFRESH_INTERVAL_MINUTES)
        )
        val due = dirty || last == null || Duration.between(last, now) >= cadence
        if (!force && !due) return
        if (Notifications.hasNotificationPermission(context).not()) return
        val app = context.applicationContext as? com.isaakhanimann.journal.di.JournalApplication ?: return
        val scope = app.applicationScope
        scope.launch {
            try {
                val repo: ExperienceRepository = app.experienceRepository
                val ingestions = repo.getIngestionsWithCompanionsFlow(experienceId).first()
                if (ingestions.isEmpty()) return@launch
                val ratings = repo.getRatingsFlow(experienceId).first()
                val timedNotes = repo.getTimedNotes(experienceId)
                val bitmap = renderTimelineBitmapForNotification(
                    context = context,
                    ingestions = ingestions,
                    ratings = ratings,
                    timedNotes = timedNotes,
                    substanceRepo = app.substanceRepo,
                    lifecycleView = view,
                    widthPx = context.resources.displayMetrics.widthPixels
                ) ?: return@launch
                lastRender = Instant.now()
                dirty = false
                Notifications.showEffectNotification(
                    context = context,
                    experienceId = experienceId,
                    substanceName = substanceName,
                    ingestionTime = ingestionTime,
                    timelineBitmap = bitmap
                )
            } catch (e: Exception) {
                Log.w(TAG, "Timeline refresh failed", e)
            }
        }
    }
}
