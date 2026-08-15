/*
 * Copyright (c) 2022-2026. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.tabs.journal.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import com.isaakhanimann.journal.ui.utils.getDateWithWeekdayText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    navigateToExperience: (Int) -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val notes = viewModel.notesFlow.collectAsState().value
    val searchText = viewModel.searchTextFlow.collectAsState().value
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(i18n("notes_title")) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = viewModel::onSearchChange,
                singleLine = true,
                label = { Text(i18n("notes_search_hint")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 8.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (notes.isEmpty()) {
                    item {
                        Text(
                            text = i18n("notes_empty"),
                            modifier = Modifier.padding(horizontalPadding)
                        )
                    }
                }
                items(notes, key = { it.ingestionId }) { note ->
                    NoteRow(
                        note = note,
                        substanceDisplayName = viewModel.substanceRepo.getDisplayName(note.substanceName),
                        onClick = { navigateToExperience(note.experienceId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteRow(
    note: NoteRowData,
    substanceDisplayName: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = substanceDisplayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = note.time.getDateWithWeekdayText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(text = note.note, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
