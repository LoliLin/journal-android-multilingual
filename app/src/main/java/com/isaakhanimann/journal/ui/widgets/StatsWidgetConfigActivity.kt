package com.isaakhanimann.journal.ui.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWindowCounts
import com.isaakhanimann.journal.di.JournalApplication
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.theme.JournalTheme
import java.text.Collator
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Model representing a selectable substance in the widget configuration list. */
data class WidgetSubstanceItem(
    val name: String,
    val displayName: String,
    val commonNames: List<String> = emptyList()
)

/**
 * Per-widget configuration: pick a substance (or all) and a rolling window.
 * Reached from the widget's gear button and from the launcher configure step.
 */
class StatsWidgetConfigActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
            val sortedSubstances: List<WidgetSubstanceItem> = withContext(Dispatchers.Default) {
                val names = StatsWidgetData.readConfiguredSubstanceNames(
                    this@StatsWidgetConfigActivity,
                    app.experienceRepository
                )
                val collator = Collator.getInstance(Locale.getDefault())
                names.map { name ->
                    val substance = app.substanceRepo.getSubstance(name)
                    val displayName = substance?.localizedName ?: app.substanceRepo.getDisplayName(name)
                    val commonNames = substance?.commonNames ?: emptyList()
                    WidgetSubstanceItem(
                        name = name,
                        displayName = displayName,
                        commonNames = commonNames
                    )
                }.sortedWith { a, b -> collator.compare(a.displayName, b.displayName) }
            }

            setContent {
                JournalTheme {
                    StatsWidgetConfigScreen(
                        substances = sortedSubstances,
                        initialSubstance = config.substanceName,
                        initialDays = config.days,
                        onCancel = { finish() },
                        loadCounts = { substanceName, days ->
                            val to = Instant.now()
                            val from = to.minus(days.toLong(), ChronoUnit.DAYS)
                            app.experienceRepository.getIngestionWindowCounts(from, to, substanceName)
                        },
                        onSave = { substanceName, days ->
                            StatsWidgetData.writeConfig(
                                this@StatsWidgetConfigActivity,
                                appWidgetId,
                                StatsWidgetConfig(substanceName, days)
                            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsWidgetConfigScreen(
    substances: List<WidgetSubstanceItem>,
    initialSubstance: String?,
    initialDays: Int,
    onCancel: () -> Unit,
    loadCounts: suspend (substanceName: String?, days: Int) -> IngestionWindowCounts,
    onSave: (substanceName: String?, days: Int) -> Unit
) {
    var selectedSubstance by remember { mutableStateOf(initialSubstance) }
    var selectedDays by remember { mutableStateOf(initialDays) }
    var searchText by remember { mutableStateOf("") }
    val allSubstancesLabel = i18n("widget_config_all_substances")

    var previewCounts by remember { mutableStateOf<IngestionWindowCounts?>(null) }
    LaunchedEffect(selectedSubstance, selectedDays) {
        previewCounts = withContext(Dispatchers.IO) {
            loadCounts(selectedSubstance, selectedDays)
        }
    }

    val selectedDisplayName = remember(selectedSubstance, substances, allSubstancesLabel) {
        if (selectedSubstance == null) {
            allSubstancesLabel
        } else {
            substances.find { it.name == selectedSubstance }?.displayName ?: selectedSubstance
        }
    }

    val standardPeriods = remember { listOf(7, 14, 30, 90, 180, 365) }
    val periods = remember(initialDays, standardPeriods) {
        if (initialDays !in standardPeriods) {
            (standardPeriods + initialDays).sorted()
        } else {
            standardPeriods
        }
    }

    val filteredSubstances = remember(substances, searchText) {
        if (searchText.isBlank()) {
            substances
        } else {
            val query = searchText.trim()
            substances.filter { item ->
                item.displayName.contains(query, ignoreCase = true) ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.commonNames.any { it.contains(query, ignoreCase = true) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = i18n("widget_config_title"),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = i18n("common_back")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = { onSave(selectedSubstance, selectedDays) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = i18n("common_save"),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
        ) {
            // Live Preview Card
            item(key = "widget_preview") {
                WidgetPreviewSection(
                    title = "$selectedDisplayName · ${selectedDays}d",
                    counts = previewCounts,
                    isSingleSubstance = selectedSubstance != null
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Period Selection
            item(key = "period_selection") {
                Text(
                    text = i18n("widget_config_period"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    periods.forEach { days ->
                        val isSelected = selectedDays == days
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDays = days },
                            label = {
                                Text(
                                    text = "${days}d",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Substance Selection Header & Search
            item(key = "substances_header") {
                Text(
                    text = i18n("widget_config_substance"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (substances.isNotEmpty()) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        placeholder = { Text(i18n("search_substances_placeholder")) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = i18n("common_search")
                            )
                        },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { searchText = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = i18n("common_close")
                                    )
                                }
                            }
                        },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // When no substances recorded in app yet
            if (substances.isEmpty()) {
                item(key = "no_substances_hint") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = i18n("widget_config_no_substances_hint"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // All substances option
            if (searchText.isBlank() || allSubstancesLabel.contains(searchText, ignoreCase = true)) {
                item(key = "all_substances") {
                    SubstanceItemCard(
                        displayName = allSubstancesLabel,
                        secondaryName = if (substances.isNotEmpty()) {
                            "${substances.size} ${i18n("widget_stat_substances")}"
                        } else null,
                        icon = Icons.Outlined.Dashboard,
                        isSelected = selectedSubstance == null,
                        onClick = { selectedSubstance = null }
                    )
                }
            }

            // Filtered specific substances
            items(filteredSubstances, key = { it.name }) { item ->
                SubstanceItemCard(
                    displayName = item.displayName,
                    secondaryName = if (item.displayName != item.name) item.name else null,
                    isSelected = selectedSubstance == item.name,
                    onClick = { selectedSubstance = item.name }
                )
            }

            // Search empty state
            if (filteredSubstances.isEmpty() && searchText.isNotBlank() &&
                !allSubstancesLabel.contains(searchText, ignoreCase = true)
            ) {
                item(key = "search_empty") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = i18n("widget_config_search_no_results"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetPreviewSection(
    title: String,
    counts: IngestionWindowCounts?,
    isSingleSubstance: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = i18n("widget_config_preview"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF263238), Color(0xFF37474F)),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color(0xFFB0BEC5),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatPreviewColumn(
                            count = counts?.ingestionCount ?: 0,
                            label = i18n("widget_stat_ingestions"),
                            modifier = Modifier.weight(1f)
                        )
                        StatPreviewColumn(
                            count = counts?.experienceCount ?: 0,
                            label = i18n("widget_stat_experiences"),
                            modifier = Modifier.weight(1f)
                        )
                        if (!isSingleSubstance) {
                            StatPreviewColumn(
                                count = counts?.substanceCount ?: 0,
                                label = i18n("widget_stat_substances"),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPreviewColumn(
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(
            targetState = count,
            transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
            label = "stat_count"
        ) { targetCount ->
            Text(
                text = targetCount.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFB0BEC5),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SubstanceItemCard(
    displayName: String,
    secondaryName: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        },
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!secondaryName.isNullOrBlank()) {
                    Text(
                        text = secondaryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            RadioButton(
                selected = isSelected,
                onClick = null
            )
        }
    }
}
