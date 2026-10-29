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
import com.isaakhanimann.journal.data.room.experiences.entities.custom.CustomRoaInfo
import com.isaakhanimann.journal.data.substances.AdministrationRoute
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
        units = units.ifBlank { null },
        lightMin = lightMin.toDoubleOrNull(),
        commonMin = commonMin.toDoubleOrNull(),
        strongMin = strongMin.toDoubleOrNull(),
        heavyMin = heavyMin.toDoubleOrNull()
    )

    /** Swaps the editor over to [route], banking whatever was typed for the previous one. */
    fun switchTo(route: AdministrationRoute) {
        if (route == selectedRoute) return
        doseInfosByRoute = doseInfosByRoute + (selectedRoute to currentFieldsAsDoseInfo())
        selectedRoute = route
        val info = doseInfosByRoute[route]
        units = info?.units ?: ""
        lightMin = info?.lightMin?.toPlainString() ?: ""
        commonMin = info?.commonMin?.toPlainString() ?: ""
        strongMin = info?.strongMin?.toPlainString() ?: ""
        heavyMin = info?.heavyMin?.toPlainString() ?: ""
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
                                    switchTo(route)
                                    isRouteMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = units,
                    onValueChange = { units = it },
                    label = { Text(i18n("dose_units_label")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThresholdField(
                        label = i18n("dose_class_threshold"),
                        value = lightMin,
                        onValueChange = { lightMin = it },
                        modifier = Modifier.weight(1f)
                    )
                    ThresholdField(
                        label = i18n("dose_class_common"),
                        value = commonMin,
                        onValueChange = { commonMin = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThresholdField(
                        label = i18n("dose_class_strong"),
                        value = strongMin,
                        onValueChange = { strongMin = it },
                        modifier = Modifier.weight(1f)
                    )
                    ThresholdField(
                        label = i18n("dose_class_heavy"),
                        value = heavyMin,
                        onValueChange = { heavyMin = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val edited = doseInfosByRoute + (selectedRoute to currentFieldsAsDoseInfo())
                    var result = roaInfos
                    for ((route, info) in edited) {
                        result = result.withDoseInfo(route, info)
                    }
                    onConfirm(result)
                }
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
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
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
private fun Double.toPlainString(): String =
    if (this % 1.0 == 0.0) toLong().toString() else toString()

/** Human-readable route name as declared by the route itself. */
private fun AdministrationRoute.displayName(): String = displayText
