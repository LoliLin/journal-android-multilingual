package com.isaakhanimann.journal.ui.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.isaakhanimann.journal.MainActivity
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.ui.notifications.EXTRA_NAVIGATE_TO
import com.isaakhanimann.journal.ui.notifications.NAV_ADD_INGESTION
import com.isaakhanimann.journal.ui.notifications.NAV_STATS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Classic RemoteViews widgets: tap opens the app via MainActivity's intent
 * steering (EXTRA_NAVIGATE_TO), so no custom broadcast receiver is needed.
 */
private fun pendingActivity(context: Context, navTarget: String, requestCode: Int): PendingIntent =
    PendingIntent.getActivity(
        context,
        requestCode,
        Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(EXTRA_NAVIGATE_TO, navTarget)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

class QuickAddWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_quick_add)
        views.setOnClickPendingIntent(
            R.id.widget_quick_add_root,
            pendingActivity(context, NAV_ADD_INGESTION, REQUEST_CODE_ADD)
        )
        appWidgetIds.forEach { appWidgetManager.updateAppWidget(it, views) }
    }

    companion object {
        private const val REQUEST_CODE_ADD = 1001
    }
}

class StatsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Recompute before rendering so the widget always shows current data.
                val app = context.applicationContext as? com.isaakhanimann.journal.di.JournalApplication
                if (app != null) {
                    StatsWidgetData.refresh(context, app.experienceRepository)
                }
                render(context, appWidgetManager, appWidgetIds)
            } finally {
                pending.finish()
            }
        }
    }

    /** Pushes the stored summary into the widget views. */
    fun render(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val summary = StatsWidgetData.readFromPreferences(context)
        val views = RemoteViews(context.packageName, R.layout.widget_stats).apply {
            setTextViewText(R.id.widget_stats_ingestions, summary.ingestionCount.toString())
            setTextViewText(R.id.widget_stats_experiences, summary.experienceCount.toString())
            setTextViewText(R.id.widget_stats_substances, summary.substanceCount.toString())
            setOnClickPendingIntent(
                R.id.widget_stats_root,
                pendingActivity(context, NAV_STATS, REQUEST_CODE_STATS)
            )
        }
        appWidgetIds.forEach { appWidgetManager.updateAppWidget(it, views) }
    }

    companion object {
        private const val REQUEST_CODE_STATS = 1002
    }
}
