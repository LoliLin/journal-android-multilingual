package com.isaakhanimann.journal.ui.tabs.journal.experience.editingestion

import com.isaakhanimann.journal.ui.utils.parseNonNegativeFiniteNumber
import java.time.LocalDateTime
import java.time.ZoneId

internal fun isDoseInputValid(isKnown: Boolean, dose: String): Boolean =
    !isKnown || parseNonNegativeFiniteNumber(dose) != null

internal fun isEstimatedDeviationInputValid(
    isKnown: Boolean,
    isEstimate: Boolean,
    deviation: String,
): Boolean =
    !isKnown || !isEstimate || deviation.isBlank() || parseNonNegativeFiniteNumber(deviation) != null

internal fun isTimeRangeInputValid(
    start: LocalDateTime,
    end: LocalDateTime,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Boolean {
    val startInstant = start.atZone(zoneId).toInstant()
    val endInstant = end.atZone(zoneId).toInstant()
    return !endInstant.isBefore(startInstant)
}
