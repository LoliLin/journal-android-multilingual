package com.isaakhanimann.journal.ui.tabs.journal.experience.timednote.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.entities.TimedNote
import com.isaakhanimann.journal.ui.main.navigation.routers.EXPERIENCE_ID_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Minimal timed-note entry: current time, default color, on the timeline,
 * attached to the experience from the route. Zero configuration, one tap.
 */
@HiltViewModel
class QuickTimedNoteViewModel @Inject constructor(
    private val experienceRepo: ExperienceRepository,
    state: SavedStateHandle
) : ViewModel() {
    var note by mutableStateOf("")
        private set
    // Null-safe: a missing/unsurvivable navigation argument must not crash the app
    // (e.g. process death while the route was being rebuilt).
    val experienceId: Int? = state.get<Int>(EXPERIENCE_ID_KEY)

    fun onChangeNote(newNote: String) {
        note = newNote
    }

    fun onDoneTap() {
        val targetExperienceId = experienceId ?: return
        if (note.isBlank()) return
        val newTimedNote = TimedNote(
            time = Instant.now(),
            creationDate = Instant.now(),
            experienceId = targetExperienceId,
            isPartOfTimeline = true,
            color = AdaptiveColor.BLUE,
            note = note.trim()
        )
        viewModelScope.launch {
            experienceRepo.insert(timedNote = newTimedNote)
        }
    }
}
