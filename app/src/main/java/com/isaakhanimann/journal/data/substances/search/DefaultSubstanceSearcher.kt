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
        val searchString = clean(word)
        val mainPrefixMatches = mutableListOf<Substance>()
        val prefixMatches = mutableListOf<Substance>()
        val substringMatches = mutableListOf<Substance>()

        for (substance in sources) {
            val cleanedName = clean(substance.name)
            if (cleanedName.startsWith(searchString, ignoreCase = true)) {
                mainPrefixMatches.add(substance)
                continue
            }

            val cleanedLocalized = substance.localizedName?.let { clean(it) }
            if (cleanedLocalized != null && cleanedLocalized.startsWith(searchString, ignoreCase = true)) {
                prefixMatches.add(substance)
                continue
            }

            var matchedPrefix = false
            for (commonName in substance.commonNames) {
                if (clean(commonName).startsWith(searchString, ignoreCase = true)) {
                    prefixMatches.add(substance)
                    matchedPrefix = true
                    break
                }
            }
            if (matchedPrefix) continue

            if (cleanedName.contains(searchString, ignoreCase = true) ||
                (cleanedLocalized != null && cleanedLocalized.contains(searchString, ignoreCase = true)) ||
                substance.commonNames.any { clean(it).contains(searchString, ignoreCase = true) }
            ) {
                substringMatches.add(substance)
            }
        }

        return mainPrefixMatches + prefixMatches + substringMatches
    }

    private fun clean(s: String): String {
        if (s.indexOf('-') == -1 && s.indexOf(' ') == -1) return s
        val sb = StringBuilder(s.length)
        for (i in 0 until s.length) {
            val c = s[i]
            if (c != '-' && c != ' ') {
                sb.append(c)
            }
        }
        return sb.toString()
    }
}

