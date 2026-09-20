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

package com.isaakhanimann.journal.di

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.notifications.Notifications
import com.isaakhanimann.journal.ui.notifications.TimeCapsuleWorker
import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferences
import com.isaakhanimann.journal.ui.utils.DateFormat
import com.isaakhanimann.journal.ui.utils.TimeFormat
import com.isaakhanimann.journal.ui.widgets.StatsWidgetSync
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltAndroidApp
class JournalApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var userPreferences: UserPreferences

    /** Used by widgets, notifications, and config activity. */
    @Inject
    lateinit var substanceRepo: SubstanceRepository

    /** Used by the stats widget path (provider refresh + config activity). */
    @Inject
    lateinit var experienceRepository: ExperienceRepository

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        TimeFormat.refreshSystemDefault(this)
        applicationScope.launch {
            userPreferences.use24HourClockFlow.collect { TimeFormat.setUserOverride(it) }
        }
        applicationScope.launch {
            userPreferences.dateLocaleOptionFlow.collect { DateFormat.setOption(it) }
        }
        // Keep the desktop stats widget in sync: journal writes, the app language (widget
        // labels are localized) and extension-pack strings all trigger a refresh, so the
        // widget never waits for the hourly updatePeriod. Only the wiring lives here —
        // StatsWidgetSync owns a worker on its own off-main dispatcher, because building a
        // RemoteViews and calling updateAppWidget on this main-thread scope is exactly what
        // used to stall recomposition and navigation animations.
        StatsWidgetSync.start(
            appContext = this,
            experienceRepository = experienceRepository,
            // Any language emission means "the widget labels may be stale".
            languageChanges = userPreferences.selectedLanguageFlow.map { Unit }
        )
        Notifications.createChannels(this)
        // Daily time-capsule check: "this day last year".
        val request = PeriodicWorkRequestBuilder<TimeCapsuleWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            TIME_CAPSULE_WORK_NAME,
            // UPDATE so a future change to the worker spec (interval/constraints)
            // is picked up on the next launch instead of lingering on the old one.
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    companion object {
        private const val TIME_CAPSULE_WORK_NAME = "time_capsule_daily"
    }
}
