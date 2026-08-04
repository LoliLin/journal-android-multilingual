package com.isaakhanimann.journal.ui.tabs.journal.experience.timednote.add

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FocusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.isaakhanimann.journal.localization.i18n

/**
 * One-tap timed note entry: a single focused text field and a done button.
 * The note is stamped with the current time and attached to the experience
 * passed via the route (from the effect notification or the journal bar).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTimedNoteScreen(
    viewModel: QuickTimedNoteViewModel = hiltViewModel(),
    navigateBack: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(i18n("quick_note_title")) },
                actions = {
                    IconButton(onClick = {
                        viewModel.onDoneTap()
                        navigateBack()
                    }) {
                        Icon(
                            Icons.Filled.Done,
                            contentDescription = i18n("common_done")
                        )
                    }
                }
            )
        }
    ) { padding ->
        TextField(
            value = viewModel.note,
            onValueChange = viewModel::onChangeNote,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .focusRequester(focusRequester),
            placeholder = { Text(i18n("quick_note_hint")) }
        )
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
