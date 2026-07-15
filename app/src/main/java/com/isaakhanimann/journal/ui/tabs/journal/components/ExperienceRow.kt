/*
 * Copyright (c) 2022-2023. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * PsychonautWiki Journal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PsychonautWiki Journal.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.ui.tabs.journal.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsCompanionsAndRatings
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithCompanionAndCustomUnit
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.tabs.journal.experience.ShareableExperienceCard
import com.isaakhanimann.journal.ui.tabs.journal.experience.prepareShareableExperienceCardData
import com.isaakhanimann.journal.ui.theme.JournalTheme
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import com.isaakhanimann.journal.ui.utils.getStringOfPattern
import com.isaakhanimann.journal.ui.utils.renderComposeViewToBitmap
import com.isaakhanimann.journal.ui.utils.shareBitmap
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun ExperienceRow(
    @PreviewParameter(
        ExperienceWithIngestionsCompanionsAndRatingsPreviewProvider::class
    ) experienceWithIngestionsCompanionsAndRatings: ExperienceWithIngestionsCompanionsAndRatings,
    rowViewModel: ExperienceRowViewModel = hiltViewModel(),
    navigateToExperienceScreen: () -> Unit = {},
    isTimeRelativeToNow: Boolean = true,
    substanceRepository: SubstanceRepository,
    ownerUserName: String = "You"
) {
    Row(
        modifier = Modifier
            .clickable {
                navigateToExperienceScreen()
            }
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = horizontalPadding, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current
        val currentView = LocalView.current
        val coroutineScope = rememberCoroutineScope()
        val ingestions = experienceWithIngestionsCompanionsAndRatings.ingestionsWithCompanions
        val experience = experienceWithIngestionsCompanionsAndRatings.experience
        val timedNotes = rowViewModel.getTimedNotes(
            experience.id
        ).collectAsState(initial = emptyList()).value
        val substanceRepo = rowViewModel.substanceRepo
        val interactionChecker = rowViewModel.interactionChecker
        val getSubstanceDisplayName = rowViewModel.substanceRepo::getDisplayName
        val achievements = rowViewModel.achievementsFlow.collectAsState().value

        val cardData = prepareShareableExperienceCardData(
            substanceRepo = substanceRepo,
            interactionChecker = interactionChecker,
            ownerUserName = ownerUserName,
            getSubstanceDisplayName = getSubstanceDisplayName,
            timedNotes = timedNotes,
            achievements = achievements,
            experienceWithIngestionsCompanionsAndRatings = experienceWithIngestionsCompanionsAndRatings
        )

        ColorRectangle(ingestions = ingestions)
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = experience.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (experience.isFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = i18n("journal_is_favorite")
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                val substanceNames = remember(ingestions) {
                    ingestions.map {
                        substanceRepository.getDisplayName(it.ingestion.substanceName)
                    }.distinct()
                        .joinToString(separator = ", ")
                }
                if (substanceNames.isNotEmpty()) {
                    Text(text = substanceNames)
                } else {
                    Text(
                        text = i18n("no_substance_yet")
                    )
                }
                val rating = experienceWithIngestionsCompanionsAndRatings.rating?.sign
                if (rating != null) {
                    Text(text = rating)
                }
            }
            val consumerNames = remember(ingestions, ownerUserName) {
                ingestions.map {
                    (
                        it.ingestion.consumerName?.ifBlank { ownerUserName }
                            ?: ownerUserName
                        )
                }.distinct()
                    .joinToString(separator = ", ")
            }

            if (consumerNames.isNotEmpty()) {
                Text(text = consumerNames, style = MaterialTheme.typography.labelSmall)
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                val timeStyle = MaterialTheme.typography.labelMedium
                if (isTimeRelativeToNow) {
                    RelativeDateTextNew(
                        dateTime = experienceWithIngestionsCompanionsAndRatings.sortInstant,
                        style = timeStyle
                    )
                } else {
                    Text(
                        text = experienceWithIngestionsCompanionsAndRatings.sortInstant.getStringOfPattern(
                            "EEE, dd MMM yyyy"
                        ),
                        style = timeStyle
                    )
                }
                val location = experience.location
                if (location != null) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        IconButton(onClick = {
            coroutineScope.launch {
                try {
                    val activity = context as? androidx.activity.ComponentActivity
                    if (activity != null) {
                        val bitmap = renderComposeViewToBitmap(
                            context = context,
                            widthPx = 1080,
                            lifecycleView = currentView,
                            content = {
                                JournalTheme {
                                    ShareableExperienceCard(cardData = cardData)
                                }
                            },
                            postLayoutDelayMs = 300L
                        )
                        shareBitmap(context, bitmap)
                    }
                } catch (e: Exception) {
                    Log.e("ExperienceRow", "error", e)
                    Toast.makeText(context, "${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Icon(
                Icons.Outlined.Share,
                contentDescription = "分享卡片",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
        }
    }
}

@Composable
fun ColorRectangle(ingestions: List<IngestionWithCompanionAndCustomUnit>) {
    val isDarkTheme = isSystemInDarkTheme()
    val width = 11.dp
    val cornerRadius = 3.dp
    if (ingestions.size >= 2) {
        val brush = remember(ingestions) {
            val colors =
                ingestions.map { it.substanceCompanion!!.color.getComposeColor(isDarkTheme) }
            Brush.verticalGradient(colors = colors)
        }
        Box(
            modifier = Modifier
                .width(width)
                .fillMaxHeight()
                .clip(RoundedCornerShape(cornerRadius))
                .background(brush)
        ) {}
    } else if (ingestions.size == 1) {
        Box(
            modifier = Modifier
                .width(width)
                .fillMaxHeight()
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    ingestions.first().substanceCompanion!!.color.getComposeColor(
                        isDarkTheme
                    )
                )
        ) {}
    } else {
        Box(
            modifier = Modifier
                .width(width)
                .fillMaxHeight()
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color.LightGray.copy(0.1f))
        ) {}
    }
}
