/*
 * Fork feature: process-wide signal emitted by ExperienceRepository writes.
 * Lets UI-layer listeners (e.g. the notification timeline refresher) react to
 * journal data changes without the data layer depending on UI code.
 */
package com.isaakhanimann.journal.data.room.experiences

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object JournalDataEvents {
    private val _journalChangeSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val journalChangeSignal = _journalChangeSignal.asSharedFlow()

    fun notifyJournalChanged() {
        _journalChangeSignal.tryEmit(Unit)
    }
}
