package com.isaakhanimann.journal

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.ReleaseForm
import com.isaakhanimann.journal.ui.tabs.settings.IngestionSerializable
import com.isaakhanimann.journal.ui.tabs.settings.JournalExport
import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferences
import com.isaakhanimann.journal.ui.tabs.settings.UserPreferencesBackup
import com.isaakhanimann.journal.ui.tabs.settings.journalImportJson
import com.isaakhanimann.journal.ui.tabs.settings.toIngestion
import com.isaakhanimann.journal.ui.tabs.settings.toIngestionSerializable
import java.time.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
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
    fun dataStorePreferenceTypesSerializeIntoBackup() {
        val snapshot = UserPreferences.createBackup(
            preferencesOf(
                booleanPreferencesKey("key_hide_dosage_dots") to true,
                byteArrayPreferencesKey("binary_value") to byteArrayOf(1, 2, 3),
                doublePreferencesKey("double_value") to Double.NaN,
                floatPreferencesKey("float_value") to Float.POSITIVE_INFINITY,
                intPreferencesKey("int_value") to 7,
                longPreferencesKey("key_roa_duration_preset_ORAL") to 90L,
                stringPreferencesKey("key_owner_user_name") to "Alex",
                stringSetPreferencesKey("substanceInteractions") to setOf("Alcohol", "Caffeine")
            )
        )

        assertEquals(true, snapshot.booleanValues["key_hide_dosage_dots"])
        assertEquals("AQID", snapshot.byteArrayValues["binary_value"])
        assertEquals("NaN", snapshot.doubleValues["double_value"])
        assertEquals("Infinity", snapshot.floatValues["float_value"])
        assertEquals(7, snapshot.intValues["int_value"])
        assertEquals(90L, snapshot.longValues["key_roa_duration_preset_ORAL"])
        assertEquals("Alex", snapshot.stringValues["key_owner_user_name"])
        assertEquals(
            setOf("Alcohol", "Caffeine"),
            snapshot.stringSetValues["substanceInteractions"]
        )
    }

    @Test
    fun settingsPreferencesAndAvatarSurviveBackupRoundTrip() {
        val preferences = UserPreferencesBackup(
            booleanValues = mapOf("key_hide_dosage_dots" to true),
            byteArrayValues = mapOf("binary_value" to "AQID"),
            doubleValues = mapOf("double_value" to "NaN"),
            floatValues = mapOf("float_value" to "Infinity"),
            intValues = mapOf("int_value" to 7),
            longValues = mapOf("key_roa_duration_preset_ORAL" to 90L),
            stringValues = mapOf("key_owner_user_name" to "Alex"),
            stringSetValues = mapOf("substanceInteractions" to setOf("Alcohol", "Caffeine"))
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
    fun olderBackupWithoutPreferencesPreservesLocalPreferences() {
        val localPreferences = mutablePreferencesOf(
            booleanPreferencesKey("key_hide_dosage_dots") to true,
            stringSetPreferencesKey("substanceInteractions") to setOf("Alcohol")
        )
        val restored = journalImportJson.decodeFromString<JournalExport>("{}")

        assertNull(restored.preferences)
        UserPreferences.restoreValidatedBackupInto(localPreferences, restored.preferences)

        assertEquals(true, localPreferences[booleanPreferencesKey("key_hide_dosage_dots")])
        assertEquals(
            setOf("Alcohol"),
            localPreferences[stringSetPreferencesKey("substanceInteractions")]
        )
    }

    @Test
    fun partialBackupOverlaysKnownKeysAndPreservesUnrelatedOrMismatchedSettings() {
        val localPreferences = mutablePreferencesOf(
            booleanPreferencesKey("key_app_lock_enabled") to true,
            booleanPreferencesKey("key_hide_dosage_dots") to false,
            stringPreferencesKey("device_only") to "keep",
            stringSetPreferencesKey("substanceInteractions") to setOf("Alcohol")
        )
        val backup = UserPreferencesBackup(
            booleanValues = mapOf("key_hide_dosage_dots" to true),
            stringValues = mapOf(
                "key_app_lock_enabled" to "wrong type",
                "key_owner_user_name" to "Alex",
                "future_key" to "ignore on this version"
            ),
            stringSetValues = mapOf("substanceInteractions" to setOf("Caffeine"))
        )

        UserPreferences.validateBackup(backup)
        UserPreferences.restoreValidatedBackupInto(localPreferences, backup)

        assertEquals(true, localPreferences[booleanPreferencesKey("key_hide_dosage_dots")])
        assertEquals(true, localPreferences[booleanPreferencesKey("key_app_lock_enabled")])
        assertEquals("keep", localPreferences[stringPreferencesKey("device_only")])
        assertEquals("Alex", localPreferences[stringPreferencesKey("key_owner_user_name")])
        assertEquals(
            setOf("Caffeine"),
            localPreferences[stringSetPreferencesKey("substanceInteractions")]
        )
        assertNull(localPreferences[stringPreferencesKey("future_key")])
    }

    @Test
    fun conflictingPreferenceTypesAreRejectedWithTheKeyName() {
        val invalid = UserPreferencesBackup(
            booleanValues = mapOf("same_key" to true),
            stringValues = mapOf("same_key" to "value")
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            invalid.validate()
        }
        assertTrue(exception.message.orEmpty().contains("same_key"))
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
