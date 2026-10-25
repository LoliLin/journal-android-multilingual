package com.isaakhanimann.journal.ui.tabs.journal.experience.components.ingestion

import com.isaakhanimann.journal.ui.tabs.journal.experience.components.TimeDisplayOption
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.getDurationText
import java.time.Instant

/**
 * The caller must supply the previous ingestion from the same chronologically sorted
 * display list. Null or out-of-order timestamps have no meaningful elapsed interval.
 */
internal fun getTimeBetweenIngestionsText(previousIngestionTime: Instant?, time: Instant): String? =
    previousIngestionTime
        ?.takeUnless { time.isBefore(it) }
        ?.let { "+ ${getDurationText(it, time)}" }

/**
 * Without a valid predecessor, TIME_BETWEEN explicitly displays a clock time.
 * Other time display modes must not be changed.
 */
internal fun resolveIngestionTimeDisplayOption(
    selectedOption: TimeDisplayOption,
    intervalText: String?
): TimeDisplayOption =
    if (selectedOption == TimeDisplayOption.TIME_BETWEEN && intervalText == null) {
        TimeDisplayOption.REGULAR
    } else {
        selectedOption
    }
