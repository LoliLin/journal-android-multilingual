package com.isaakhanimann.journal.ui.tabs.journal.timecapsule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsTimedNotesAndRatings
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.utils.getStringOfPattern

/**
 * "This day last year" recap: the experiences recorded exactly one year ago.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeCapsuleScreen(
    viewModel: TimeCapsuleViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
    navigateToExperience: (experienceId: Int) -> Unit
) {
    var experiences by remember { mutableStateOf<List<ExperienceWithIngestionsTimedNotesAndRatings>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        experiences = viewModel.loadLastYearExperiences()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(i18n("time_capsule_screen_title")) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = i18n("common_back")
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else if (experiences.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Text(
                    text = i18n("time_capsule_empty"),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(experiences, key = { it.experience.id }) { item ->
                    val substanceNames = item.ingestions
                        .map { it.ingestion.substanceName }
                        .distinct()
                        .joinToString(", ")
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navigateToExperience(item.experience.id) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = item.experience.sortDate.getStringOfPattern("HH:mm"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = item.experience.title.ifBlank { i18n("common_untitled") },
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (substanceNames.isNotBlank()) {
                            Text(
                                text = substanceNames,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (item.timedNotes.isNotEmpty()) {
                            Text(
                                text = item.timedNotes.first().note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
