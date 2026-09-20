/*
 * Copyright (c) 2022. Isaak Hanimann.
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

package com.isaakhanimann.journal.data.substances.parse

import org.json.JSONArray
import org.json.JSONObject

fun JSONObject.getOptionalJSONObject(name: String): JSONObject? = optJSONObject(name)

fun JSONObject.getOptionalString(name: String): String? =
    if (has(name) && !isNull(name)) optString(name) else null

fun JSONObject.getOptionalBoolean(name: String): Boolean? =
    if (has(name) && !isNull(name)) optBoolean(name) else null

fun JSONObject.getOptionalLong(name: String): Long? =
    if (has(name) && !isNull(name)) optLong(name) else null

fun JSONObject.getOptionalJSONArray(name: String): JSONArray? = optJSONArray(name)

fun JSONObject.getOptionalDouble(name: String): Double? =
    if (has(name) && !isNull(name)) {
        val d = optDouble(name)
        if (d.isNaN()) null else d
    } else {
        null
    }

fun JSONArray.getOptionalString(index: Int): String? =
    if (index in 0 until length() && !isNull(index)) optString(index) else null

fun JSONArray.getOptionalJSONObject(index: Int): JSONObject? = optJSONObject(index)
