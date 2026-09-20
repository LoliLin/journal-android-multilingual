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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import java.text.Collator
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            val sortedSubstances: List<Pair<String, String>> = withContext(Dispatchers.Default) {
                val names = StatsWidgetData.readConfiguredSubstanceNames(
                    this@StatsWidgetConfigActivity,
                    app.experienceRepository
                )
                // Localized display names, sorted naturally according to current locale (pinyin/alphabetical).
                val collator = Collator.getInstance(Locale.getDefault())
                names.map { name ->
                    name to app.substanceRepo.getDisplayName(name)
                }.sortedWith { a, b -> collator.compare(a.second, b.second) }
            }

            setContent {
                JournalTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        StatsWidgetConfigContent(
                            substances = sortedSubstances,
                            initialSubstance = config.substanceName,
                            initialDays = config.days,
                            onSave = { substanceName, days ->
                                StatsWidgetData.writeConfig(
                                    this@StatsWidgetConfigActivity,
                                    appWidgetId,
                                    StatsWidgetConfig(substanceName, days)
                                )
                                // Refresh this widget specifically.
                                StatsWidgetSync.requestRefresh(appWidgetIds = intArrayOf(appWidgetId))
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
    substances: List<Pair<String, String>>,
    initialSubstance: String?,
    initialDays: Int,
    onSave: (substanceName: String?, days: Int) -> Unit
) {
    var selectedSubstance by remember { mutableStateOf(initialSubstance) }
    var selectedDays by remember { mutableStateOf(initialDays) }
    var searchText by remember { mutableStateOf("") }
    val allSubstancesLabel = i18n("widget_config_all_substances")

    val filteredSubstances = remember(substances, searchText) {
        if (searchText.isBlank()) {
            substances
        } else {
            substances.filter { (name, displayName) ->
                displayName.contains(searchText, ignoreCase = true) ||
                    name.contains(searchText, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = i18n("widget_config_title"),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            // Period selection first, directly above "All substances", so it is
            // always visible without scrolling.
            item(key = "period_selection") {
                Text(
                    text = i18n("widget_config_period"),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
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
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (substances.size > 5) {
                item(key = "search_field") {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        placeholder = { Text(i18n("search_substances_placeholder")) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = i18n("common_search")
                            )
                        },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { searchText = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = i18n("common_close")
                                    )
                                }
                            }
                        },
                        singleLine = true
                    )
                }
            }

            if (searchText.isBlank() || allSubstancesLabel.contains(searchText, ignoreCase = true)) {
                item(key = "all_substances") {
                    SubstanceRow(
                        name = allSubstancesLabel,
                        isSelected = selectedSubstance == null,
                        onClick = { selectedSubstance = null }
                    )
                }
            }

            items(filteredSubstances, key = { it.first }) { (name, displayName) ->
                SubstanceRow(
                    name = displayName,
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
