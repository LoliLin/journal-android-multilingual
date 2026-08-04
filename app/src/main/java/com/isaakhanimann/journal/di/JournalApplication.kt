package com.isaakhanimann.journal.di

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.isaakhanimann.journal.ui.notifications.Notifications
import com.isaakhanimann.journal.ui.notifications.TimeCapsuleWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class JournalApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        Notifications.createChannels(this)
        // Daily time-capsule check: "this day last year".
        val request = PeriodicWorkRequestBuilder<TimeCapsuleWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            TIME_CAPSULE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    companion object {
        private const val TIME_CAPSULE_WORK_NAME = "time_capsule_daily"
    }
}
