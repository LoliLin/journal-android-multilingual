package com.isaakhanimann.journal.data.achievement

import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferences
import javax.inject.Inject

class AchievementUnlocker @Inject constructor(
    private val userPreferences: UserPreferences,
    private val definitionsLoader: AchievementDefinitionsLoader
) {
    suspend fun unlock(registerName: String) {
        if (definitionsLoader.definitions.none { it.registerName == registerName }) return
        if (userPreferences.addAchievement(registerName)) {
            AchievementEventBus.send(registerName)
        }
    }

    companion object {
        const val LOCAL_BREWERY = "local_brewery"
    }
}
