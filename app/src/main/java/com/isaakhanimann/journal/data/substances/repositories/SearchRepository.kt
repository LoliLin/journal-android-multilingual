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
class SearchRepository @Inject constructor(
    val substanceRepo: SubstanceRepository
) : SearchRepositoryInterface {

    override fun getMatchingSubstances(
        searchText: String,
        filterCategories: List<String>,
        recentlyUsedSubstanceNamesSorted: List<String>,
    ): List<SubstanceWithCategories> {
        val substancesMatchingCategories = getSubstancesMatchingCategories(filterCategories)
        val substancesFilteredWithText = getSubstancesMatchingSearchText(searchText, prefilteredSubstances = substancesMatchingCategories)
        return getSubstancesSorted(prefilteredSubstances = substancesFilteredWithText, recentlyUsedSubstanceNamesSorted = recentlyUsedSubstanceNamesSorted)
    }

    private fun getSubstancesMatchingCategories(filterCategories: List<String>): List<SubstanceWithCategories> {
        return substanceRepo.getAllSubstancesWithCategories().filter { substanceWithCategories ->
            filterCategories.all { substanceWithCategories.substance.categories.contains(it) }
        }
    }

    private fun getSubstancesMatchingSearchText(searchText: String, prefilteredSubstances: List<SubstanceWithCategories>): List<SubstanceWithCategories> {
        val sources = prefilteredSubstances.map { it.substance }
        val matches = substanceRepo.searcher.search(searchText, sources)
        val substanceByName = prefilteredSubstances.associateBy { it.substance.name }
        return matches.mapNotNull { substanceByName[it.name] }
    }

    private fun getSubstancesSorted(
        prefilteredSubstances: List<SubstanceWithCategories>,
        recentlyUsedSubstanceNamesSorted: List<String>
    ): List<SubstanceWithCategories> {
        val recentNames = recentlyUsedSubstanceNamesSorted.distinct()
        val recentlyUsedMatches =
            recentNames.filter { recent -> prefilteredSubstances.any { it.substance.name == recent } }
                .mapNotNull {
                    substanceRepo.getSubstanceWithCategories(
                        substanceName = it
                    )
                }
        val commonSubstanceMatches =
            prefilteredSubstances.filter { sub -> sub.categories.any { cat -> cat.name == "common" } }
        return (recentlyUsedMatches + commonSubstanceMatches + prefilteredSubstances).distinctBy { it.substance.name }
    }
}
