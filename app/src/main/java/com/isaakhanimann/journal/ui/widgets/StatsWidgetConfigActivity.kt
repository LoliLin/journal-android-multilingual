package com.isaakhanimann.journal.ui.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.isaakhanimann.journal.di.JournalApplication
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.theme.JournalTheme
import kotlinx.coroutines.launch

/**
 * Per-widget configuration: pick a substance (or all) and a rolling window.
 * Reached from the widget's gear button and from the launcher configure step.
 */
class StatsWidgetConfigActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val app = application as JournalApplication
        val config = StatsWidgetData.readConfig(this, appWidgetId)

        lifecycleScope.launch {
            val names = StatsWidgetData.readConfiguredSubstanceNames(
                this@StatsWidgetConfigActivity,
                app.experienceRepository
            )
            setContent {
                JournalTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        StatsWidgetConfigContent(
                            substanceNames = names,
                            initialSubstance = config.substanceName,
                            initialDays = config.days,
                            onSave = { substanceName, days ->
                                StatsWidgetData.writeConfig(
                                    this@StatsWidgetConfigActivity,
                                    appWidgetId,
                                    StatsWidgetConfig(substanceName, days)
                                )
                                val manager =
                                    AppWidgetManager.getInstance(this@StatsWidgetConfigActivity)
                                lifecycleScope.launch {
                                    StatsWidgetData.refresh(
                                        this@StatsWidgetConfigActivity,
                                        appWidgetId,
                                        app.experienceRepository
                                    )
                                    manager.updateAppWidget(
                                        appWidgetId,
                                        StatsWidgetProvider.render(
                                            this@StatsWidgetConfigActivity,
                                            appWidgetId
                                        )
                                    )
                                }
                                setResult(
                                    Activity.RESULT_OK,
                                    Intent().putExtra(
                                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                                        appWidgetId
                                    )
                                )
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsWidgetConfigContent(
    substanceNames: List<String>,
    initialSubstance: String?,
    initialDays: Int,
    onSave: (substanceName: String?, days: Int) -> Unit
) {
    var selectedSubstance by remember { mutableStateOf(initialSubstance) }
    var selectedDays by remember { mutableStateOf(initialDays) }
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = i18n("widget_config_title"),
            style = MaterialTheme.typography.titleLarge
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(7, 30, 90).forEach { days ->
                FilterChip(
                    selected = selectedDays == days,
                    onClick = { selectedDays = days },
                    label = { Text("${days}d") }
                )
            }
        }
        Text(
            text = i18n("widget_config_substance"),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                SubstanceRow(
                    name = i18n("widget_config_all_substances"),
                    isSelected = selectedSubstance == null,
                    onClick = { selectedSubstance = null }
                )
            }
            items(substanceNames) { name ->
                SubstanceRow(
                    name = name,
                    isSelected = selectedSubstance == name,
                    onClick = { selectedSubstance = name }
                )
            }
        }
        Button(
            onClick = { onSave(selectedSubstance, selectedDays) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text(i18n("common_save"))
        }
    }
}

@Composable
private fun SubstanceRow(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
