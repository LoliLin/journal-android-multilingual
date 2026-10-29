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

package com.isaakhanimann.journal.data.room.experiences.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A photo attached to a timed note.
 *
 * [filePath] points into the app's internal storage, so the image survives even when the
 * original content URI is revoked. Deleting the parental [TimedNote] cascades to its photos.
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = TimedNote::class,
            parentColumns = ["id"],
            childColumns = ["timedNoteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["timedNoteId"])]
)
data class TimedNotePhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timedNoteId: Int,
    val filePath: String,
    val creationDate: Instant = Instant.now(),
    val caption: String? = null
)
