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
        config().fSh2S(false)
            .fCh2C(false)
            .fZh2Z(false)
            .commit()
    }

    override fun search(
        query: String,
        substances: List<Substance>
    ): List<Substance> {
        if (query.isBlank()) return substances
        

        val trimmedQuery = query.trim().replace(Regex("[- ]"), "").lowercase()


        return substances.filter { substance ->
            val allNames = substance.commonNames + listOfNotNull(substance.name, substance.localizedName)

            allNames.any { commonName ->
                pinIn.contains(commonName, trimmedQuery)
            }
        }
    }
}
