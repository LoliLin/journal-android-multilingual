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

class PinyinSubstanceSearcher(
    private val pinyinConverter: PinyinConverter = PinyinConverter(),
) : SubstanceSearcher {

    private val fallbackSearcher = DefaultSubstanceSearcher()

    override fun search(word: String, sources: List<Substance>): List<Substance> {
        val baseMatches = fallbackSearcher.search(word, sources)
        if (word.isBlank()) {
            return baseMatches
        }
        val normalizedSearch = normalize(word)
        val pinyinSearch = normalize(pinyinConverter.toPinyin(word))
        val pinyinMatches = sources.filter { substance ->
            val allNames = substance.commonNames + listOfNotNull(substance.name, substance.localizedName)
            allNames.any { name ->
                val normalizedName = normalize(name)
                val pinyinName = normalize(pinyinConverter.toPinyin(name))
                normalizedName.contains(normalizedSearch, ignoreCase = true) ||
                    (pinyinSearch.isNotBlank() && pinyinName.contains(pinyinSearch, ignoreCase = true))
            }
        }
        return (baseMatches + pinyinMatches).distinctBy { it.name }
    }

    private fun normalize(value: String): String {
        return value.replace(Regex("[- ]"), "")
    }
}

class PinyinConverter {
    private val converter = buildConverter()

    fun toPinyin(value: String): String {
        return converter?.invoke(value) ?: value
    }

    private fun buildConverter(): ((String) -> String)? {
        val methodNames = listOf("toPinyin", "pinyin", "convert", "parse")
        val classNames = listOf(
            "me.towdium.pinyin.PinIn",
            "me.towdium.pinyin.Pinyin",
            "me.towdium.pinin.PinIn",
            "com.github.towdium.pinyin.PinIn",
            "com.github.towdium.pinyin.Pinyin",
        )
        for (className in classNames) {
            val clazz = runCatching { Class.forName(className) }.getOrNull() ?: continue
            for (methodName in methodNames) {
                val method = clazz.methods.firstOrNull { candidate ->
                    candidate.name == methodName &&
                        candidate.parameterCount == 1 &&
                        (candidate.parameterTypes[0].isAssignableFrom(String::class.java) ||
                            candidate.parameterTypes[0].isAssignableFrom(CharSequence::class.java))
                } ?: continue
                val instance = if (Modifier.isStatic(method.modifiers)) {
                    null
                } else {
                    runCatching { clazz.getField("INSTANCE").get(null) }.getOrNull()
                        ?: runCatching { clazz.getDeclaredConstructor().newInstance() }.getOrNull()
                }
                return { input ->
                    runCatching { method.invoke(instance, input)?.toString() ?: input }.getOrDefault(input)
                }
            }
        }
        return null
    }
}
