package com.isaakhanimann.journal.ui.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Daily check: if anything was recorded exactly one year ago today, post the
 * "time capsule" notification. Idempotent (fixed notification id, one per day).
 */
@HiltWorker
class TimeCapsuleWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val experienceRepo: ExperienceRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val lastYear = LocalDate.now().minusYears(1)
            val from = lastYear.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
            val to = from.plus(1, ChronoUnit.DAYS)
            val experiences =
                experienceRepo.getExperiencesWithIngestionsTimedNotesAndRatingsInRange(from, to)
            if (experiences.isNotEmpty()) {
                Notifications.showTimeCapsuleNotification(
                    applicationContext,
                    experiences.size
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
