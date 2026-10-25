/*
 * Copyright (c) 2026.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 */

package com.isaakhanimann.journal.ui.tabs.settings

import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull

/**
 * Import-only compatibility for backups created by forks that store an ARGB Int in
 * `customColor` and set the legacy `color` enum to null.
 *
 * The local data model and export format remain unchanged. Custom colors are
 * approximated with the closest existing AdaptiveColor, so the original ARGB
 * cannot be recovered after import.
 */
internal fun decodeCompatibleJournalImport(text: String): JournalExport {
    // Keep the regular, streaming decoder for backups without custom colors.
    // If a different fork wrote null/unknown predefined colors, retry after
    // normalizing only the two color-bearing sections.
    if (!text.contains("\"customColor\"")) {
        try {
            return journalImportJson.decodeFromString(text)
        } catch (originalError: SerializationException) {
            val parsed = journalImportJson.parseToJsonElement(text)
            val normalized = normalizeImportedColors(parsed)
            if (normalized == parsed) throw originalError
            return journalImportJson.decodeFromJsonElement(normalized)
        }
    }

    val parsed = journalImportJson.parseToJsonElement(text)
    return journalImportJson.decodeFromJsonElement(normalizeImportedColors(parsed))
}

private val supportedColors = AdaptiveColor.entries.map { it.name }.toSet()

private fun normalizeImportedColors(element: JsonElement): JsonElement {
    val root = element as? JsonObject ?: return element
    val updated = root.toMutableMap()

    (root["substanceCompanions"] as? JsonArray)?.let { companions ->
        val adapted = JsonArray(companions.map { (it as? JsonObject)?.let(::normalizeColor) ?: it })
        if (adapted != companions) updated["substanceCompanions"] = adapted
    }

    (root["experiences"] as? JsonArray)?.let { experiences ->
        val adapted = JsonArray(experiences.map { experience ->
            val record = experience as? JsonObject ?: return@map experience
            val notes = record["timedNotes"] as? JsonArray ?: return@map experience
            val adaptedNotes = JsonArray(notes.map { (it as? JsonObject)?.let(::normalizeColor) ?: it })
            if (adaptedNotes == notes) experience
            else JsonObject(record + ("timedNotes" to adaptedNotes))
        })
        if (adapted != experiences) updated["experiences"] = adapted
    }

    return if (updated == root) root else JsonObject(updated)
}

private fun normalizeColor(record: JsonObject): JsonObject {
    val current = (record["color"] as? JsonPrimitive)
        ?.takeIf { it.isString }?.content
    val customArgb = (record["customColor"] as? JsonPrimitive)?.intOrNull
    val normalized = when {
        customArgb != null -> closestPresetColor(customArgb).name
        current !in supportedColors -> AdaptiveColor.BLUE.name
        else -> current
    }

    return if (normalized == current) record
    else JsonObject(record + ("color" to JsonPrimitive(normalized)))
}

/** Android ARGB integers are signed; use unsigned shifts to extract the RGB channels. */
private fun closestPresetColor(argb: Int): AdaptiveColor {
    val red = (argb ushr 16) and 0xFF
    val green = (argb ushr 8) and 0xFF
    val blue = argb and 0xFF

    return AdaptiveColor.entries.minBy { preset ->
        val color = preset.getComposeColor(isDarkTheme = false)
        val dr = red - color.red * 255f
        val dg = green - color.green * 255f
        val db = blue - color.blue * 255f
        dr * dr + dg * dg + db * db
    }
}
