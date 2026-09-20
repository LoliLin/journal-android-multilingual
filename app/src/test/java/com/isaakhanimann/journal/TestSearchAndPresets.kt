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

package com.isaakhanimann.journal

import com.isaakhanimann.journal.data.substances.classes.Substance
import com.isaakhanimann.journal.data.substances.search.DefaultSubstanceSearcher
import com.isaakhanimann.journal.data.substances.search.PinyinSubstanceSearcher
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.time.durationPresetLabel
import com.isaakhanimann.journal.ui.tabs.journal.splitSearchTerms
import org.junit.Assert.assertEquals
import org.junit.Test

class TestSearchAndPresets {

    @Test
    fun splitSearchTermsSplitsWhitespaceSeparatedTerms() {
        assertEquals(listOf("LSD", "MDMA"), splitSearchTerms("LSD MDMA"))
        assertEquals(listOf("LSD", "2C-B"), splitSearchTerms("  LSD   2C-B  "))
        assertEquals(listOf("Cannabis"), splitSearchTerms("Cannabis"))
        assertEquals(emptyList<String>(), splitSearchTerms("   "))
        // comma is not a separator
        assertEquals(listOf("LSD,MDMA"), splitSearchTerms("LSD,MDMA"))
    }

    @Test
    fun durationPresetLabels() {
        assertEquals("15m", durationPresetLabel(15))
        assertEquals("45m", durationPresetLabel(45))
        assertEquals("1h", durationPresetLabel(60))
        assertEquals("2h", durationPresetLabel(120))
        assertEquals("4h", durationPresetLabel(240))
    }

    @Test
    fun defaultSubstanceSearcherMatchesPrefixAndIgnoresHyphens() {
        val lsd = dummySubstance("LSD", localizedName = "麦角酸二乙酰胺", commonNames = listOf("Acid"))
        val cb2 = dummySubstance("2C-B", localizedName = null, commonNames = listOf("Nexus"))
        val mdma = dummySubstance("MDMA", localizedName = "亚甲二氧甲基苯丙胺", commonNames = listOf("Ecstasy", "Molly"))
        val sources = listOf(lsd, cb2, mdma)

        val searcher = DefaultSubstanceSearcher()

        // Blank query returns sources
        assertEquals(sources, searcher.search("", sources))

        // Hyphen and space stripping
        val match2cb = searcher.search("2cb", sources)
        assertEquals(listOf(cb2), match2cb)
        val match2cbSpace = searcher.search("2c b", sources)
        assertEquals(listOf(cb2), match2cbSpace)

        // Common names prefix match
        val matchAcid = searcher.search("ac", sources)
        assertEquals(listOf(lsd), matchAcid)

        // Substring match
        val matchStasy = searcher.search("tasy", sources)
        assertEquals(listOf(mdma), matchStasy)
    }

    @Test
    fun pinyinSubstanceSearcherMatchesPinyinAndLocalizedName() {
        val mdma = dummySubstance("MDMA", localizedName = "氯胺酮", commonNames = listOf("K粉"))
        val lsd = dummySubstance("LSD", localizedName = "麦角酸二乙酰胺", commonNames = listOf("Acid"))
        val sources = listOf(mdma, lsd)

        val searcher = PinyinSubstanceSearcher()

        // Pinyin search for localizedName prefix "lv" / "lu"
        val matchPinyin = searcher.search("lv", sources)
        assertEquals(listOf(mdma), matchPinyin)

        // Pinyin search for commonNames "kf"
        val matchCommonPinyin = searcher.search("kf", sources)
        assertEquals(listOf(mdma), matchCommonPinyin)
    }

    private fun dummySubstance(
        name: String,
        localizedName: String? = null,
        commonNames: List<String> = emptyList()
    ) = Substance(
        name = name,
        localizedName = localizedName,
        commonNames = commonNames,
        url = "",
        isApproved = true,
        tolerance = null,
        crossTolerances = emptyList(),
        addictionPotential = null,
        toxicities = emptyList(),
        categories = emptyList(),
        summary = null,
        effectsSummary = null,
        dosageRemark = null,
        generalRisks = null,
        longtermRisks = null,
        saferUse = emptyList(),
        interactions = null,
        roas = emptyList()
    )
}

