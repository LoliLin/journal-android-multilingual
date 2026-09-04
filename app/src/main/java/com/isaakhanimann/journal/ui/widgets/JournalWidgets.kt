package com.isaakhanimann.journal.ui.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.isaakhanimann.journal.MainActivity
import com.isaakhanimann.journal.di.JournalApplication
import com.isaakhanimann.journal.ui.notifications.EXTRA_NAVIGATE_TO
import com.isaakhanimann.journal.ui.notifications.NAV_ADD_INGESTION
import com.isaakhanimann.journal.ui.notifications.NAV_STATS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private fun addIngestionIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        putExtra(EXTRA_NAVIGATE_TO, NAV_ADD_INGESTION)
    }

private fun statsIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        putExtra(EXTRA_NAVIGATE_TO, NAV_STATS)
    }

/** Home-screen shortcut that opens the add-ingestion flow. */
class QuickAddWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Content() }
    }

    @Composable
    private fun Content() {
        val context = androidx.compose.ui.platform.LocalContext.current
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(16.dp)
                .background(ColorProvider(Color(0xFF1B5E20)))
                .clickable(actionStartActivity(addIngestionIntent(context))),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = GlanceModifier.padding(8.dp),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text(
                    text = "+",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Add",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

class QuickAddWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickAddWidget()
}

/** Summary shown by [StatsWidget]: counts over the rolling window. */
data class StatsWidgetSummary(
    val ingestionCount: Int,
    val experienceCount: Int,
    val substanceCount: Int
)

/** Home-screen widget with recent ingestion statistics; tap opens the Stats tab. */
class StatsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Content() }
    }

    @Composable
    private fun Content() {
        val context = androidx.compose.ui.platform.LocalContext.current
        val summary = StatsWidgetData.readFromPreferences(context)
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(16.dp)
                .background(ColorProvider(Color(0xFF263238)))
                .clickable(actionStartActivity(statsIntent(context)))
                .padding(10.dp)
        ) {
            Text(
                text = "Journal",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.glance.text.TextAlign.Center
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )
            StatCell(value = summary.ingestionCount, label = "ingestions")
            StatCell(value = summary.experienceCount, label = "experiences")
            StatCell(value = summary.substanceCount, label = "substances")
        }
    }
}

@Composable
private fun StatCell(value: Int, label: String) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(top = 6.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = value.toString(),
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = "  $label",
            style = TextStyle(
                color = ColorProvider(Color(0xFFB0BEC5)),
                fontSize = 12.sp
            )
        )
    }
}

class StatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StatsWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as? JournalApplication
                if (app != null) {
                    StatsWidgetData.refresh(context, app.experienceRepository)
                    glanceAppWidget.updateAll(context)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
