package com.isaakhanimann.journal.ui.tabs.journal.timecapsule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsTimedNotesAndRatings
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class TimeCapsuleViewModel @Inject constructor(
    private val experienceRepo: ExperienceRepository
) : ViewModel() {

    suspend fun loadLastYearExperiences(): List<ExperienceWithIngestionsTimedNotesAndRatings> =
        withContext(Dispatchers.IO) {
            val lastYear = LocalDate.now().minusYears(1)
            val from = lastYear.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
            val to = from.plus(1, ChronoUnit.DAYS)
            experienceRepo.getExperiencesWithIngestionsTimedNotesAndRatingsInRange(from, to)
        }
}
