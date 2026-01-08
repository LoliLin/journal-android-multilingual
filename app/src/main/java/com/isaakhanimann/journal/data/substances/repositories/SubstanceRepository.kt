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
import com.isaakhanimann.journal.localization.I18n
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubstanceRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val substanceParser: SubstanceParserInterface,
) : SubstanceRepositoryInterface {

    companion object {
        private const val SUBSTANCES_DIR = "substances"
        private const val CATEGORIES_FILE_NAME = "_categories.json"
        private const val ROOT_LANGUAGE_KEY = "root"
        private const val FALLBACK_LANGUAGE_KEY = ROOT_LANGUAGE_KEY
    }

    private var substanceFile: SubstanceFile

    init {
        val languageKey = I18n.getPreferredLanguageKey() ?: I18n.getCurrentLanguageKey()
        substanceFile = loadSubstanceFile(languageKey)
    }

    private fun loadSubstanceFile(languageKey: String): SubstanceFile {
        val languageKeys = listOf(ROOT_LANGUAGE_KEY, FALLBACK_LANGUAGE_KEY, languageKey).distinct()
        val categories = languageKeys.fold(emptyList<Category>()) { merged, key ->
            mergeCategories(merged, loadCategoriesForLanguage(key))
        }
        val substancesByFile = languageKeys.fold(emptyMap<String, JSONObject>()) { merged, key ->
            mergeSubstanceJsonMaps(merged, loadSubstanceJsonForLanguage(key))
        }
        val substances = parseSubstancesFromJsonMap(substancesByFile)

        return SubstanceFile(categories = categories, substances = substances)
    }

    private fun loadCategoriesForLanguage(languageKey: String): List<Category> {
        val path = "$SUBSTANCES_DIR/$languageKey/$CATEGORIES_FILE_NAME"
        return runCatching {
            appContext.assets.open(path).bufferedReader().use { reader ->
                substanceParser.parseCategories(reader.readText())
            }
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

    private fun mergeCategories(fallback: List<Category>, localized: List<Category>): List<Category> {
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

    private fun parseSubstancesFromJsonMap(substancesByFile: Map<String, JSONObject>): List<Substance> {
        return substancesByFile.toSortedMap().mapNotNull { (key, json) ->
            if (!json.has("name")) {
                json.put("name", key)
            }
            substanceParser.parseSubstance(json.toString())
        }
    }

    override fun getAllSubstances(): List<Substance> {
        return substanceFile.substances
    }

    override fun getAllSubstancesWithCategories(): List<SubstanceWithCategories> {
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
        return substanceFile.categories
    }

    override fun getSubstance(substanceName: String): Substance? {
        return substanceFile.substancesMap[substanceName]
    }

    override fun getCategory(categoryName: String): Category? {
        return substanceFile.categories.firstOrNull { it.name == categoryName }
    }

    override fun getSubstanceWithCategories(substanceName: String): SubstanceWithCategories? {
        val substance =
            substanceFile.substances.firstOrNull { it.name == substanceName } ?: return null
        return SubstanceWithCategories(
            substance = substance,
            categories = substanceFile.categories.filter { substance.categories.contains(it.name) }
        )
    }
}
