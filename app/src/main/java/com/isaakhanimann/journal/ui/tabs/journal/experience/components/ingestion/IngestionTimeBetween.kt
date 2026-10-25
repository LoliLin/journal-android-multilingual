package com.isaakhanimann.journal.ui.tabs.journal.experience.components.ingestion

import com.isaakhanimann.journal.ui.tabs.journal.experience.components.getDurationText
import com.isaakhanimann.journal.ui.tabs.journal.experience.models.IngestionElement
import java.time.Instant

/**
 * The ingestion lists in the experience screen are already sorted and scoped by consumer.
 * Always look up the predecessor within the same displayed list.
 */
internal fun getPreviousIngestionTime(
    ingestionElements: List<IngestionElement>,
    index: Int
): Instant? = ingestionElements.getOrNull(index - 1)
    ?.ingestionWithCompanionAndCustomUnit?.ingestion?.time

/**
 * Null indicates that the first ingestion should keep its absolute clock time.
 */
internal fun getTimeBetweenIngestionsText(previousIngestionTime: Instant?, time: Instant): String? =
    previousIngestionTime?.let { "+ ${getDurationText(it, time)}" }
