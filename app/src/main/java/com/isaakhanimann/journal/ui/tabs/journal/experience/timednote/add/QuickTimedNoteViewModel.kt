package com.isaakhanimann.journal.ui.tabs.journal.experience.timednote.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.entities.TimedNote
import com.isaakhanimann.journal.ui.main.navigation.routes.QuickTimedNoteRoute
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import kotlinx.coroutines.launch

/**
 * Minimal timed-note entry: current time, default color, on the timeline,
 * attached to the experience from the route. Zero configuration, one tap.
 */
@HiltViewModel(assistedFactory = QuickTimedNoteViewModel.Factory::class)
class QuickTimedNoteViewModel @AssistedInject constructor(
    private val experienceRepo: ExperienceRepository,
    @Assisted val route: QuickTimedNoteRoute
) : ViewModel() {
    var note by mutableStateOf("")
        private set
    val experienceId: Int = route.experienceId

    fun onChangeNote(newNote: String) {
        note = newNote
    }

    fun onDoneTap() {
        if (note.isBlank()) return
        val newTimedNote = TimedNote(
            time = Instant.now(),
            creationDate = Instant.now(),
            experienceId = experienceId,
            isPartOfTimeline = true,
            color = AdaptiveColor.BLUE,
            note = note.trim()
        )
        viewModelScope.launch {
            experienceRepo.insert(timedNote = newTimedNote)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(route: QuickTimedNoteRoute): QuickTimedNoteViewModel
    }
}
