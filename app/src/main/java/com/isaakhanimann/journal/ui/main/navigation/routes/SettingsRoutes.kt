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

import androidx.navigation3.runtime.NavKey
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import kotlinx.serialization.Serializable

/** Start destination of the settings tab. */
@Serializable
data object SettingsRoute : NavKey

@Serializable
data object FAQRoute : NavKey

@Serializable
data object DonateRoute : NavKey

@Serializable
data object PreferencesRoute : NavKey

@Serializable
data object IconPickerRoute : NavKey

@Serializable
data object ExtensionPackRoute : NavKey

@Serializable
data object CombinationSettingsRoute : NavKey

@Serializable
data object SubstanceColorsRoute : NavKey

@Serializable
data object CustomUnitsRoute : NavKey

@Serializable
data object CustomUnitArchiveRoute : NavKey

@Serializable
data class EditCustomUnitRoute(val customUnitId: Int) : NavKey

// ---------------------------------------------------------------------------------------------
// Add-custom-unit flow: screens the settings tab pushes on top of its root
// ---------------------------------------------------------------------------------------------

/** First screen of the add-custom-unit flow; navigating here starts the flow. */
@Serializable
data object AddCustomUnitsRoute : NavKey

@Serializable
data class ChooseRouteOfAddCustomUnitRoute(val substanceName: String) : NavKey

@Serializable
data class FinishAddCustomUnitRoute(
    val substanceName: String,
    val administrationRoute: AdministrationRoute
) : NavKey
