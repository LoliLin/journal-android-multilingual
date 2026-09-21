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

/** Start destination of the substances tab. */
@Serializable
data object SubstancesRoute : NavKey

@Serializable
data class SubstanceRoute(val substanceName: String) : NavKey

@Serializable
data class CategoryRoute(val categoryName: String) : NavKey

@Serializable
data class EditCustomSubstanceRoute(val customSubstanceId: Int) : NavKey

@Serializable
data object AddCustomSubstanceRoute : NavKey

@Serializable
data object ExplainTimelineOnSubstancesTabRoute : NavKey

@Serializable
data object DosageExplanationOnSubstancesTabRoute : NavKey

@Serializable
data object VolumetricDosingOnSubstancesTabRoute : NavKey

// The safer-use articles are reachable from both the substances tab and the safer tab. Each tab
// needs its own destination class, because the route string is derived from the class name.
@Serializable
data object SaferHallucinogensOnSubstancesTabRoute : NavKey

@Serializable
data object SaferStimulantsOnSubstancesTabRoute : NavKey

@Serializable
data class SubstancesTabUrlRoute(val url: String) : NavKey
