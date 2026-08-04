/*
 * Copyright (c) 2022. Isaak Hanimann.
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

package com.isaakhanimann.journal.data.substances.repositories

import android.content.Context
import com.isaakhanimann.journal.data.substances.classes.Category
import com.isaakhanimann.journal.data.substances.classes.Substance
import com.isaakhanimann.journal.data.substances.classes.SubstanceFile
import com.isaakhanimann.journal.data.substances.classes.SubstanceWithCategories
import com.isaakhanimann.journal.data.substances.parse.SubstanceParserInterface
import com.isaakhanimann.journal.data.substances.search.DefaultSubstanceSearcher
import com.isaakhanimann.journal.data.substances.search.PinyinSubstanceSearcher
import com.isaakhanimann.journal.data.substances.search.SubstanceSearcher
import com.isaakhanimann.journal.localization.I18n
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

object SubstanceEvents {
    private val _substanceReloadSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val substanceReloadSignal = _substanceReloadSignal.asSharedFlow()

    fun notifySubstanceReload() {
        _substanceReloadSignal.tryEmit(Unit)
    }
}

@Singleton
class SubstanceRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val substanceParser: SubstanceParserInterface
) : SubstanceRepositoryInterface {

    companion object {
        private const val SUBSTANCES_DIR = "substances"
        private const val CATEGORIES_FILE_NAME = "_categories.json"
        private const val ROOT_LANGUAGE_KEY = "root"
        private const val FALLBACK_LANGUAGE_KEY = ROOT_LANGUAGE_KEY
        private const val ZH_CN_LANGUAGE_KEY = "zh_cn"
    }

    @Volatile
    private var needsReload = false

    fun markDirty() {
        needsReload = true
    }

    @Volatile
    private var substanceFile: SubstanceFile
    @Volatile
    private var loadedLanguageKey: String
    @Volatile
    var searcher: SubstanceSearcher = DefaultSubstanceSearcher()
        private set

    // Serializes lazy reloads: getters may be called from any thread (e.g. IO vs main)
    // and a reload swaps the in-memory substanceFile reference. Substance objects are
    // immutable snapshots, so old references held by the UI stay valid after a swap.
    private val reloadLock = Any()

    init {
        val languageKey = I18n.getPreferredLanguageKey() ?: I18n.getCurrentLanguageKey()
        substanceFile = loadSubstanceFile(languageKey)
        loadedLanguageKey = languageKey
        updateSearcher(languageKey)
        needsReload = false
        CoroutineScope(Dispatchers.Default).launch {
            SubstanceEvents.substanceReloadSignal.collect {
                markDirty()
            }
        }
    }

    private fun ensureLanguageLoaded() {
        val languageKey = I18n.getPreferredLanguageKey() ?: I18n.getCurrentLanguageKey()
        if (languageKey == loadedLanguageKey && !needsReload) return
        synchronized(reloadLock) {
            // Re-check inside the lock: another thread may have reloaded meanwhile.
            if (languageKey == loadedLanguageKey && !needsReload) return
            reload(languageKey)
        }
    }

    private fun reload(languageKey: String) {
        substanceFile = loadSubstanceFile(languageKey)
        loadedLanguageKey = languageKey
        updateSearcher(languageKey)
        needsReload = false
    }

    fun updateSearcher(languageKey: String) {
        searcher = when (languageKey.lowercase()) {
            ZH_CN_LANGUAGE_KEY -> PinyinSubstanceSearcher()
            else -> DefaultSubstanceSearcher()
        }
    }

    fun getDisplayName(substanceName: String): String =
        getSubstance(substanceName)?.displayName ?: substanceName

    fun isSubstance(substanceName: String): Boolean = getSubstance(substanceName) != null

    fun isCategory(substanceName: String): Boolean = getCategory(substanceName) != null

    fun getSubstanceDisplayName(substanceName: String): String =
        getSubstance(substanceName)?.displayName ?: substanceName

    private fun loadSubstanceFile(languageKey: String): SubstanceFile {
        val languageKeys = listOf(ROOT_LANGUAGE_KEY, FALLBACK_LANGUAGE_KEY, languageKey).distinct()
        val categories = languageKeys.fold(emptyList<Category>()) { merged, key ->
            mergeCategories(merged, loadCategoriesForLanguage(key))
        }
        var substancesByFile = languageKeys.fold(emptyMap<String, JSONObject>()) { merged, key ->
            val v0 = mergeSubstanceJsonMaps(merged, loadSubstanceJsonForLanguage(key))
            mergeSubstanceJsonMaps(v0, loadSubstanceJsonFromExtensions(key, appContext))
        }

        val substances = parseSubstancesFromJsonMap(substancesByFile)

        return SubstanceFile(categories = categories, substances = substances)
    }

    private fun loadCategoriesForLanguage(languageKey: String): List<Category> {
        val path = "$SUBSTANCES_DIR/$languageKey/$CATEGORIES_FILE_NAME"

        val mergedJson = run {
            val baseArray = try {
                val baseText = appContext.assets.open(path).bufferedReader().use { it.readText() }
                JSONArray(baseText)
            } catch (_: Exception) {
                JSONArray()
            }

            val extDir = java.io.File(appContext.filesDir, "ext_packs")
            if (extDir.exists() && extDir.isDirectory) {
                extDir.listFiles()?.forEach { packDir ->
                    val extFile = java.io.File(packDir, path)
                    if (extFile.isFile) {
                        try {
                            val extArray = JSONArray(extFile.readText())
                            for (i in 0 until extArray.length()) {
                                baseArray.put(extArray.get(i))
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            }
            baseArray.toString()
        }

        return runCatching {
            substanceParser.parseCategories(mergedJson)
        }.getOrElse { emptyList() }
    }

    private fun loadSubstanceJsonForLanguage(languageKey: String): Map<String, JSONObject> {
        val directory = "$SUBSTANCES_DIR/$languageKey"
        val files = runCatching {
            appContext.assets.list(directory)?.toList() ?: emptyList()
        }.getOrElse {
            emptyList()
        }
        return files.filter { it.endsWith(".json") && it != CATEGORIES_FILE_NAME }
            .sorted()
            .mapNotNull { fileName ->
                runCatching {
                    appContext.assets.open("$directory/$fileName").bufferedReader().use { reader ->
                        val key = fileName.removeSuffix(".json")
                        key to JSONObject(reader.readText())
                    }
                }.getOrNull()
            }
            .toMap()
    }

    private fun loadSubstanceJsonFromExtensions(
        languageKey: String,
        context: android.content.Context
    ): Map<String, JSONObject> {
        val extDir = java.io.File(context.filesDir, "ext_packs")
        if (!extDir.exists()) return emptyMap()

        val files = extDir.listFiles()
            ?.flatMap { packDir ->
                val subDir = java.io.File(packDir, "substances/$languageKey")
                if (subDir.exists()) {
                    subDir.listFiles()?.toList() ?: emptyList()
                } else {
                    emptyList()
                }
            }
            ?: emptyList()

        return files
            .filter { it.name.endsWith(".json") && it.name != CATEGORIES_FILE_NAME }
            .sortedBy { it.name }
            .mapNotNull { file ->
                runCatching {
                    val key = file.nameWithoutExtension
                    val json = org.json.JSONObject(file.readText())
                    key to json
                }.getOrNull()
            }
            .toMap()
    }

    private fun mergeCategories(
        fallback: List<Category>,
        localized: List<Category>
    ): List<Category> {
        val merged = linkedMapOf<String, Category>()
        fallback.forEach { merged[it.name] = it }
        localized.forEach { merged[it.name] = it }
        return merged.values.toList()
    }

    private fun mergeSubstanceJsonMaps(
        fallback: Map<String, JSONObject>,
        localized: Map<String, JSONObject>
    ): Map<String, JSONObject> {
        val merged = linkedMapOf<String, JSONObject>()
        fallback.forEach { (key, value) -> merged[key] = value }
        localized.forEach { (key, value) ->
            val existing = merged[key]
            merged[key] = if (existing == null) {
                value
            } else {
                mergeJsonObjects(existing, value)
            }
        }
        return merged
    }

    private fun mergeJsonObjects(base: JSONObject, overlay: JSONObject): JSONObject {
        val result = JSONObject(base.toString())
        val keys = overlay.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val overlayValue = overlay.get(key)
            val baseValue = result.opt(key)
            val mergedValue = if (overlayValue is JSONObject && baseValue is JSONObject) {
                mergeJsonObjects(baseValue, overlayValue)
            } else {
                overlayValue
            }
            result.put(key, mergedValue)
        }
        return result
    }

    private fun parseSubstancesFromJsonMap(
        substancesByFile: Map<String, JSONObject>
    ): List<Substance> = substancesByFile.toSortedMap().mapNotNull { (key, json) ->
        if (!json.has("name")) {
            json.put("name", key)
        }
        substanceParser.parseSubstance(json.toString())
    }

    override fun getAllSubstances(): List<Substance> {
        ensureLanguageLoaded()
        return substanceFile.substances
    }

    override fun getAllSubstancesWithCategories(): List<SubstanceWithCategories> {
        ensureLanguageLoaded()
        return substanceFile.substances.map { substance ->
            SubstanceWithCategories(
                substance = substance,
                categories = substanceFile.categories.filter { category ->
                    substance.categories.contains(category.name)
                }
            )
        }
    }

    override fun getAllCategories(): List<Category> {
        ensureLanguageLoaded()
        return substanceFile.categories
    }

    override fun getSubstance(substanceName: String): Substance? {
        ensureLanguageLoaded()
        return substanceFile.substancesMap[substanceName]
    }

    override fun getCategory(categoryName: String): Category? {
        ensureLanguageLoaded()
        return substanceFile.categories.firstOrNull { it.name == categoryName }
    }

    override fun getSubstanceWithCategories(substanceName: String): SubstanceWithCategories? {
        ensureLanguageLoaded()
        val substance =
            substanceFile.substances.firstOrNull { it.name == substanceName } ?: return null
        return SubstanceWithCategories(
            substance = substance,
            categories = substanceFile.categories.filter { substance.categories.contains(it.name) }
        )
    }
}
