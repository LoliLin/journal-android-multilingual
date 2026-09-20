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
import me.towdium.pinin.PinIn

class PinyinSubstanceSearcher : SubstanceSearcher {

    private val pinIn = PinIn().apply {
        config()
            .accelerate(true)
            .fSh2S(true)
            .fCh2C(true)
            .fZh2Z(true)
            .commit()
    }

    override fun search(word: String, sources: List<Substance>): List<Substance> {
        if (word.isBlank()) return sources

        val searchString = cleanSearchTerm(word).lowercase()
        val firstSearchChar = searchString.firstOrNull()?.toString()

        val mainPrefixMatches = mutableListOf<Substance>()
        val prefixMatches = mutableListOf<Substance>()
        val substringMatches = mutableListOf<Substance>()

        for (substance in sources) {
            val cleanedName = cleanSearchTerm(substance.name)
            val cleanedLocalized = substance.localizedName?.let { cleanSearchTerm(it) }

            // 1. Main prefix match (substance name or localizedName)
            if (isPrefixMatch(cleanedName, searchString, firstSearchChar) ||
                (cleanedLocalized != null && isPrefixMatch(cleanedLocalized, searchString, firstSearchChar))
            ) {
                mainPrefixMatches.add(substance)
                continue
            }

            // 2. Secondary prefix match (commonNames)
            var matchedPrefix = false
            for (commonName in substance.commonNames) {
                val cleanedCommon = cleanSearchTerm(commonName)
                if (isPrefixMatch(cleanedCommon, searchString, firstSearchChar)) {
                    prefixMatches.add(substance)
                    matchedPrefix = true
                    break
                }
            }
            if (matchedPrefix) continue

            // 3. Substring / pinyin contains match
            if (isSubstringMatch(cleanedName, searchString) ||
                (cleanedLocalized != null && isSubstringMatch(cleanedLocalized, searchString)) ||
                substance.commonNames.any { isSubstringMatch(cleanSearchTerm(it), searchString) }
            ) {
                substringMatches.add(substance)
            }
        }

        return mainPrefixMatches + prefixMatches + substringMatches
    }

    private fun isPrefixMatch(cleanedText: String, searchString: String, firstSearchChar: String?): Boolean {
        if (cleanedText.startsWith(searchString, ignoreCase = true)) return true
        if (cleanedText.isNotEmpty() && firstSearchChar != null) {
            val firstChar = cleanedText.first().toString()
            val firstCharMatch = firstChar.equals(firstSearchChar, ignoreCase = true) ||
                pinIn.contains(firstChar, firstSearchChar)
            val lowerText = cleanedText.lowercase()
            return firstCharMatch && (pinIn.contains(cleanedText, searchString) || pinIn.contains(lowerText, searchString))
        }
        return false
    }

    private fun isSubstringMatch(cleanedText: String, searchString: String): Boolean {
        if (cleanedText.contains(searchString, ignoreCase = true)) return true
        val lowerText = cleanedText.lowercase()
        return pinIn.contains(cleanedText, searchString) || pinIn.contains(lowerText, searchString)
    }
}
