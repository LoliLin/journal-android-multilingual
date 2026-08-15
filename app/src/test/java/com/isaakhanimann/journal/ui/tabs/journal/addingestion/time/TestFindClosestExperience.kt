/*
 * Copyright (c) 2022-2026. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.tabs.journal.addingestion.time

import com.isaakhanimann.journal.data.room.experiences.entities.Experience
import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestions
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TestFindClosestExperience {

    private val zone: ZoneId = ZoneId.of("UTC")

    private fun instantOf(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0): Instant =
        LocalDateTime.of(year, month, day, hour, minute).atZone(zone).toInstant()

    private fun experience(id: Int, ingestionTimes: List<Instant>): ExperienceWithIngestions {
        val experience = Experience(
            id = id,
            title = "Experience $id",
            text = "",
            creationDate = ingestionTimes.first(),
            sortDate = ingestionTimes.first(),
            location = null
        )
        val ingestions = ingestionTimes.map { time ->
            Ingestion(
                substanceName = "LSD",
                time = time,
                creationDate = null,
                administrationRoute = AdministrationRoute.ORAL,
                dose = null,
                isDoseAnEstimate = false,
                estimatedDoseStandardDeviation = null,
                units = null,
                experienceId = id,
                notes = null,
                stomachFullness = null,
                consumerName = null,
                customUnitId = null
            )
        }
        return ExperienceWithIngestions(experience = experience, ingestions = ingestions)
    }

    @Test
    fun postMidnightIngestionAfterDaytimeSessionStartsNewExperience() {
        val previousDay = experience(
            id = 1,
            ingestionTimes = listOf(
                instantOf(2026, 8, 14, 12),
                instantOf(2026, 8, 14, 16),
                instantOf(2026, 8, 14, 20)
            )
        )
        val result = findClosestExperience(
            experiences = listOf(previousDay),
            selectedInstant = instantOf(2026, 8, 15, 0, 30),
            zone = zone
        )
        assertNull(result)
    }

    @Test
    fun postMidnightIngestionAfterLateNightSessionStartsNewExperience() {
        // last ingestion 23:00 is within the 3h-continuity bound, but a new calendar day started
        val previousDay = experience(
            id = 1,
            ingestionTimes = listOf(
                instantOf(2026, 8, 14, 10),
                instantOf(2026, 8, 14, 23)
            )
        )
        val result = findClosestExperience(
            experiences = listOf(previousDay),
            selectedInstant = instantOf(2026, 8, 15, 0, 30),
            zone = zone
        )
        assertNull(result)
    }

    @Test
    fun ingestionOnSameDayAsSessionAttaches() {
        val experience1 = experience(
            id = 1,
            ingestionTimes = listOf(
                instantOf(2026, 8, 14, 12),
                instantOf(2026, 8, 14, 16),
                instantOf(2026, 8, 14, 20)
            )
        )
        val result = findClosestExperience(
            experiences = listOf(experience1),
            selectedInstant = instantOf(2026, 8, 14, 18),
            zone = zone
        )
        assertEquals(1, result?.experience?.id)
    }

    @Test
    fun ingestionSlightlyBeforeFirstIngestionAttaches() {
        val experience1 = experience(id = 1, ingestionTimes = listOf(instantOf(2026, 8, 14, 22)))
        val result = findClosestExperience(
            experiences = listOf(experience1),
            selectedInstant = instantOf(2026, 8, 14, 20),
            zone = zone
        )
        assertEquals(1, result?.experience?.id)
    }

    @Test
    fun midnightCrossingSessionContinues() {
        val experience1 = experience(
            id = 1,
            ingestionTimes = listOf(instantOf(2026, 8, 14, 22), instantOf(2026, 8, 15, 1))
        )
        val result = findClosestExperience(
            experiences = listOf(experience1),
            selectedInstant = instantOf(2026, 8, 15, 2, 30),
            zone = zone
        )
        assertEquals(1, result?.experience?.id)
    }

    @Test
    fun singleLateNightDoseDoesNotAbsorbPostMidnightRedose() {
        // a session that has not crossed midnight does not absorb ingestions of the next day
        val experience1 = experience(id = 1, ingestionTimes = listOf(instantOf(2026, 8, 14, 22)))
        val result = findClosestExperience(
            experiences = listOf(experience1),
            selectedInstant = instantOf(2026, 8, 15, 1),
            zone = zone
        )
        assertNull(result)
    }

    @Test
    fun muchLaterNextDayStartsNewExperience() {
        val previousDay = experience(
            id = 1,
            ingestionTimes = listOf(instantOf(2026, 8, 14, 12), instantOf(2026, 8, 14, 16))
        )
        val result = findClosestExperience(
            experiences = listOf(previousDay),
            selectedInstant = instantOf(2026, 8, 15, 14),
            zone = zone
        )
        assertNull(result)
    }

    @Test
    fun laterSameDayAttachesToNewDayExperienceWhenBothInRange() {
        val previousDay = experience(
            id = 1,
            ingestionTimes = listOf(instantOf(2026, 8, 14, 12), instantOf(2026, 8, 14, 20))
        )
        val newDay = experience(id = 2, ingestionTimes = listOf(instantOf(2026, 8, 15, 0, 30)))
        // the repository returns the newest sortDate first
        val result = findClosestExperience(
            experiences = listOf(newDay, previousDay),
            selectedInstant = instantOf(2026, 8, 15, 14),
            zone = zone
        )
        assertEquals(2, result?.experience?.id)
    }

    @Test
    fun experienceWithoutIngestionsNeverMatches() {
        val empty = ExperienceWithIngestions(
            experience = Experience(
                id = 3,
                title = "empty",
                text = "",
                sortDate = instantOf(2026, 8, 14, 12),
                location = null
            ),
            ingestions = emptyList()
        )
        val result = findClosestExperience(
            experiences = listOf(empty),
            selectedInstant = instantOf(2026, 8, 14, 13),
            zone = zone
        )
        assertNull(result)
    }
}
