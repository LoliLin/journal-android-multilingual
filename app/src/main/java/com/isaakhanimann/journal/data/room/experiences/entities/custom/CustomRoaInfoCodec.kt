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

package com.isaakhanimann.journal.data.room.experiences.entities.custom

import kotlinx.serialization.json.Json

/**
 * Encodes and decodes the [CustomRoaInfo] column.
 *
 * This deliberately works on `String` only and is not a Room `@TypeConverter`: Room runs its
 * annotation processor before the serialization plugin has generated `serializer()` for these
 * classes, and a converter mentioning them fails with a missing-type error. So the entity
 * stores the raw JSON text and calls in here explicitly.
 *
 * Decoding is forgiving — malformed or absent JSON yields an empty list, because losing dose
 * metadata must never make a substance (or the whole database read) fail.
 */
object CustomRoaInfoCodec {

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = kotlinx.serialization.builtins.ListSerializer(CustomRoaInfo.serializer())

    fun decode(raw: String?): List<CustomRoaInfo> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(emptyList())
    }

    fun encode(roaInfos: List<CustomRoaInfo>): String = json.encodeToString(serializer, roaInfos)
}
