/*
 * Copyright (c) 2023. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.main.navigation.routes

import com.isaakhanimann.journal.data.substances.AdministrationRoute
import kotlinx.serialization.Serializable

/** Start destination of the settings tab. */
@Serializable
data object SettingsRoute

@Serializable
data object FAQRoute

@Serializable
data object DonateRoute

@Serializable
data object PreferencesRoute

@Serializable
data object IconPickerRoute

@Serializable
data object ExtensionPackRoute

@Serializable
data object CombinationSettingsRoute

@Serializable
data object SubstanceColorsRoute

@Serializable
data object CustomUnitsRoute

@Serializable
data object CustomUnitArchiveRoute

@Serializable
data class EditCustomUnitRoute(val customUnitId: Int)

// ---------------------------------------------------------------------------------------------
// Add-custom-unit flow: a nested graph on the settings tab
// ---------------------------------------------------------------------------------------------

/** Route of the nested add-custom-unit graph; navigating here starts the flow. */
@Serializable
data object AddCustomUnitsRoute

/** Start destination of the add-custom-unit flow. */
@Serializable
data object AddCustomUnitsSearchSubstanceRoute

@Serializable
data class ChooseRouteOfAddCustomUnitRoute(val substanceName: String)

@Serializable
data class FinishAddCustomUnitRoute(
    val substanceName: String,
    val administrationRoute: AdministrationRoute
)
