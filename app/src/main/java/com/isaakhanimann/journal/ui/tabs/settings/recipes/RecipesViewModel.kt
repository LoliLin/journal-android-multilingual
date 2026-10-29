package com.isaakhanimann.journal.ui.tabs.settings.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.Recipe
import com.isaakhanimann.journal.data.room.experiences.entities.RecipeItem
import com.isaakhanimann.journal.data.room.experiences.relations.RecipeWithItems
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

@HiltViewModel
class RecipesViewModel @Inject constructor(private val repository: ExperienceRepository) : ViewModel() {
    val recipes = repository.getRecipes(false).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val archivedRecipes = repository.getRecipes(true).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customUnits = repository.getAllCustomUnitsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(id: Int, name: String, items: List<RecipeItem>, isArchived: Boolean, done: () -> Unit) = viewModelScope.launch {
        repository.saveRecipe(Recipe(id = id, name = name.trim(), isArchived = isArchived), items)
        done()
    }
    fun setArchived(recipe: Recipe, archived: Boolean) = viewModelScope.launch {
        repository.setRecipeArchived(recipe.id, archived)
    }
    fun delete(recipe: Recipe) = viewModelScope.launch { repository.deleteRecipe(recipe) }
    fun apply(recipe: RecipeWithItems, done: (Int) -> Unit) = viewModelScope.launch {
        done(repository.applyRecipe(recipe.recipe.id, Instant.now()))
    }
}
