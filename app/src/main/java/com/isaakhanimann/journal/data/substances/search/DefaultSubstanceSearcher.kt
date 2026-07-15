/*
 * Copyright (c) 2024. Isaak Hanimann.
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

package com.isaakhanimann.journal.data.substances.search

import com.isaakhanimann.journal.data.substances.classes.Substance

class DefaultSubstanceSearcher : SubstanceSearcher {
    override fun search(word: String, sources: List<Substance>): List<Substance> {
        if (word.isBlank()) {
            return sources
        }
        val searchString = word.replace(Regex("[- ]"), "")
        val mainPrefixMatches = sources.filter { substance ->
            substance.name.replace(Regex("[- ]"), "").startsWith(
                prefix = searchString,
                ignoreCase = true
            )
        }
        val prefixMatches = sources.filter { substance ->
            val allNames =
                substance.commonNames + listOfNotNull(substance.name, substance.localizedName)
            allNames.any { name ->
                name.replace(Regex("[- ]"), "").startsWith(
                    prefix = searchString,
                    ignoreCase = true
                )
            }
        }
        val matches = sources.filter { substance ->
            val allNames =
                substance.commonNames + listOfNotNull(substance.name, substance.localizedName)
            allNames.any { name ->
                name.replace(Regex("[- ]"), "").contains(
                    other = searchString,
                    ignoreCase = true
                )
            }
        }
        return (mainPrefixMatches + prefixMatches + matches).distinctBy { it.name }
    }
}
