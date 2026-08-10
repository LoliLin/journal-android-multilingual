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

package com.isaakhanimann.journal.ui.tabs.journal.experience

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExposurePlus2
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.CardTitle
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.CardTitleWithAvatar
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.CumulativeDoseRow
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.ExperienceEffectTimelines
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.InteractionRow
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.SavedTimeDisplayOption
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.TimeDisplayOption
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.ingestion.IngestionRow
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.rating.RatingRow
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.timednote.TimedNoteRow
import com.isaakhanimann.journal.ui.tabs.journal.experience.models.OneExperienceScreenModel
import com.isaakhanimann.journal.ui.tabs.journal.experience.timeline.DataForOneRating
import com.isaakhanimann.journal.ui.tabs.journal.experience.timeline.DataForOneTimedNote
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import com.isaakhanimann.journal.ui.utils.getStringOfPattern
import java.time.Instant

@Composable
fun OneExperienceScreen(
    viewModel: OneExperienceViewModel = hiltViewModel(),
    navigateToAddIngestionSearch: () -> Unit,
    navigateToEditExperienceScreen: () -> Unit,
    navigateToExplainTimeline: () -> Unit,
    navigateToURL: (url: String) -> Unit,
    navigateToIngestionScreen: (ingestionId: Int) -> Unit,
    navigateToAddRatingScreen: () -> Unit,
    navigateToAddTimedNoteScreen: () -> Unit,
    navigateToEditRatingScreen: (ratingId: Int) -> Unit,
    navigateToEditTimedNoteScreen: (timedNoteId: Int) -> Unit,
    navigateToTimelineScreen: (consumerName: String) -> Unit,
    navigateBack: () -> Unit
) {
    val ingestionsWithCompanions = viewModel.ingestionsWithCompanionsFlow.collectAsState().value
    val experience = viewModel.experienceFlow.collectAsState().value
    val isFavorite = viewModel.isFavoriteFlow.collectAsState().value
    val oneExperienceScreenModel = OneExperienceScreenModel(
        isFavorite = isFavorite,
        title = experience?.title ?: "",
        firstIngestionTime = ingestionsWithCompanions.firstOrNull()?.ingestion?.time
            ?: experience?.sortDate ?: Instant.now(),
        notes = experience?.text ?: "",
        locationName = experience?.location?.name ?: "",
        isCurrentExperience = viewModel.isCurrentExperienceFlow.collectAsState().value,
        ingestionElements = viewModel.ingestionElementsFlow.collectAsState().value,
        cumulativeDoses = viewModel.cumulativeDosesFlow.collectAsState().value,
        interactions = viewModel.interactionsFlow.collectAsState().value,
        interactionExplanations = viewModel.interactionExplanationsFlow.collectAsState().value,
        ratings = viewModel.ratingsFlow.collectAsState().value,
        timedNotes = viewModel.timedNotesFlow.collectAsState().value,
        consumersWithIngestions = viewModel.consumersWithIngestionsFlow.collectAsState().value
    )
    OneExperienceScreen(
        oneExperienceScreenModel = oneExperienceScreenModel,
        viewModel = viewModel,
        isOralDisclaimerHidden = viewModel.isOralTimelineDisclaimerHidden.collectAsState().value,
        onChangeIsOralDisclaimerHidden = viewModel::saveOralDisclaimerIsHidden,
        addIngestion = navigateToAddIngestionSearch,
        deleteExperience = viewModel::deleteExperience,
        navigateToEditExperienceScreen = navigateToEditExperienceScreen,
        navigateToExplainTimeline = navigateToExplainTimeline,
        navigateToIngestionScreen = navigateToIngestionScreen,
        navigateToAddRatingScreen = navigateToAddRatingScreen,
        navigateToAddTimedNoteScreen = navigateToAddTimedNoteScreen,
        navigateBack = navigateBack,
        saveIsFavorite = viewModel::saveIsFavorite,
        navigateToURL = navigateToURL,
        navigateToEditRatingScreen = navigateToEditRatingScreen,
        navigateToEditTimedNoteScreen = navigateToEditTimedNoteScreen,
        savedTimeDisplayOption = viewModel.savedTimeDisplayOption.collectAsState().value,
        timeDisplayOption = viewModel.timeDisplayOptionFlow.collectAsState().value,
        onChangeTimeDisplayOption = viewModel::saveTimeDisplayOption,
        navigateToTimelineScreen = navigateToTimelineScreen,
        areDosageDotsHidden = viewModel.areDosageDotsHiddenFlow.collectAsState().value,
        ownerUserName = viewModel.ownerUserNameFlow.collectAsState().value ?: "You",
        getSubstanceDisplayName = viewModel.substanceRepo::getDisplayName
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OneExperienceScreen(
    oneExperienceScreenModel: OneExperienceScreenModel,
    viewModel: OneExperienceViewModel,
    isOralDisclaimerHidden: Boolean,
    onChangeIsOralDisclaimerHidden: (Boolean) -> Unit,
    addIngestion: () -> Unit,
    deleteExperience: () -> Unit,
    navigateToEditExperienceScreen: () -> Unit,
    navigateToURL: (url: String) -> Unit,
    navigateToExplainTimeline: () -> Unit,
    navigateToIngestionScreen: (ingestionId: Int) -> Unit,
    navigateToAddRatingScreen: () -> Unit,
    navigateToAddTimedNoteScreen: () -> Unit,
    navigateBack: () -> Unit,
    saveIsFavorite: (Boolean) -> Unit,
    navigateToEditRatingScreen: (ratingId: Int) -> Unit,
    navigateToEditTimedNoteScreen: (timedNoteId: Int) -> Unit,
    savedTimeDisplayOption: SavedTimeDisplayOption,
    timeDisplayOption: TimeDisplayOption,
    onChangeTimeDisplayOption: (SavedTimeDisplayOption) -> Unit,
    navigateToTimelineScreen: (consumerName: String) -> Unit,
    areDosageDotsHidden: Boolean,
    ownerUserName: String,
    getSubstanceDisplayName: (String) -> String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(oneExperienceScreenModel.title) },
                actions = {
                    var areTimeOptionsExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { areTimeOptionsExpanded = true }) {
                        Icon(Icons.Outlined.Timer, contentDescription = i18n("time_display_option"))
                    }
                    DropdownMenu(
                        expanded = areTimeOptionsExpanded,
                        onDismissRequest = { areTimeOptionsExpanded = false }
                    ) {
                        SavedTimeDisplayOption.values().forEach { option ->
                            val context = LocalContext.current
                            DropdownMenuItem(
                                text = { Text(option.getTranslatedText(context)) },
                                onClick = {
                                    onChangeTimeDisplayOption(option)
                                    areTimeOptionsExpanded = false
                                },
                                leadingIcon = {
                                    if (option == savedTimeDisplayOption) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = i18n("common_check"),
                                            modifier = Modifier.size(ButtonDefaults.IconSize)
                                        )
                                    }
                                }
                            )
                        }
                    }
                    var areEditOptionsExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { areEditOptionsExpanded = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = i18n("edit_options")
                        )
                    }
                    var isShowingDeleteDialog by remember { mutableStateOf(false) }
                    AnimatedVisibility(visible = isShowingDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { isShowingDeleteDialog = false },
                            title = {
                                Text(text = i18n("delete_experience_question"))
                            },
                            text = {
                                Text(i18n("delete_experience_ensure"))
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        isShowingDeleteDialog = false
                                        deleteExperience()
                                        navigateBack()
                                    }
                                ) {
                                    Text(i18n("common_delete"))
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { isShowingDeleteDialog = false }
                                ) {
                                    Text(i18n("common_cancel"))
                                }
                            }
                        )
                    }
                    DropdownMenu(
                        expanded = areEditOptionsExpanded,
                        onDismissRequest = { areEditOptionsExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(i18n("edit_title_notes_location")) },

                            onClick = {
                                navigateToEditExperienceScreen()

                                areEditOptionsExpanded = false
                            },

                            leadingIcon = {
                                Icon(

                                    Icons.Outlined.Edit,

                                    contentDescription = i18n("edit_title_notes_location"),
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                            }
                        )
                        val isFavorite = oneExperienceScreenModel.isFavorite
                        if (isFavorite) {
                            DropdownMenuItem(
                                text = { Text(i18n("unmark_favorite")) },

                                onClick = {
                                    saveIsFavorite(false)

                                    areEditOptionsExpanded = false
                                },

                                leadingIcon = {
                                    Icon(

                                        Icons.Filled.Star,

                                        contentDescription = i18n("unmark_favorite"),
                                        modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text(i18n("mark_favorite")) },

                                onClick = {
                                    saveIsFavorite(true)

                                    areEditOptionsExpanded = false
                                },

                                leadingIcon = {
                                    Icon(

                                        Icons.Outlined.StarOutline,

                                        contentDescription = i18n("mark_favorite"),
                                        modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                }
                            )
                        }
                        DropdownMenuItem(

                            text = { Text(i18n("delete_experience_question")) },

                            onClick = {
                                isShowingDeleteDialog = true

                                areEditOptionsExpanded = false
                            },

                            leadingIcon = {
                                Icon(

                                    Icons.Outlined.Delete,

                                    contentDescription = i18n("delete_experience_question"),
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                            }
                        )
                    }

                    var areAddOptionsExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { areAddOptionsExpanded = true }) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = i18n("add_options")
                        )
                    }
                    DropdownMenu(
                        expanded = areAddOptionsExpanded,
                        onDismissRequest = { areAddOptionsExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(i18n("add_timed_note")) },

                            onClick = {
                                navigateToAddTimedNoteScreen()

                                areAddOptionsExpanded = false
                            },

                            leadingIcon = {
                                Icon(

                                    Icons.AutoMirrored.Outlined.NoteAdd,

                                    contentDescription = i18n("add_timed_note"),
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(i18n("add_shulgin_rating")) },

                            onClick = {
                                navigateToAddRatingScreen()

                                areAddOptionsExpanded = false
                            },

                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.ExposurePlus2,
                                    contentDescription = i18n("add_shulgin_rating"),
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (oneExperienceScreenModel.isCurrentExperience) {
                ExtendedFloatingActionButton(
                    onClick = addIngestion,
                    icon = {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = i18n("common_add")
                        )
                    },
                    text = { Text(i18n("journal_ingestion")) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = horizontalPadding)
        ) {
            val verticalCardPadding = 4.dp
            val ingestionElements = oneExperienceScreenModel.ingestionElements
            val dataForRatings = oneExperienceScreenModel.ratings.mapNotNull {
                val ratingTime = it.time
                return@mapNotNull if (ratingTime == null) {
                    null
                } else {
                    DataForOneRating(
                        time = ratingTime,
                        option = it.option
                    )
                }
            }
            val dataForTimedNotes =
                oneExperienceScreenModel.timedNotes.filter { it.isPartOfTimeline }
                    .map {
                        DataForOneTimedNote(time = it.time, color = it.color)
                    }
            val isWorthDrawing =
                ingestionElements.isNotEmpty() &&
                    !(
                        ingestionElements.all { it.roaDuration == null } &&
                            dataForRatings.isEmpty() &&
                            dataForTimedNotes.isEmpty()
                        )
            if (isWorthDrawing) {
                ElevatedCard(modifier = Modifier.padding(vertical = verticalCardPadding)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CardTitleWithAvatar(
                            title = if (ownerUserName ==
                                "You"
                            ) {
                                i18n("effect_timeline")
                            } else {
                                ownerUserName
                            },
                            username = ownerUserName,
                            // weight(1f): CardTitleWithAvatar fills the row width on its own,
                            // which would push the "Limitations" button off-screen.
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = navigateToExplainTimeline) {
                            Text(text = i18n("limitations"))
                        }
                    }
                    Column(
                        modifier = Modifier
                            .padding(horizontal = horizontalPadding)
                            .padding(bottom = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        // When the substance data lacks onset/comeup/peak/offset the curve
                        // degrades to a bottom line; shrink the canvas so there is no big
                        // empty gap between the title and the curve.
                        val hasFullDuration = ingestionElements.any {
                            it.roaDuration?.onset != null &&
                                it.roaDuration.comeup != null &&
                                it.roaDuration.peak != null &&
                                it.roaDuration.offset != null
                        }
                        ExperienceEffectTimelines(
                            ingestionElements = oneExperienceScreenModel.ingestionElements,
                            dataForRatings = dataForRatings,
                            dataForTimedNotes = dataForTimedNotes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (hasFullDuration) 200.dp else 110.dp)
                                .clickable {
                                    navigateToTimelineScreen(ownerUserName)
                                }
                        )
                        val hasOralIngestion =
                            oneExperienceScreenModel.ingestionElements.any {
                                it.ingestionWithCompanionAndCustomUnit.ingestion.administrationRoute ==
                                    AdministrationRoute.ORAL
                            }
                        if (hasOralIngestion && !isOralDisclaimerHidden) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = i18n("FULL_STOMACH_DISCLAIMER"),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { onChangeIsOralDisclaimerHidden(true) }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = i18n("close_disclaimer")
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (oneExperienceScreenModel.ingestionElements.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(vertical = verticalCardPadding)) {
                    CardTitle(
                        title = oneExperienceScreenModel.firstIngestionTime.getStringOfPattern(
                            "EEE, dd MMM yyyy"
                        )
                    )
                    if (oneExperienceScreenModel.ingestionElements.isNotEmpty()) {
                        HorizontalDivider()
                    }
                    oneExperienceScreenModel.ingestionElements.forEachIndexed {
                            index,
                            ingestionElement
                        ->
                        IngestionRow(
                            ingestionElement = ingestionElement,
                            timeDisplayOption = timeDisplayOption,
                            startTime = oneExperienceScreenModel.firstIngestionTime,
                            areDosageDotsHidden = areDosageDotsHidden,
                            modifier = Modifier
                                .clickable {
                                    navigateToIngestionScreen(
                                        ingestionElement.ingestionWithCompanionAndCustomUnit.ingestion.id
                                    )
                                }
                                .fillMaxWidth()
                                .padding(vertical = 5.dp, horizontal = horizontalPadding),
                            getSubstanceDisplayName = getSubstanceDisplayName
                        )
                        if (index < oneExperienceScreenModel.ingestionElements.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            }
            val cumulativeDoses = oneExperienceScreenModel.cumulativeDoses
            if (cumulativeDoses.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(vertical = verticalCardPadding)) {
                    CardTitle(title = i18n("your_cumulative_doses"))
                    if (cumulativeDoses.isNotEmpty()) {
                        HorizontalDivider()
                    }
                    cumulativeDoses.forEachIndexed { index, cumulativeDose ->
                        CumulativeDoseRow(
                            cumulativeDose = cumulativeDose,
                            areDosageDotsHidden = areDosageDotsHidden,
                            getSubstanceDisplayName = getSubstanceDisplayName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp, horizontal = horizontalPadding)
                        )
                        if (index < cumulativeDoses.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            }
            val timedNotes = oneExperienceScreenModel.timedNotes
            if (timedNotes.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(vertical = verticalCardPadding)) {
                    CardTitle(title = i18n("timed_notes"))
                    if (timedNotes.isNotEmpty()) {
                        HorizontalDivider()
                    }
                    timedNotes.forEachIndexed { index, timedNote ->
                        TimedNoteRow(
                            timedNote = timedNote,
                            timeDisplayOption = timeDisplayOption,
                            startTime = oneExperienceScreenModel.firstIngestionTime,
                            modifier = Modifier
                                .clickable {
                                    navigateToEditTimedNoteScreen(timedNote.id)
                                }
                                .fillMaxWidth()
                                .padding(vertical = 5.dp, horizontal = horizontalPadding)
                        )
                        if (index < timedNotes.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            }
            if (oneExperienceScreenModel.ratings.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(vertical = verticalCardPadding)) {
                    CardTitle(title = i18n("shulgin_ratings"))
                    HorizontalDivider()
                    val ratingsWithTime =
                        oneExperienceScreenModel.ratings.filter { it.time != null }
                    ratingsWithTime.forEachIndexed { index, rating ->
                        RatingRow(
                            rating = rating,
                            timeDisplayOption = timeDisplayOption,
                            startTime = oneExperienceScreenModel.firstIngestionTime,
                            modifier = Modifier
                                .clickable {
                                    navigateToEditRatingScreen(rating.id)
                                }
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = horizontalPadding)
                        )
                        if (index < ratingsWithTime.size - 1) {
                            HorizontalDivider()
                        }
                    }
                    val overallRating =
                        oneExperienceScreenModel.ratings.firstOrNull { it.time == null }
                    if (overallRating != null) {
                        if (ratingsWithTime.isNotEmpty()) {
                            HorizontalDivider()
                        }
                        RatingRow(
                            rating = overallRating,
                            timeDisplayOption = timeDisplayOption,
                            startTime = oneExperienceScreenModel.firstIngestionTime,
                            modifier = Modifier
                                .clickable {
                                    navigateToEditRatingScreen(overallRating.id)
                                }
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = horizontalPadding)
                        )
                    }
                }
            }
            val notes = oneExperienceScreenModel.notes
            if (notes.isNotBlank()) {
                ElevatedCard(
                    modifier = Modifier
                        .padding(vertical = verticalCardPadding)
                        .fillMaxWidth()
                        .clickable { navigateToEditExperienceScreen() }
                ) {
                    CardTitle(title = i18n("common_notes"))
                    Column(
                        modifier = Modifier
                            .padding(horizontal = horizontalPadding)
                            .padding(bottom = 10.dp)
                    ) {
                        Text(text = oneExperienceScreenModel.notes)
                        if (oneExperienceScreenModel.locationName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(text = "Location: ${oneExperienceScreenModel.locationName}")
                        }
                    }
                }
            }
            oneExperienceScreenModel.consumersWithIngestions.forEach { consumerWithIngestions ->
                ElevatedCard(modifier = Modifier.padding(vertical = verticalCardPadding)) {
                    CardTitleWithAvatar(
                        title = consumerWithIngestions.consumerName,
                        username = consumerWithIngestions.consumerName
                    )
                    Column(
                        modifier = Modifier
                            .padding(horizontal = horizontalPadding)
                            .padding(bottom = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        ExperienceEffectTimelines(
                            ingestionElements = consumerWithIngestions.ingestionElements,
                            dataForRatings = emptyList(),
                            dataForTimedNotes = emptyList(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clickable {
                                    navigateToTimelineScreen(consumerWithIngestions.consumerName)
                                }
                        )
                    }
                    HorizontalDivider()
                    consumerWithIngestions.ingestionElements.forEachIndexed {
                            index,
                            ingestionElement
                        ->
                        IngestionRow(
                            ingestionElement = ingestionElement,
                            timeDisplayOption = timeDisplayOption,
                            startTime = oneExperienceScreenModel.firstIngestionTime,
                            areDosageDotsHidden = areDosageDotsHidden,
                            modifier = Modifier
                                .clickable {
                                    navigateToIngestionScreen(
                                        ingestionElement.ingestionWithCompanionAndCustomUnit.ingestion.id
                                    )
                                }
                                .fillMaxWidth()
                                .padding(vertical = 5.dp, horizontal = horizontalPadding),
                            getSubstanceDisplayName = getSubstanceDisplayName
                        )
                        if (index < consumerWithIngestions.ingestionElements.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            }
            val interactions = oneExperienceScreenModel.interactions
            AnimatedVisibility(visible = interactions.isNotEmpty()) {
                ElevatedCard(
                    modifier = Modifier
                        .padding(vertical = verticalCardPadding)
                ) {
                    CardTitle(title = i18n("substance_interactions_title"))
                    interactions.forEachIndexed { index, interaction ->
                        InteractionRow(
                            interaction = interaction,
                            getSubstanceDisplayName = getSubstanceDisplayName
                        )
                        if (index < interactions.size - 1) {
                            HorizontalDivider()
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = i18n("interaction_explanations"),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = horizontalPadding)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = horizontalPadding)
                    ) {
                        oneExperienceScreenModel.interactionExplanations.forEach {
                            SuggestionChip(
                                onClick = {
                                    navigateToURL(it.url)
                                },
                                label = { Text(viewModel.substanceRepo.getDisplayName(it.name)) }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
