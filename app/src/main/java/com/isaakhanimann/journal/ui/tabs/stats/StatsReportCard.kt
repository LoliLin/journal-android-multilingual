package com.isaakhanimann.journal.ui.tabs.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.localization.i18nOrDefault
import com.isaakhanimann.journal.ui.utils.getStringOfPattern
import java.time.Instant

/**
 * A polished, fixed-width (1080px) stats report card rendered for sharing.
 * Always drawn in the light color scheme so the shared image looks the same
 * regardless of the app's dark-mode setting.
 */
@Composable
fun StatsReportCard(statsModel: StatsModel) {
    // The off-screen renderer measures the content with an EXACT width constraint
    // (e.g. 1080px), so the card fills the container instead of declaring its own
    // width in dp — dp would be scaled by density and overflow the constraint.
    MaterialTheme(colorScheme = lightColorScheme()) {
        // Force the day uiMode for the whole subtree: BarChart reads
        // isSystemInDarkTheme() from LocalConfiguration, which would otherwise
        // draw dark-theme ticks on the light card when the system is in dark mode.
        val configuration = LocalConfiguration.current
        CompositionLocalProvider(
            LocalConfiguration provides configuration.copy(
                uiMode = (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                    Configuration.UI_MODE_NIGHT_NO
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(36.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ReportHeader(statsModel)
                ReportNumbers(statsModel)
                ReportTrend(statsModel)
                ReportTopSubstances(statsModel)
                ReportFooter()
            }
        }
    }
}

@Composable
private fun ReportHeader(statsModel: StatsModel) {
    val periodText = i18nOrDefault(
        "stats_report_period_${statsModel.selectedOption.name.lowercase()}",
        statsModel.selectedOption.longDisplayText
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            )
            .padding(horizontal = 32.dp, vertical = 28.dp)
    ) {
        Column {
            Text(
                text = i18n("stats_report_title"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$periodText · ${statsModel.startDateText}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun ReportNumbers(statsModel: StatsModel) {
    val records = statsModel.statItems.sumOf { it.ingestionCount }
    val experiences = statsModel.statItems.sumOf { it.experienceCount }
    val substances = statsModel.statItems.size
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        NumberCard(value = records, label = i18n("stats_report_records"), Modifier.weight(1f))
        NumberCard(
            value = experiences,
            label = i18n("stats_report_experiences"),
            Modifier.weight(1f)
        )
        NumberCard(value = substances, label = i18n("stats_report_substances"), Modifier.weight(1f))
    }
}

@Composable
private fun NumberCard(value: Int, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReportTrend(statsModel: StatsModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                text = i18n("stats_report_trend"),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
            BarChart(
                buckets = statsModel.chartBuckets,
                startDateText = statsModel.startDateText
            )
        }
    }
}

@Composable
private fun ReportTopSubstances(statsModel: StatsModel) {
    val top = statsModel.statItems
        .sortedByDescending { it.ingestionCount }
        .take(5)
    if (top.isEmpty()) return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = i18n("stats_report_top"),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            val maxCount = top.maxOf { it.ingestionCount }.coerceAtLeast(1)
            top.forEachIndexed { index, item ->
                if (index > 0) Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(item.color.getComposeColor(isDarkTheme = false))
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.substanceRepo?.getDisplayName(item.substanceName)
                                ?: item.substanceName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(
                                        item.ingestionCount.toFloat() / maxCount
                                    )
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${item.ingestionCount}×",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportFooter() {
    Text(
        text = i18n(
            "stats_report_generated",
            mapOf("date" to Instant.now().getStringOfPattern("dd MMM yyyy"))
        ),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}
