package com.isaakhanimann.journal.data.room.experiences.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.isaakhanimann.journal.data.room.experiences.entities.Recipe
import com.isaakhanimann.journal.data.room.experiences.entities.RecipeItem

data class RecipeWithItems(
    @Embedded val recipe: Recipe,
    @Relation(parentColumn = "id", entityColumn = "recipeId") val items: List<RecipeItem>
)
