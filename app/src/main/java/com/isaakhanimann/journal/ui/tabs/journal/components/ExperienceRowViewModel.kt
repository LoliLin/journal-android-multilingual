
package com.isaakhanimann.journal.ui.tabs.journal.components

import androidx.compose.foundation.layout.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.TimedNote
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.interactions.InteractionChecker
import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ExperienceRowViewModel @Inject constructor(
    val substanceRepo: SubstanceRepository,
    val interactionChecker: InteractionChecker,
    val experienceRepo: ExperienceRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    fun getTimedNotes(experienceId: Int): Flow<List<TimedNote>> =
        experienceRepo.getTimedNotesFlowSorted(experienceId)

    val achievementsFlow = userPreferences.achievementsFlow.stateIn(
        initialValue = emptyList(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )
}
