/*
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

package com.isaakhanimann.journal.ui.tabs.search.custom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.data.room.experiences.entities.custom.CustomDoseInfo
import com.isaakhanimann.journal.data.room.experiences.entities.custom.hasValidThresholds
import com.isaakhanimann.journal.data.room.experiences.entities.custom.CustomRoaInfo
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.ui.utils.parsePositiveFiniteNumber
import com.isaakhanimann.journal.localization.i18n

/**
 * Edits the dose table for one route at a time of a custom substance.
 *
 * Every edited route is retained, so filling in oral first and then insufflated keeps both.
 * Only thresholds are collected: a custom substance rarely has the full dose ladder, and an
 * empty field simply means "unknown", which the dose resolver already tolerates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDoseInfoDialog(
    roaInfos: List<CustomRoaInfo>,
    initialRoute: AdministrationRoute,
    onDismiss: () -> Unit,
    onConfirm: (List<CustomRoaInfo>) -> Unit
) {
    // The committed table for every route that already had dose info.
    var doseInfosByRoute by remember {
        mutableStateOf(
            roaInfos.mapNotNull { info ->
                info.doseInfo?.let { doseInfo -> info.administrationRoute to doseInfo }
            }.toMap()
        )
    }
    var selectedRoute by remember { mutableStateOf(initialRoute) }
    var isRouteMenuExpanded by remember { mutableStateOf(false) }

    // Raw text of the route currently open in the editor.
    var units by remember { mutableStateOf(doseInfosByRoute[initialRoute]?.units ?: "") }
    var lightMin by remember {
        mutableStateOf(doseInfosByRoute[initialRoute]?.lightMin?.toPlainString() ?: "")
    }
    var commonMin by remember {
        mutableStateOf(doseInfosByRoute[initialRoute]?.commonMin?.toPlainString() ?: "")
    }
    var strongMin by remember {
        mutableStateOf(doseInfosByRoute[initialRoute]?.strongMin?.toPlainString() ?: "")
    }
    var heavyMin by remember {
        mutableStateOf(doseInfosByRoute[initialRoute]?.heavyMin?.toPlainString() ?: "")
    }

    fun currentFieldsAsDoseInfo() = CustomDoseInfo(
        units = units.trim().ifEmpty { null },
        lightMin = parsePositiveFiniteNumber(lightMin),
        commonMin = parsePositiveFiniteNumber(commonMin),
        strongMin = parsePositiveFiniteNumber(strongMin),
        heavyMin = parsePositiveFiniteNumber(heavyMin)
    )

    fun currentFieldsAreValid(): Boolean {
        val rawThresholds = listOf(lightMin, commonMin, strongMin, heavyMin)
        if (rawThresholds.any { it.isNotBlank() && parsePositiveFiniteNumber(it) == null }) return false
        return currentFieldsAsDoseInfo().hasValidThresholds()
    }

    fun allDoseInfoIsValid(): Boolean = currentFieldsAreValid() &&
        doseInfosByRoute.filterKeys { it != selectedRoute }.values.all { it.hasValidThresholds() }

    val currentThresholdValues = listOf(lightMin, commonMin, strongMin, heavyMin)
        .map { parsePositiveFiniteNumber(it) }
    val currentThresholdsAreOrdered = currentThresholdValues.filterNotNull()
        .zipWithNext().all { (lower, upper) -> lower < upper }
    val currentThresholdsNeedUnits = currentThresholdValues.any { it != null } && units.isBlank()
    fun isThresholdFieldError(text: String, value: Double?): Boolean =
        (text.isNotBlank() && value == null) ||
            (value != null && (!currentThresholdsAreOrdered || currentThresholdsNeedUnits))

    /** Swaps the editor over to [route], banking whatever was typed for the previous one. */
    fun switchTo(route: AdministrationRoute): Boolean {
        if (route == selectedRoute) return true
        if (!currentFieldsAreValid()) return false
        doseInfosByRoute = doseInfosByRoute + (selectedRoute to currentFieldsAsDoseInfo())
        selectedRoute = route
        val info = doseInfosByRoute[route]
        units = info?.units ?: ""
        lightMin = info?.lightMin?.toPlainString() ?: ""
        commonMin = info?.commonMin?.toPlainString() ?: ""
        strongMin = info?.strongMin?.toPlainString() ?: ""
        heavyMin = info?.heavyMin?.toPlainString() ?: ""
        return true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(i18n("custom_substance_dose_info")) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = isRouteMenuExpanded,
                    onExpandedChange = { isRouteMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedRoute.displayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(i18n("administration_route")) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = isRouteMenuExpanded
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = isRouteMenuExpanded,
                        onDismissRequest = { isRouteMenuExpanded = false }
                    ) {
                        AdministrationRoute.entries.forEach { route ->
                            DropdownMenuItem(
                                text = { Text(route.displayName()) },
                                onClick = {
                                    if (switchTo(route)) isRouteMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = units,
                    onValueChange = { units = it },
                    label = { Text(i18n("dose_units_label")) },
                    isError = currentThresholdsNeedUnits,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThresholdField(
                        label = i18n("dose_class_threshold"),
                        value = lightMin,
                        onValueChange = { lightMin = it },
                        isError = isThresholdFieldError(lightMin, currentThresholdValues[0]),
                        modifier = Modifier.weight(1f)
                    )
                    ThresholdField(
                        label = i18n("dose_class_common"),
                        value = commonMin,
                        onValueChange = { commonMin = it },
                        isError = isThresholdFieldError(commonMin, currentThresholdValues[1]),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThresholdField(
                        label = i18n("dose_class_strong"),
                        value = strongMin,
                        onValueChange = { strongMin = it },
                        isError = isThresholdFieldError(strongMin, currentThresholdValues[2]),
                        modifier = Modifier.weight(1f)
                    )
                    ThresholdField(
                        label = i18n("dose_class_heavy"),
                        value = heavyMin,
                        onValueChange = { heavyMin = it },
                        isError = isThresholdFieldError(heavyMin, currentThresholdValues[3]),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (allDoseInfoIsValid()) {
                        val edited = doseInfosByRoute + (selectedRoute to currentFieldsAsDoseInfo())
                        var result = roaInfos
                        for ((route, info) in edited) {
                            result = result.withDoseInfo(route, info)
                        }
                        onConfirm(result)
                    }
                },
                enabled = allDoseInfoIsValid()
            ) {
                Text(i18n("common_done"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(i18n("common_cancel")) }
        }
    )
}

@Composable
private fun ThresholdField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}

/**
 * Merges an edited dose table into the route list, replacing the entry for that route while
 * keeping every other route and its duration info untouched.
 */
fun List<CustomRoaInfo>.withDoseInfo(
    route: AdministrationRoute,
    doseInfo: CustomDoseInfo
): List<CustomRoaInfo> {
    val existing = firstOrNull { it.administrationRoute == route }
    val updated = (existing ?: CustomRoaInfo(administrationRoute = route))
        .copy(doseInfo = doseInfo)
    return filterNot { it.administrationRoute == route } + updated
}

/** Renders an optional double without a trailing `.0`, so fields round-trip cleanly. */
private fun Double.toPlainString(): String = toString().removeSuffix(".0")

/** Human-readable route name as declared by the route itself. */
private fun AdministrationRoute.displayName(): String = displayText
