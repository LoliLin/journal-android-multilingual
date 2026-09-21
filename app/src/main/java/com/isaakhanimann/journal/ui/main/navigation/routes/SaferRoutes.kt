/*
 * Copyright (c) 2022-2023. Isaak Hanimann.
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
import kotlinx.serialization.Serializable

/** Start destination of the safer-use tab. */
@Serializable
data object SaferRoute : NavKey

@Serializable
data object SaferHallucinogensRoute : NavKey

@Serializable
data object SaferStimulantsRoute : NavKey

@Serializable
data object DosageExplanationOnSaferTabRoute : NavKey

@Serializable
data object AdministrationRouteExplanationRoute : NavKey

@Serializable
data object DrugTestingRoute : NavKey

@Serializable
data object DosageGuideRoute : NavKey

@Serializable
data object VolumetricDosingOnSaferTabRoute : NavKey

@Serializable
data object ReagentTestingRoute : NavKey

@Serializable
data class SaferTabUrlRoute(val url: String) : NavKey
