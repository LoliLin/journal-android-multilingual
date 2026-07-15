package com.isaakhanimann.journal.ui.tabs.search.substance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.*

@HiltViewModel
class UrlViewModel @Inject constructor(
    @ApplicationContext val appContext: Context,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val isOpenLinkInBrowserFlow = userPreferences.isOpenLinkInBrowserFlow.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )
}
