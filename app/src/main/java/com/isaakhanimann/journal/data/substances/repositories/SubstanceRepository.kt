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
        private const val FALLBACK_LANGUAGE_KEY = "en_US"
    }

    private var substanceFile: SubstanceFile

    init {
        val languageKey = I18n.getPreferredLanguageKey() ?: I18n.getCurrentLanguageKey()
        substanceFile = loadSubstanceFile(languageKey)
    }

    private fun loadSubstanceFile(languageKey: String): SubstanceFile {
        val fallbackCategories = loadCategoriesForLanguage(FALLBACK_LANGUAGE_KEY)
        val localizedCategories = if (languageKey != FALLBACK_LANGUAGE_KEY) {
            loadCategoriesForLanguage(languageKey)
        } else {
            emptyList()
        }
        val categories = mergeCategories(fallbackCategories, localizedCategories)

        val fallbackSubstances = loadSubstancesForLanguage(FALLBACK_LANGUAGE_KEY)
        val localizedSubstances = if (languageKey != FALLBACK_LANGUAGE_KEY) {
            loadSubstancesForLanguage(languageKey)
        } else {
            emptyList()
        }
        val substances = mergeSubstances(fallbackSubstances, localizedSubstances)

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

    private fun loadSubstancesForLanguage(languageKey: String): List<Substance> {
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
                        substanceParser.parseSubstance(reader.readText())
                    }
                }.getOrNull()
            }
    }

    private fun mergeCategories(fallback: List<Category>, localized: List<Category>): List<Category> {
        val merged = linkedMapOf<String, Category>()
        fallback.forEach { merged[it.name] = it }
        localized.forEach { merged[it.name] = it }
        return merged.values.toList()
    }

    private fun mergeSubstances(fallback: List<Substance>, localized: List<Substance>): List<Substance> {
        val merged = linkedMapOf<String, Substance>()
        fallback.forEach { merged[it.name] = it }
        localized.forEach { merged[it.name] = it }
        return merged.values.toList()
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