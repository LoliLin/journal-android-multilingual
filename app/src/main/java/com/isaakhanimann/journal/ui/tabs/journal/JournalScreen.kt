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

package com.isaakhanimann.journal.ui.tabs.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.isaakhanimann.journal.data.achievement.AchievementEvaluator
import com.isaakhanimann.journal.data.achievement.AchievementGetToast
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsCompanionsAndRatings
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.tabs.journal.components.ExperienceRow
import com.isaakhanimann.journal.ui.main.bottomBarNestedScroll
import com.isaakhanimann.journal.ui.main.bottomBarOverlayDp
import com.isaakhanimann.journal.ui.main.bottomBarOverlayPadding
import com.isaakhanimann.journal.ui.tabs.stats.EmptyScreenDisclaimer
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import kotlinx.coroutines.launch

@Composable
fun JournalScreen(
    navigateToExperiencePopNothing: (experienceId: Int) -> Unit,
    navigateToAddIngestion: () -> Unit,
    navigateToCalendar: () -> Unit,
    navigateToQuickTimedNote: (experienceId: Int) -> Unit,
    viewModel: JournalViewModel = hiltViewModel()
) {
    val experiences = viewModel.experiences.collectAsState().value

    val achievements by viewModel.achievementsFlow.collectAsState(initial = null as List<String>?)
    val ingestions by viewModel.ingestionsFlow.collectAsState(initial = emptyList())
    val ownerUserName = viewModel.ownerUserNameFlow.collectAsState().value

    LaunchedEffect(ingestions, achievements, ownerUserName) {
        // Skip until DataStore has loaded: with the initial emptyList() a cold start
        // re-triggered already-unlocked achievements (toast + event) before the
        // persisted list arrived.
        val achieved = achievements ?: return@LaunchedEffect
        val ownerName = ownerUserName ?: return@LaunchedEffect
        for (definition in viewModel.achievementDefinitions) {
            if (
                !achieved.contains(definition.registerName) &&
                AchievementEvaluator.evaluate(definition, ingestions, ownerName)
            ) {
                viewModel.addAchievement(definition.registerName)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.maybeMigrate()
    }

    JournalScreen(
        navigateToExperiencePopNothing = navigateToExperiencePopNothing,
        navigateToAddIngestion = {
            viewModel.resetAddIngestionTimes()
            navigateToAddIngestion()
        },
        navigateToCalendar = navigateToCalendar,
        isFavoriteEnabled = viewModel.isFavoriteEnabledFlow.collectAsState().value,
        onChangeIsFavorite = viewModel::onChangeFavorite,
        isTimeRelativeToNow = viewModel.isTimeRelativeToNow.value,
        onChangeIsRelative = viewModel::onChangeRelative,
        searchText = viewModel.searchTextFlow.collectAsState().value,
        onChangeSearchText = viewModel::search,
        isSearchEnabled = viewModel.isSearchEnabled.value,
        onChangeIsSearchEnabled = viewModel::onChangeOfIsSearchEnabled,
        experiences = experiences,
        substanceRepository = viewModel.substanceRepository,
        ownerUserName = ownerUserName ?: "You",
        navigateToQuickTimedNote = navigateToQuickTimedNote
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    navigateToExperiencePopNothing: (experienceId: Int) -> Unit,
    navigateToAddIngestion: () -> Unit,
    navigateToCalendar: () -> Unit,
    isFavoriteEnabled: Boolean,
    onChangeIsFavorite: (Boolean) -> Unit,
    isTimeRelativeToNow: Boolean,
    onChangeIsRelative: (Boolean) -> Unit,
    searchText: String,
    onChangeSearchText: (String) -> Unit,
    isSearchEnabled: Boolean,
    onChangeIsSearchEnabled: (Boolean) -> Unit,
    experiences: List<ExperienceWithIngestionsCompanionsAndRatings>,
    substanceRepository: SubstanceRepository,
    ownerUserName: String,
    navigateToQuickTimedNote: (experienceId: Int) -> Unit = {}
) {
    val latestExperienceId = experiences
        .firstOrNull { it.ingestionsWithCompanions.isNotEmpty() }
        ?.experience?.id
    Scaffold(
        modifier = Modifier.bottomBarNestedScroll(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(i18n("journal")) },
                actions = {
                    IconToggleButton(
                        checked = isTimeRelativeToNow,
                        onCheckedChange = onChangeIsRelative
                    ) {
                        if (isTimeRelativeToNow) {
                            Icon(
                                Icons.Filled.Timer,
                                contentDescription = i18n("journal_regular_time")
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Timer,
                                contentDescription = i18n("journal_time_relative_to_now")
                            )
                        }
                    }
                    if (latestExperienceId != null) {
                        IconButton(onClick = { navigateToQuickTimedNote(latestExperienceId) }) {
                            Icon(
                                Icons.Outlined.EditNote,
                                contentDescription = i18n("quick_note_title")
                            )
                        }
                    }
                    IconToggleButton(
                        checked = isFavoriteEnabled,
                        onCheckedChange = onChangeIsFavorite
                    ) {
                        if (isFavoriteEnabled) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = i18n("journal_is_favorite")
                            )
                        } else {
                            Icon(
                                Icons.Outlined.StarOutline,
                                contentDescription = i18n("journal_is_not_favorite")
                            )
                        }
                    }
                    IconToggleButton(
                        checked = isSearchEnabled,
                        onCheckedChange = onChangeIsSearchEnabled
                    ) {
                        if (isSearchEnabled) {
                            Icon(
                                Icons.Outlined.SearchOff,
                                contentDescription = i18n("journal_search_off")
                            )
                        } else {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = i18n("common_search")
                            )
                        }
                    }
                    IconButton(onClick = navigateToCalendar) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = i18n("journal_navigate_to_calendar")
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isSearchEnabled) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.padding(bottom = bottomBarOverlayDp()),
                    onClick = navigateToAddIngestion,
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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AchievementGetToast(
                modifier = Modifier.align(Alignment.TopEnd)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                AnimatedVisibility(visible = isSearchEnabled) {
                    Column {
                        val focusManager = LocalFocusManager.current
                        TextField(
                            value = searchText,
                            onValueChange = onChangeSearchText,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = i18n("common_search")
                                )
                            },
                            trailingIcon = {
                                if (searchText != "") {
                                    IconButton(
                                        onClick = {
                                            onChangeSearchText("")
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = i18n("common_close")
                                        )
                                    }
                                }
                            },
                            label = { Text(text = i18n("journal_search_by_title_or_substance")) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                            }),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            singleLine = true
                        )
                        if (experiences.isEmpty() && isSearchEnabled && searchText.isNotEmpty()) {
                            if (isFavoriteEnabled) {
                                Column(modifier = Modifier.padding(horizontalPadding)) {
                                    Text(
                                        text = i18n("journal_no_results"),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = i18n(
                                            "journal_no_favorite_experience_titles_match_search"
                                        ),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            } else {
                                Column(modifier = Modifier.padding(horizontalPadding)) {
                                    Text(
                                        text = i18n("journal_no_results"),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = i18n("journal_no_experience_titles_match_search"),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
                val listState = rememberLazyListState()
                val isScrollUpButtonShown by remember {
                    derivedStateOf {
                        listState.firstVisibleItemIndex > 0
                    }
                }
                Box(contentAlignment = Alignment.TopEnd) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = bottomBarOverlayPadding()
                    ) {
                        if (experiences.isNotEmpty()) {
                            item {
                                HorizontalDivider()
                            }
                        }
                        items(
                            experiences,
                            key = { it.experience.id }
                        ) { experienceWithIngestions ->
                            ExperienceRow(
                                experienceWithIngestions,
                                navigateToExperienceScreen = {
                                    navigateToExperiencePopNothing(
                                        experienceWithIngestions.experience.id
                                    )
                                },
                                isTimeRelativeToNow = isTimeRelativeToNow,
                                substanceRepository = substanceRepository,
                                ownerUserName = ownerUserName
                            )
                            HorizontalDivider()
                        }
                    }
                    this@Column.AnimatedVisibility(visible = isScrollUpButtonShown) {
                        val scope = rememberCoroutineScope()
                        ElevatedButton(
                            modifier = Modifier.padding(all = horizontalPadding),
                            onClick = {
                                scope.launch {
                                    listState.scrollToItem(index = 0)
                                }
                            }) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Scroll to top")
                        }
                    }
                }
            }
            if (experiences.isEmpty() && !isSearchEnabled) {
                if (isFavoriteEnabled) {
                    EmptyScreenDisclaimer(
                        title = i18n("journal_no_favorites"),
                        description = i18n("journal_no_favorites_description")
                    )
                } else {
                    EmptyScreenDisclaimer(
                        title = i18n("journal_empty_title"),
                        description = i18n("journal_empty_description")
                    )
                }
            }
        }
    }
}
