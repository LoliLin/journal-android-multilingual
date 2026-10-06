/*
 * Copyright (c) 2023. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.tabs.settings.combinations

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.SavedTimeDisplayOption
import com.isaakhanimann.journal.ui.tabs.settings.UserPreferencesBackup
import com.isaakhanimann.journal.ui.utils.DateLocaleOption
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class UserPreferences @Inject constructor(private val dataStore: DataStore<Preferences>) {
    private object PreferencesKeys {
        val KEY_TIME_DISPLAY_OPTION = stringPreferencesKey("key_time_display_option")

        // last ingestion time of experience is used when adding an ingestion from a past experience
        // cloned ingestion time is used to copy the time from another ingestion
        // those values need to be set/reset whenever an ingestion is added
        val KEY_LAST_INGESTION_OF_EXPERIENCE = longPreferencesKey("KEY_LAST_INGESTION_OF_EXPERIENCE")
        val KEY_CLONED_INGESTION_TIME = longPreferencesKey("KEY_CLONED_INGESTION_TIME")

        val KEY_HIDE_ORAL_DISCLAIMER = booleanPreferencesKey("key_hide_oral_disclaimer")
        val KEY_HIDE_DOSAGE_DOTS = booleanPreferencesKey("key_hide_dosage_dots")
        val KEY_OPEN_LINK_IN_BROWSER = booleanPreferencesKey("key_open_link_in_browser")
        val KEY_SELECTED_LANGUAGE = stringPreferencesKey("key_selected_language")
        val KEY_OWNER_USER_NAME = stringPreferencesKey("key_owner_user_name")
        val KEY_OWNER_USER_ACHIEVEMENT = stringPreferencesKey("key_owner_user_achievement") // registerName;
        val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("key_app_lock_enabled")
        val KEY_EFFECT_NOTIFICATION_ENABLED = booleanPreferencesKey("key_effect_notification_enabled")
        val KEY_ARE_SUBSTANCE_HEIGHTS_INDEPENDENT = booleanPreferencesKey("KEY_ARE_SUBSTANCE_HEIGHTS_INDEPENDENT")
        val KEY_IS_BOTTOM_BAR_PINNED = booleanPreferencesKey("key_is_bottom_bar_pinned")
        val KEY_IS_TIMELINE_HIDDEN = booleanPreferencesKey("KEY_IS_TIMELINE_HIDDEN")
        val KEY_MIDNIGHT_CUTOFF_ENABLED = booleanPreferencesKey("key_midnight_cutoff_enabled")
        val KEY_USE_24_HOUR_CLOCK = booleanPreferencesKey("key_use_24_hour_clock")
        val KEY_STATS_BY_INGESTION_TIME = booleanPreferencesKey("key_stats_by_ingestion_time")
        val KEY_DATE_LOCALE_OPTION = stringPreferencesKey("key_date_locale_option")
        val KEY_THEME_PALETTE = stringPreferencesKey("key_theme_palette")
    }

    suspend fun saveTimeDisplayOption(value: SavedTimeDisplayOption) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_TIME_DISPLAY_OPTION] = value.name
        }
    }

    suspend fun addAchievement(value: String): Boolean {
        var added = false
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.KEY_OWNER_USER_ACHIEVEMENT]
            val items = current?.split(";")?.filter { it.isNotEmpty() }?.toMutableSet()
                ?: mutableSetOf()
            added = items.add(value)
            preferences[PreferencesKeys.KEY_OWNER_USER_ACHIEVEMENT] = items.joinToString(";")
        }
        return added
    }

    val savedTimeDisplayOptionFlow: Flow<SavedTimeDisplayOption> = dataStore.data
        .map { preferences ->
            val name =
                preferences[PreferencesKeys.KEY_TIME_DISPLAY_OPTION]
                    ?: SavedTimeDisplayOption.REGULAR.name
            SavedTimeDisplayOption.valueOf(name)
        }

    suspend fun saveLastIngestionTimeOfExperience(value: Instant?) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_LAST_INGESTION_OF_EXPERIENCE] = value?.epochSecond ?: 0L
        }
    }

    val lastIngestionTimeOfExperienceFlow: Flow<Instant?> = dataStore.data
        .map { preferences ->
            val epochSecond = preferences[PreferencesKeys.KEY_LAST_INGESTION_OF_EXPERIENCE] ?: 0L
            if (epochSecond != 0L) {
                Instant.ofEpochSecond(epochSecond)
            } else {
                null
            }
        }

    suspend fun saveClonedIngestionTime(value: Instant?) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_CLONED_INGESTION_TIME] = value?.epochSecond ?: 0L
        }
    }

    val clonedIngestionTimeFlow: Flow<Instant?> = dataStore.data
        .map { preferences ->
            val epochSecond = preferences[PreferencesKeys.KEY_CLONED_INGESTION_TIME] ?: 0L
            if (epochSecond != 0L) {
                Instant.ofEpochSecond(epochSecond)
            } else {
                null
            }
        }

    suspend fun saveOralDisclaimerIsHidden(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_HIDE_ORAL_DISCLAIMER] = value
        }
    }

    val achievementsFlow: Flow<List<String>> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.KEY_OWNER_USER_ACHIEVEMENT]
                ?.split(";")
                ?.filter { it.isNotEmpty() }
                ?: emptyList()
        }

    val isOralDisclaimerHiddenFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.KEY_HIDE_ORAL_DISCLAIMER] ?: false
        }

    suspend fun saveDosageDotsAreHidden(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_HIDE_DOSAGE_DOTS] = value
        }
    }

    val areDosageDotsHiddenFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.KEY_HIDE_DOSAGE_DOTS] ?: false
        }

    suspend fun saveUse24HourClock(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_USE_24_HOUR_CLOCK] = value
        }
    }

    /** Null until the user chooses, in which case the system clock setting is followed. */
    val use24HourClockFlow: Flow<Boolean?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.KEY_USE_24_HOUR_CLOCK]
        }

    suspend fun saveAreSubstanceHeightsIndependent(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_ARE_SUBSTANCE_HEIGHTS_INDEPENDENT] = value
        }
    }

    val areSubstanceHeightsIndependentFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.KEY_ARE_SUBSTANCE_HEIGHTS_INDEPENDENT] ?: false
        }

    val isTimelineHiddenFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.KEY_IS_TIMELINE_HIDDEN] ?: false
        }

    suspend fun saveIsTimelineHidden(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_IS_TIMELINE_HIDDEN] = value
        }
    }

    val isBottomBarPinnedFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.KEY_IS_BOTTOM_BAR_PINNED] ?: false
        }

    suspend fun saveIsBottomBarPinned(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_IS_BOTTOM_BAR_PINNED] = value
        }
    }

    val isMidnightCutoffEnabledFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.KEY_MIDNIGHT_CUTOFF_ENABLED] ?: false
        }

    suspend fun saveMidnightCutoffEnabled(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_MIDNIGHT_CUTOFF_ENABLED] = value
        }
    }

    val isStatsByIngestionTimeFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.KEY_STATS_BY_INGESTION_TIME] ?: false
        }

    suspend fun saveStatsByIngestionTime(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_STATS_BY_INGESTION_TIME] = value
        }
    }

    suspend fun getRoaDurationPreset(roa: AdministrationRoute): Long? =
        dataStore.data.first()[longPreferencesKey("key_roa_duration_preset_${roa.name}")]

    suspend fun saveRoaDurationPreset(roa: AdministrationRoute, minutes: Long) {
        dataStore.edit { preferences ->
            preferences[longPreferencesKey("key_roa_duration_preset_${roa.name}")] = minutes
        }
    }

    suspend fun saveOpenLinkInBrowser(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_OPEN_LINK_IN_BROWSER] = value
        }
    }

    val isOpenLinkInBrowserFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.KEY_OPEN_LINK_IN_BROWSER] ?: false
        }

    suspend fun saveSelectedLanguage(value: String?) {
        dataStore.edit { preferences ->
            if (value == null) {
                preferences.remove(PreferencesKeys.KEY_SELECTED_LANGUAGE)
            } else {
                preferences[PreferencesKeys.KEY_SELECTED_LANGUAGE] = value
            }
        }
    }

    val selectedLanguageFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.KEY_SELECTED_LANGUAGE]
        }

    val isAppLockEnabledFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.KEY_APP_LOCK_ENABLED] ?: false
        }

    val isEffectNotificationEnabledFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.KEY_EFFECT_NOTIFICATION_ENABLED] ?: true
        }

    suspend fun saveEffectNotificationEnabled(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_EFFECT_NOTIFICATION_ENABLED] = value
        }
    }

    suspend fun saveAppLockEnabled(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_APP_LOCK_ENABLED] = value
        }
    }

    suspend fun saveOwnerUserName(value: String?) {
        dataStore.edit { preferences ->
            if (value == null) {
                preferences.remove(PreferencesKeys.KEY_OWNER_USER_NAME)
            } else {
                preferences[PreferencesKeys.KEY_OWNER_USER_NAME] = value
            }
        }
    }

    val ownerUserNameFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.KEY_OWNER_USER_NAME] ?: "You"
        }

    val themePaletteFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.KEY_THEME_PALETTE]
    }

    suspend fun saveThemePalette(value: String?) {
        dataStore.edit { preferences ->
            if (value == null) preferences.remove(PreferencesKeys.KEY_THEME_PALETTE)
            else preferences[PreferencesKeys.KEY_THEME_PALETTE] = value
        }
    }

    val dateLocaleOptionFlow: Flow<DateLocaleOption> = dataStore.data
        .map { preferences ->
            val name = preferences[PreferencesKeys.KEY_DATE_LOCALE_OPTION]
                ?: DateLocaleOption.FOLLOW_LANGUAGE.name
            runCatching { DateLocaleOption.valueOf(name) }
                .getOrDefault(DateLocaleOption.FOLLOW_LANGUAGE)
        }

    suspend fun saveDateLocaleOption(value: DateLocaleOption) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_DATE_LOCALE_OPTION] = value.name
        }
    }

    suspend fun exportForBackup(): UserPreferencesBackup =
        createBackup(dataStore.data.first())

    internal suspend fun restoreValidatedBackup(backup: UserPreferencesBackup) {
        dataStore.edit { preferences ->
            restoreValidatedBackupInto(preferences, backup)
        }
    }

    companion object {
        internal fun createBackup(source: Preferences): UserPreferencesBackup {
            val booleanValues = linkedMapOf<String, Boolean>()
            val byteArrayValues = linkedMapOf<String, String>()
            val doubleValues = linkedMapOf<String, String>()
            val floatValues = linkedMapOf<String, String>()
            val intValues = linkedMapOf<String, Int>()
            val longValues = linkedMapOf<String, Long>()
            val stringValues = linkedMapOf<String, String>()
            val stringSetValues = linkedMapOf<String, Set<String>>()

            source.asMap().forEach { (key, value) ->
                when (value) {
                    is Boolean -> booleanValues[key.name] = value
                    is ByteArray -> byteArrayValues[key.name] =
                        java.util.Base64.getEncoder().encodeToString(value)
                    is Double -> doubleValues[key.name] = value.toString()
                    is Float -> floatValues[key.name] = value.toString()
                    is Int -> intValues[key.name] = value
                    is Long -> longValues[key.name] = value
                    is String -> stringValues[key.name] = value
                    is Set<*> -> {
                        val stringSet = value.filterIsInstance<String>()
                        check(stringSet.size == value.size) {
                            "Non-string value in preference set ${key.name}"
                        }
                        stringSetValues[key.name] = stringSet.toSet()
                    }
                    else -> error("Unsupported preference value for ${key.name}: ${value::class}")
                }
            }

            return UserPreferencesBackup(
                booleanValues = booleanValues,
                byteArrayValues = byteArrayValues,
                doubleValues = doubleValues,
                floatValues = floatValues,
                intValues = intValues,
                longValues = longValues,
                stringValues = stringValues,
                stringSetValues = stringSetValues
            )
        }

        internal fun validateBackup(backup: UserPreferencesBackup) {
            backup.validate()
            backup.byteArrayValues.forEach { (key, value) ->
                if (preferenceType(key) == BackupPreferenceType.BYTE_ARRAY) {
                    java.util.Base64.getDecoder().decode(value)
                }
            }
            backup.doubleValues.forEach { (key, value) ->
                if (preferenceType(key) == BackupPreferenceType.DOUBLE) value.toDouble()
            }
            backup.floatValues.forEach { (key, value) ->
                if (preferenceType(key) == BackupPreferenceType.FLOAT) value.toFloat()
            }
        }

        internal fun restoreValidatedBackupInto(
            preferences: MutablePreferences,
            backup: UserPreferencesBackup?
        ) {
            if (backup == null) return
            backup.booleanValues.forEach { (key, value) ->
                if (preferenceType(key) == BackupPreferenceType.BOOLEAN) {
                    preferences[booleanPreferencesKey(key)] = value
                }
            }
            backup.byteArrayValues.forEach { (key, value) ->
                if (preferenceType(key) == BackupPreferenceType.BYTE_ARRAY) {
                    preferences[byteArrayPreferencesKey(key)] =
                        java.util.Base64.getDecoder().decode(value)
                }
            }
            backup.doubleValues.forEach { (key, value) ->
                if (preferenceType(key) == BackupPreferenceType.DOUBLE) {
                    preferences[doublePreferencesKey(key)] = value.toDouble()
                }
            }
            backup.floatValues.forEach { (key, value) ->
                if (preferenceType(key) == BackupPreferenceType.FLOAT) {
                    preferences[floatPreferencesKey(key)] = value.toFloat()
                }
            }
            backup.intValues.forEach { (key, value) ->
                if (preferenceType(key) == BackupPreferenceType.INT) {
                    preferences[intPreferencesKey(key)] = value
                }
            }
            backup.longValues.forEach { (key, value) ->
                if (preferenceType(key) == BackupPreferenceType.LONG) {
                    preferences[longPreferencesKey(key)] = value
                }
            }
            backup.stringValues.forEach { (key, value) ->
                if (preferenceType(key) == BackupPreferenceType.STRING) {
                    preferences[stringPreferencesKey(key)] = value
                }
            }
            backup.stringSetValues.forEach { (key, value) ->
                if (preferenceType(key) == BackupPreferenceType.STRING_SET) {
                    preferences[stringSetPreferencesKey(key)] = value
                }
            }
        }

        // Do not restore app/migration state; apply only keys whose type this version knows.
        // Update this list when adding a user preference to avoid restoring it under a wrong type.
        private fun preferenceType(key: String): BackupPreferenceType? = when (key) {
            PreferencesKeys.KEY_TIME_DISPLAY_OPTION.name,
            PreferencesKeys.KEY_SELECTED_LANGUAGE.name,
            PreferencesKeys.KEY_OWNER_USER_NAME.name,
            PreferencesKeys.KEY_OWNER_USER_ACHIEVEMENT.name,
            PreferencesKeys.KEY_DATE_LOCALE_OPTION.name,
            PreferencesKeys.KEY_THEME_PALETTE.name -> BackupPreferenceType.STRING

            PreferencesKeys.KEY_HIDE_ORAL_DISCLAIMER.name,
            PreferencesKeys.KEY_HIDE_DOSAGE_DOTS.name,
            PreferencesKeys.KEY_OPEN_LINK_IN_BROWSER.name,
            PreferencesKeys.KEY_APP_LOCK_ENABLED.name,
            PreferencesKeys.KEY_EFFECT_NOTIFICATION_ENABLED.name,
            PreferencesKeys.KEY_ARE_SUBSTANCE_HEIGHTS_INDEPENDENT.name,
            PreferencesKeys.KEY_IS_BOTTOM_BAR_PINNED.name,
            PreferencesKeys.KEY_IS_TIMELINE_HIDDEN.name,
            PreferencesKeys.KEY_MIDNIGHT_CUTOFF_ENABLED.name,
            PreferencesKeys.KEY_USE_24_HOUR_CLOCK.name,
            PreferencesKeys.KEY_STATS_BY_INGESTION_TIME.name -> BackupPreferenceType.BOOLEAN

            "substanceInteractions" -> BackupPreferenceType.STRING_SET
            PreferencesKeys.KEY_LAST_INGESTION_OF_EXPERIENCE.name,
            PreferencesKeys.KEY_CLONED_INGESTION_TIME.name -> BackupPreferenceType.LONG
            else -> if (key.startsWith("key_roa_duration_preset_")) {
                BackupPreferenceType.LONG
            } else {
                null
            }
        }
    }
}

private enum class BackupPreferenceType {
    BOOLEAN,
    BYTE_ARRAY,
    DOUBLE,
    FLOAT,
    INT,
    LONG,
    STRING,
    STRING_SET
}
