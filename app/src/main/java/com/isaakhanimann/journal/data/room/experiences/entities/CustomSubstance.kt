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

package com.isaakhanimann.journal.data.room.experiences.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.isaakhanimann.journal.data.room.experiences.entities.custom.CustomRoaInfo
import com.isaakhanimann.journal.data.room.experiences.entities.custom.CustomRoaInfoCodec
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Entity
@Serializable
data class CustomSubstance(
    @Transient
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    var units: String,
    var description: String,
    /**
     * Per-route dose and duration knowledge, serialized as JSON.
     *
     * Stored as plain text rather than a typed list because Room's processor cannot resolve
     * these serializable types; see [CustomRoaInfoCodec]. Use [roaInfos] to read it.
     */
    @ColumnInfo(defaultValue = "[]")
    var roaInfosJson: String = "[]"
) {
    /** Dose info for this substance, decoded from [roaInfosJson]. */
    val roaInfos: List<CustomRoaInfo> get() = CustomRoaInfoCodec.decode(roaInfosJson)

    fun withRoaInfos(newRoaInfos: List<CustomRoaInfo>): CustomSubstance =
        copy(roaInfosJson = CustomRoaInfoCodec.encode(newRoaInfos))
}
