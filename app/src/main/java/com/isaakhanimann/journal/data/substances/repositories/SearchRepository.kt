/*
 * Copyright (c) 2023. Isaak Hanimann.
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

package com.isaakhanimann.journal.data.substances.repositories

import com.isaakhanimann.journal.data.substances.classes.SubstanceWithCategories
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(val substanceRepo: SubstanceRepository) :
    SearchRepositoryInterface {

    override fun getMatchingSubstances(
        searchText: String,
        filterCategories: List<String>,
        recentlyUsedSubstanceNamesSorted: List<String>
    ): List<SubstanceWithCategories> {
        val substancesMatchingCategories = getSubstancesMatchingCategories(filterCategories)
        val substancesFilteredWithText =
            getSubstancesMatchingSearchText(
                searchText,
                prefilteredSubstances = substancesMatchingCategories
            )
        return getSubstancesSorted(
            prefilteredSubstances = substancesFilteredWithText,
            recentlyUsedSubstanceNamesSorted = recentlyUsedSubstanceNamesSorted
        )
    }

    fun getSubstancesMatchingCategories(
        filterCategories: List<String>
    ): List<SubstanceWithCategories> =
        substanceRepo.getAllSubstancesWithCategories().filter { substanceWithCategories ->
            filterCategories.all { substanceWithCategories.substance.categories.contains(it) }
        }

    private fun getSubstancesMatchingSearchText(
        searchText: String,
        prefilteredSubstances: List<SubstanceWithCategories>
    ): List<SubstanceWithCategories> {
        val sources = prefilteredSubstances.map { it.substance }
        val matches = substanceRepo.searcher.search(searchText, sources)
        val substanceByName = prefilteredSubstances.associateBy { it.substance.name }
        return matches.mapNotNull { substanceByName[it.name] }
    }

    private fun getSubstancesSorted(
        prefilteredSubstances: List<SubstanceWithCategories>,
        recentlyUsedSubstanceNamesSorted: List<String>
    ): List<SubstanceWithCategories> {
        if (prefilteredSubstances.isEmpty()) return emptyList()

        val prefilteredByName = prefilteredSubstances.associateBy { it.substance.name }
        val result = ArrayList<SubstanceWithCategories>(prefilteredSubstances.size)
        val seen = HashSet<String>(prefilteredSubstances.size)

        for (name in recentlyUsedSubstanceNamesSorted) {
            val match = prefilteredByName[name]
            if (match != null && seen.add(match.substance.name)) {
                result.add(match)
            }
        }

        for (sub in prefilteredSubstances) {
            if (sub.categories.any { it.name == "common" } && seen.add(sub.substance.name)) {
                result.add(sub)
            }
        }

        for (sub in prefilteredSubstances) {
            if (seen.add(sub.substance.name)) {
                result.add(sub)
            }
        }

        return result
    }
}
