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

fun JSONObject.getOptionalString(name: String): String? {
    val v = opt(name) ?: return null
    if (v === JSONObject.NULL) return null
    return when (v) {
        is String -> v
        is Number, is Boolean -> v.toString()
        else -> null
    }
}

fun JSONObject.getOptionalBoolean(name: String): Boolean? {
    val v = opt(name) ?: return null
    if (v === JSONObject.NULL) return null
    return when (v) {
        is Boolean -> v
        is String -> when {
            v.equals("true", ignoreCase = true) -> true
            v.equals("false", ignoreCase = true) -> false
            else -> null
        }
        else -> null
    }
}

fun JSONObject.getOptionalLong(name: String): Long? {
    val v = opt(name) ?: return null
    if (v === JSONObject.NULL) return null
    return when (v) {
        is Number -> v.toLong()
        is String -> v.toLongOrNull()
        else -> null
    }
}

fun JSONObject.getOptionalJSONArray(name: String): JSONArray? = optJSONArray(name)

fun JSONObject.getOptionalDouble(name: String): Double? {
    val v = opt(name) ?: return null
    if (v === JSONObject.NULL) return null
    return when (v) {
        is Number -> {
            val d = v.toDouble()
            if (d.isNaN()) null else d
        }
        is String -> v.toDoubleOrNull()
        else -> null
    }
}

fun JSONArray.getOptionalString(index: Int): String? {
    if (index !in 0 until length() || isNull(index)) return null
    val v = opt(index) ?: return null
    if (v === JSONObject.NULL) return null
    return when (v) {
        is String -> v
        is Number, is Boolean -> v.toString()
        else -> null
    }
}

fun JSONArray.getOptionalJSONObject(index: Int): JSONObject? = optJSONObject(index)
