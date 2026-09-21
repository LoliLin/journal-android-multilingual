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

/**
 * Base paths for the externally reachable `journal://` deep links.
 *
 * Navigation 3 matches these intents in MainScreen and converts the path segments to route keys.
 * Keeping the constants beside the route definitions avoids duplicating the public URI contract.
 */
private const val DEEP_LINK_ROOT = "journal://open"

const val DEEP_LINK_JOURNAL = "$DEEP_LINK_ROOT/journal"
const val DEEP_LINK_STATS = "$DEEP_LINK_ROOT/stats"
const val DEEP_LINK_SUBSTANCES = "$DEEP_LINK_ROOT/substances"
const val DEEP_LINK_SAFER = "$DEEP_LINK_ROOT/safer"
const val DEEP_LINK_SETTINGS = "$DEEP_LINK_ROOT/settings"
const val DEEP_LINK_SUBSTANCE = "$DEEP_LINK_ROOT/substance"
const val DEEP_LINK_SUBSTANCE_COMPANION = "$DEEP_LINK_ROOT/substance-companion"
const val DEEP_LINK_CATEGORY = "$DEEP_LINK_ROOT/category"
const val DEEP_LINK_EXPERIENCE = "$DEEP_LINK_ROOT/experience"
const val DEEP_LINK_QUICK_NOTE = "$DEEP_LINK_ROOT/quick-note"
const val DEEP_LINK_TIME_CAPSULE = "$DEEP_LINK_ROOT/time-capsule"
const val DEEP_LINK_ADD_INGESTION = "$DEEP_LINK_ROOT/add-ingestion"
const val DEEP_LINK_CHOOSE_ROUTE = "$DEEP_LINK_ROOT/choose-route"
