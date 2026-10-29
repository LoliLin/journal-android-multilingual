package com.isaakhanimann.journal.data.room.experiences.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.isaakhanimann.journal.data.substances.AdministrationRoute

@Entity
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isArchived: Boolean = false
)

@Entity(primaryKeys = ["recipeId", "position"])
data class RecipeItem(
    val recipeId: Int,
    val position: Int,
    val substanceName: String,
    val administrationRoute: AdministrationRoute,
    val dose: Double?,
    val isDoseAnEstimate: Boolean = false,
    val estimatedDoseStandardDeviation: Double? = null,
    val units: String? = null,
    val customUnitId: Int? = null
)
