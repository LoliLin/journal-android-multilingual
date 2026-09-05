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

private fun pendingConfigure(context: Context, appWidgetId: Int): PendingIntent =
    PendingIntent.getActivity(
        context,
        appWidgetId,
        Intent(context, StatsWidgetConfigActivity::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

class StatsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as? com.isaakhanimann.journal.di.JournalApplication
                appWidgetIds.forEach { appWidgetId ->
                    if (app != null) {
                        StatsWidgetData.refresh(context, appWidgetId, app.experienceRepository)
                    }
                    appWidgetManager.updateAppWidget(appWidgetId, render(context, appWidgetId))
                }
            } finally {
                pending.finish()
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        StatsWidgetData.deleteConfig(context, appWidgetIds)
    }

    companion object {
        fun render(context: Context, appWidgetId: Int): RemoteViews {
            val summary = StatsWidgetData.readSummary(context, appWidgetId)
            val title = if (summary.substanceName != null) {
                "${summary.substanceName} · ${summary.days}d"
            } else {
                "All · ${summary.days}d"
            }
            return RemoteViews(context.packageName, R.layout.widget_stats).apply {
                setTextViewText(R.id.widget_stats_title, title)
                setTextViewText(R.id.widget_stats_ingestions, summary.ingestionCount.toString())
                setTextViewText(R.id.widget_stats_experiences, summary.experienceCount.toString())
                setTextViewText(R.id.widget_stats_substances, summary.substanceCount.toString())
                setOnClickPendingIntent(
                    R.id.widget_stats_root,
                    pendingActivity(context, NAV_STATS, appWidgetId)
                )
                setOnClickPendingIntent(
                    R.id.widget_stats_add,
                    pendingActivity(context, NAV_ADD_INGESTION, 100_000 + appWidgetId)
                )
                setOnClickPendingIntent(
                    R.id.widget_stats_settings,
                    pendingConfigure(context, appWidgetId)
                )
            }
        }
    }
}
