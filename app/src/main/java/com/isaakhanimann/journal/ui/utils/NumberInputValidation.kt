package com.isaakhanimann.journal.ui.utils

internal fun parseNonNegativeFiniteNumber(value: String): Double? =
    value.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 }

internal fun parsePositiveFiniteNumber(value: String): Double? =
    value.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }

internal fun scaleDoseByUnit(inputDose: Double?, unitDose: Double?): Double? {
    val validInputDose = inputDose?.takeIf { it.isFinite() && it >= 0.0 } ?: return null
    val validUnitDose = unitDose?.takeIf { it.isFinite() && it > 0.0 } ?: return null
    return (validInputDose * validUnitDose).takeIf { it.isFinite() && it >= 0.0 }
}
