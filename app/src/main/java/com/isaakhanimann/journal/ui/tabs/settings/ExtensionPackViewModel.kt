package com.isaakhanimann.journal.ui.tabs.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.achievement.AchievementUnlocker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ExtensionPackViewModel @Inject constructor(
    private val achievementUnlocker: AchievementUnlocker
) : ViewModel() {
    fun onImportSucceeded() {
        viewModelScope.launch {
            achievementUnlocker.unlock(AchievementUnlocker.LOCAL_BREWERY)
        }
    }
}
