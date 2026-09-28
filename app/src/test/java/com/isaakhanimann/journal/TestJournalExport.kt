package com.isaakhanimann.journal

import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.ReleaseForm
import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferencesBackup
import com.isaakhanimann.journal.ui.tabs.settings.IngestionSerializable
import com.isaakhanimann.journal.ui.tabs.settings.JournalExport
import com.isaakhanimann.journal.ui.tabs.settings.journalImportJson
import com.isaakhanimann.journal.ui.tabs.settings.toIngestion
import com.isaakhanimann.journal.ui.tabs.settings.toIngestionSerializable
import java.time.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TestJournalExport {
    @Test
    fun journalRoundTripPreservesTheRecordedFormulation() {
        // The null case also exercises backups that omit the new optional field.
        (listOf(null) + ReleaseForm.entries).forEach { form ->
            val original = ingestionWith(form)
            val backup = Json.encodeToString(original.toIngestionSerializable())
            val restored = Json.decodeFromString<IngestionSerializable>(backup)
                .toIngestion(experienceId = original.experienceId)
            assertEquals("Formulation $form must survive export and import", original, restored)
        }
    }

    @Test
    fun formulationAddedByANewerAppVersionImportsAsUnspecified() {
        // Serialization rejects unknown enum values, which would abort the whole import.
        // The shared import parser has to coerce the unknown field instead.
        val exported = Json.encodeToString(
            ingestionWith(ReleaseForm.EXTENDED_RELEASE).toIngestionSerializable()
        )
        val newerBackup = exported.replace("\"EXTENDED_RELEASE\"", "\"FUTURE_RELEASE_FORM\"")
        assertTrue(
            "fixture must carry the unknown formulation",
            newerBackup.contains("FUTURE_RELEASE_FORM")
        )
        assertNull(
            journalImportJson.decodeFromString<IngestionSerializable>(newerBackup).releaseForm
        )
    }

    @Test
    fun settingsPreferencesAndAvatarSurviveBackupRoundTrip() {
        val preferences = UserPreferencesBackup(
            booleanValues = mapOf("key_hide_dosage_dots" to true),
            longValues = mapOf("key_roa_duration_preset_ORAL" to 90L),
            stringValues = mapOf("key_owner_user_name" to "Alex")
        )
        val original = JournalExport(
            preferences = preferences,
            avatars = mapOf("Alex" to "avatar-bytes-base64")
        )

        val restored = journalImportJson.decodeFromString<JournalExport>(
            Json.encodeToString(original)
        )

        assertEquals(preferences, restored.preferences)
        assertEquals(original.avatars, restored.avatars)
    }

    @Test
    fun olderBackupWithoutPreferencesRemainsImportable() {
        val restored = journalImportJson.decodeFromString<JournalExport>("{}")

        assertNull(restored.preferences)
    }

    @Test
    fun conflictingPreferenceTypesAreRejectedBeforeImport() {
        val invalid = UserPreferencesBackup(
            booleanValues = mapOf("same_key" to true),
            stringValues = mapOf("same_key" to "value")
        )

        val exception = runCatching { invalid.validate() }.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
    }

    private fun ingestionWith(form: ReleaseForm?) = Ingestion(
        substanceName = "Example medicine",
        time = Instant.parse("2026-01-01T12:00:00Z"),
        creationDate = Instant.parse("2026-01-01T12:01:00Z"),
        administrationRoute = AdministrationRoute.ORAL,
        dose = 50.0,
        isDoseAnEstimate = false,
        estimatedDoseStandardDeviation = null,
        units = "mg",
        experienceId = 7,
        notes = "Product label recorded separately",
        stomachFullness = null,
        consumerName = null,
        customUnitId = null,
        releaseForm = form
    )
}
