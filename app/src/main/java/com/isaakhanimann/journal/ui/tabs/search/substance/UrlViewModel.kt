package com.isaakhanimann.journal.ui.tabs.search.substance

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UrlViewModel @Inject constructor(
    @ApplicationContext val appContext: Context,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    val isOpenLinkInBrowserFlow = userPreferences.isOpenLinkInBrowser.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

}