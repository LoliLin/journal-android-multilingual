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

package com.isaakhanimann.journal

import com.isaakhanimann.journal.data.room.experiences.entities.Experience
import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsCompanionsAndRatings
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithCompanionAndCustomUnit
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.ui.tabs.journal.matchesAllTerms
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Multi-term journal search: every whitespace-separated term has to be satisfied, so that
 * "LSD MDMA" narrows to experiences containing both instead of widening to either.
 */
class TestJournalMultiSubstanceSearch {

    @Test
    fun requiresEveryTermToMatch() {
        val both = experience("LSD trip", substances = listOf("LSD", "MDMA"))
        val onlyLsd = experience("LSD trip", substances = listOf("LSD"))

        val terms = listOf("LSD", "MDMA")
        val namesByTerm = listOf(setOf("LSD"), setOf("MDMA"))

        assertTrue(both.matchesAllTerms(terms, namesByTerm))
        assertFalse(
            "an experience with only one of the searched substances must not match",
            onlyLsd.matchesAllTerms(terms, namesByTerm)
        )
    }

    @Test
    fun singleTermStillMatchesOnTitleAndText() {
        val titled = experience("A quiet afternoon", substances = emptyList(), text = "")
        // No substance named "afternoon" is in the index, so only the title can satisfy it.
        assertTrue(titled.matchesAllTerms(listOf("afternoon"), listOf(emptySet())))
    }

    @Test
    fun substanceTermDoesNotMatchOnlyByLooseSubstringInUnrelatedText() {
        val unrelated = experience(
            title = "Beach day",
            substances = listOf("LSD"),
            text = "I wrote a long MDMA paragraph in the description"
        )
        val terms = listOf("LSD", "MDMA")
        val namesByTerm = listOf(setOf("LSD"), setOf("MDMA"))

        // The text mention does satisfy the second term, which is intended: free text is
        // searchable. This pins that behaviour so it is a decision, not an accident.
        assertTrue(unrelated.matchesAllTerms(terms, namesByTerm))
    }

    @Test
    fun emptyTermsMatchEverything() {
        val any = experience("Anything", substances = listOf("LSD"))
        assertTrue(any.matchesAllTerms(emptyList(), emptyList()))
    }

    @Test
    fun termCanBeSatisfiedByConsumerName() {
        val withConsumer = experience(
            title = "Party",
            substances = listOf("LSD"),
            consumerName = "Lin"
        )
        assertTrue(withConsumer.matchesAllTerms(listOf("lin"), listOf(emptySet())))
    }

    private fun experience(
        title: String,
        substances: List<String>,
        text: String = "",
        consumerName: String? = null
    ): ExperienceWithIngestionsCompanionsAndRatings {
        val experienceId = 1
        val now = Instant.parse("2026-01-01T12:00:00Z")
        return ExperienceWithIngestionsCompanionsAndRatings(
            experience = Experience(
                id = experienceId,
                title = title,
                text = text,
                sortDate = now,
                location = null
            ),
            ingestionsWithCompanions = substances.mapIndexed { index, name ->
                IngestionWithCompanionAndCustomUnit(
                    ingestion = Ingestion(
                        substanceName = name,
                        time = now,
                        administrationRoute = AdministrationRoute.ORAL,
                        dose = 100.0,
                        isDoseAnEstimate = false,
                        estimatedDoseStandardDeviation = null,
                        units = "mg",
                        experienceId = experienceId,
                        notes = null,
                        stomachFullness = null,
                        consumerName = consumerName,
                        customUnitId = null,
                        id = index
                    ),
                    substanceCompanion = null,
                    customUnit = null
                )
            },
            ratings = emptyList(),
            timedNotes = emptyList()
        )
    }
}
