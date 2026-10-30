package com.isaakhanimann.journal

import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.ui.tabs.settings.JournalExport
import com.isaakhanimann.journal.ui.tabs.settings.decodeCompatibleJournalImport
import com.isaakhanimann.journal.ui.tabs.settings.journalExportJson
import com.isaakhanimann.journal.ui.tabs.settings.journalImportJson
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TestCompatibleJournalImport {
    @Test
    fun importsForeignSignedArgbIntoTheCurrentLocalSchema() {
        val backup = """
            {
              "substanceCompanions": [
                {"substanceName": "Example", "color": null, "customColor": -50384}
              ],
              "experiences": [{
                "title": "Test", "text": "keep this text",
                "sortDate": 1792886400000,
                "timedNotes": [{
                  "creationDate": 1792886400000,
                  "time": 1792886400000,
                  "note": "keep this note",
                  "color": null,
                  "customColor": -27392,
                  "isPartOfTimeline": true
                }]
              }]
            }
        """.trimIndent()

        val restored = decodeCompatibleJournalImport(backup)

        // #FFFF3B30 and #FFFF9500 exactly match the light-mode preset values.
        assertEquals(AdaptiveColor.RED, restored.substanceCompanions.single().color)
        assertEquals(AdaptiveColor.ORANGE, restored.experiences.single().timedNotes.single().color)
        assertEquals(-50384, restored.substanceCompanions.single().customColorArgb)
        assertEquals("keep this text", restored.experiences.single().text)
        assertEquals("keep this note", restored.experiences.single().timedNotes.single().note)

        // The local schema keeps custom companion colors; timed notes still use the legacy palette.
        val exported = journalExportJson.encodeToString(restored)
        assertTrue(exported.contains("\"customColor\":-50384"))
        assertFalse(exported.contains("\"customColor\":-27392"))
        assertTrue(exported.contains("\"color\":\"RED\""))
    }

    @Test
    fun customArgbTakesPrecedenceOverAStaleLegacyColor() {
        val backup = """
            {"substanceCompanions": [
              {"substanceName": "Example", "color": "BLUE", "customColor": -50384}
            ]}
        """.trimIndent()

        assertEquals(
            AdaptiveColor.RED,
            decodeCompatibleJournalImport(backup).substanceCompanions.single().color
        )
    }

    @Test
    fun nullOrUnrecognizedColorsWithoutArgbUseTheDefaultPreset() {
        val backup = """
            {"substanceCompanions": [
              {"substanceName": "Null", "color": null},
              {"substanceName": "Unknown", "color": "FROM_ANOTHER_FORK"},
              {"substanceName": "Missing"}
            ]}
        """.trimIndent()

        val colors = decodeCompatibleJournalImport(backup).substanceCompanions.map { it.color }
        assertEquals(List(3) { AdaptiveColor.BLUE }, colors)
    }

    @Test
    fun existingPresetColorsAndRegularBackupsAreNotChanged() {
        val original = JournalExport()
        val backup = journalExportJson.encodeToString(original)
        assertEquals(original, decodeCompatibleJournalImport(backup))
        assertEquals(original, journalImportJson.decodeFromString<JournalExport>(backup))

        val withPresets = """
            {"substanceCompanions": [{"substanceName": "Example", "color": "PURPLE"}]}
        """.trimIndent()
        assertEquals(
            AdaptiveColor.PURPLE,
            decodeCompatibleJournalImport(withPresets).substanceCompanions.single().color
        )
    }

    @Test
    fun malformedCustomColorRetainsAValidLegacyPreset() {
        val backup = """
            {"substanceCompanions": [
              {"substanceName": "Example", "color": "GREEN", "customColor": "not-an-int"}
            ]}
        """.trimIndent()
        assertEquals(
            AdaptiveColor.GREEN,
            decodeCompatibleJournalImport(backup).substanceCompanions.single().color
        )
    }
}
