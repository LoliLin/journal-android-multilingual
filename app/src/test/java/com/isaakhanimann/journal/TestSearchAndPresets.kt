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
}
