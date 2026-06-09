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
import java.lang.reflect.Modifier
import me.towdium.pinin.PinIn
import me.towdium.pinin.searchers.TreeSearcher
import me.towdium.pinin.searchers.Searcher.Logic.CONTAIN

class PinyinSubstanceSearcher() : SubstanceSearcher {

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

        // 统一过滤掉连字符和空格
        val searchString = word.replace(Regex("[- ]"), "").lowercase()

        val mainPrefixMatches = sources.filter { substance ->
            val cleanedName = substance.name.replace(Regex("[- ]"), "")
            
            if (cleanedName.startsWith(searchString, ignoreCase = true)) return@filter true
            
            if (cleanedName.isNotEmpty() && searchString.isNotEmpty()) {
                val firstCharMatch = pinIn.contains(cleanedName.first().toString(), searchString.first().toString())
                firstCharMatch && pinIn.contains(cleanedName, searchString)
            } else false
        }

        val prefixMatches = sources.filter { substance ->
            val allNames = substance.commonNames + listOfNotNull(substance.name, substance.localizedName)
            allNames.any { name ->
                val cleanedName = name.replace(Regex("[- ]"), "")
                cleanedName.startsWith(searchString, ignoreCase = true)
            }
        }

        val matches = sources.filter { substance ->
            val allNames = substance.commonNames + listOfNotNull(substance.name, substance.localizedName)
            allNames.any { name ->
                val cleanedName = name.replace(Regex("[- ]"), "")
                
                pinIn.contains(cleanedName, searchString) || 
                cleanedName.contains(searchString, ignoreCase = true)
            }
        }

        return (mainPrefixMatches + prefixMatches + matches).distinctBy { it.name }
    }
}
