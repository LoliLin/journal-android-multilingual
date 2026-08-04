/*
 * Copyright (c) 2022-2023. Isaak Hanimann.
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

package com.isaakhanimann.journal.data.achievement

import android.content.Context
import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

sealed class AchievementCondition {
    data class TotalDoseAtLeast(
        val substanceName: String,
        val minTotalMg: Double
    ) : AchievementCondition()

    data class SubstanceUsed(
        val substanceName: String
    ) : AchievementCondition()

    data class OwnerNameEquals(
        val value: String
    ) : AchievementCondition()
}

data class AchievementDefinition(
    val registerName: String,
    val iconPath: String,
    val condition: AchievementCondition
)

object AchievementEvaluator {

    fun evaluate(
        definition: AchievementDefinition,
        ingestions: List<Ingestion>,
        ownerUserName: String?
    ): Boolean = when (val condition = definition.condition) {
        is AchievementCondition.TotalDoseAtLeast -> ingestions
            .filter { it.substanceName == condition.substanceName }
            .sumOf { it.dose ?: 0.0 } >= condition.minTotalMg
        is AchievementCondition.SubstanceUsed -> ingestions
            .any { it.substanceName == condition.substanceName && (it.dose ?: 0.0) > 0 }
        is AchievementCondition.OwnerNameEquals -> ownerUserName == condition.value
    }
}

@Singleton
class AchievementDefinitionsLoader @Inject constructor(
    @ApplicationContext private val appContext: Context
) {

    val definitions: List<AchievementDefinition> = loadDefinitions()

    private fun loadDefinitions(): List<AchievementDefinition> {
        val jsonText = appContext.assets.open(ACHIEVEMENTS_ASSET_PATH)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        val achievementsArray = JSONObject(jsonText).getJSONArray("achievements")
        val result = mutableListOf<AchievementDefinition>()
        for (i in 0 until achievementsArray.length()) {
            val json = achievementsArray.getJSONObject(i)
            val registerName = json.getString("registerName")
            val iconPath = "file:///android_asset/images/achievements/${json.getString("iconPath")}"
            result.add(
                AchievementDefinition(
                    registerName = registerName,
                    iconPath = iconPath,
                    condition = parseCondition(json.getJSONObject("condition"))
                )
            )
            AchievementList.register(registerName, iconPath)
        }
        return result
    }

    private fun parseCondition(json: JSONObject): AchievementCondition =
        when (json.getString("type")) {
            "totalDoseAtLeast" -> AchievementCondition.TotalDoseAtLeast(
                substanceName = json.getString("substanceName"),
                minTotalMg = json.getDouble("minTotalMg")
            )
            "substanceUsed" -> AchievementCondition.SubstanceUsed(
                substanceName = json.getString("substanceName")
            )
            "ownerNameEquals" -> AchievementCondition.OwnerNameEquals(
                value = json.getString("value")
            )
            else -> throw IllegalArgumentException(
                "Unknown achievement condition type: ${json.getString("type")}"
            )
        }

    companion object {
        private const val ACHIEVEMENTS_ASSET_PATH = "achievements/achievements.json"
    }
}
