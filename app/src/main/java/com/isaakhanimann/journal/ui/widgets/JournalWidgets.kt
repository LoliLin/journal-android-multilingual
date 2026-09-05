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

/**
 * Classic RemoteViews widgets (no Glance): tap opens the app via MainActivity's
 * intent steering (EXTRA_NAVIGATE_TO), so no custom broadcast receiver is needed.
 */
private fun openAppIntent(context: Context, navTarget: String): Intent =
    Intent(context, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        putExtra(EXTRA_NAVIGATE_TO, navTarget)
    }

private fun pendingActivity(context: Context, navTarget: String, requestCode: Int): PendingIntent =
    PendingIntent.getActivity(
        context,
        requestCode,
        openAppIntent(context, navTarget),
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
        val summary = StatsWidgetData.readFromPreferences(context)
        val views = RemoteViews(context.packageName, R.layout.widget_stats)
        views.setTextViewText(
            R.id.widget_stats_ingestions,
            "${summary.ingestionCount}  ingestions"
        )
        views.setTextViewText(
            R.id.widget_stats_experiences,
            "${summary.experienceCount}  experiences"
        )
        views.setTextViewText(
            R.id.widget_stats_substances,
            "${summary.substanceCount}  substances"
        )
        views.setOnClickPendingIntent(
            R.id.widget_stats_root,
            pendingActivity(context, NAV_STATS, REQUEST_CODE_STATS)
        )
        appWidgetIds.forEach { appWidgetManager.updateAppWidget(it, views) }
    }

    companion object {
        private const val REQUEST_CODE_STATS = 1002
    }
}
