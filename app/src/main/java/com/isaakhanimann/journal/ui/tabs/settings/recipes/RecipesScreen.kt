package com.isaakhanimann.journal.ui.tabs.settings.recipes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.room.experiences.entities.RecipeItem
import com.isaakhanimann.journal.data.room.experiences.relations.RecipeWithItems
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.localization.i18n

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    onBack: () -> Unit,
    onApplied: (Int) -> Unit,
    viewModel: RecipesViewModel = hiltViewModel()
) {
    val recipes by viewModel.recipes.collectAsState()
    val archivedRecipes by viewModel.archivedRecipes.collectAsState()
    val customUnits by viewModel.customUnits.collectAsState()
    var editing by remember { mutableStateOf<RecipeWithItems?>(null) }
    var creating by remember { mutableStateOf(false) }
    var showingArchived by remember { mutableStateOf(false) }
    var recipeToDelete by remember { mutableStateOf<RecipeWithItems?>(null) }

    if (creating || editing != null) {
        RecipeEditor(
            recipe = editing,
            customUnits = customUnits,
            onCancel = { creating = false; editing = null },
            onSave = { name, items ->
                viewModel.save(
                    id = editing?.recipe?.id ?: 0,
                    name = name,
                    items = items,
                    isArchived = editing?.recipe?.isArchived ?: false
                ) {
                    creating = false
                    editing = null
                }
            }
        )
        return
    }

    val visibleRecipes = if (showingArchived) archivedRecipes else recipes
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(i18n("recipes_title")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(onClick = { showingArchived = !showingArchived }) {
                        Text(i18n(if (showingArchived) "recipes_active" else "recipes_archived"))
                    }
                    if (!showingArchived) {
                        IconButton(onClick = { creating = true }) {
                            Icon(Icons.Outlined.Add, contentDescription = i18n("recipes_add"))
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (visibleRecipes.isEmpty()) {
                item { Text(i18n("recipes_empty"), modifier = Modifier.padding(vertical = 20.dp)) }
            }
            items(visibleRecipes, key = { it.recipe.id }) { entry ->
                ListItem(
                    headlineContent = { Text(entry.recipe.name) },
                    supportingContent = {
                        Text(
                            entry.items.joinToString { item ->
                                "${item.substanceName}${item.dose?.let { " · $it ${item.units.orEmpty()}" }.orEmpty()}"
                            }
                        )
                    },
                    modifier = Modifier.clickable { editing = entry },
                    trailingContent = {
                        Row {
                            if (showingArchived) {
                                TextButton(onClick = { viewModel.setArchived(entry.recipe, false) }) {
                                    Text(i18n("recipes_restore"))
                                }
                            } else {
                                IconButton(onClick = { viewModel.apply(entry, onApplied) }) {
                                    Icon(Icons.Outlined.PlayArrow, contentDescription = i18n("recipes_apply"))
                                }
                                TextButton(onClick = { viewModel.setArchived(entry.recipe, true) }) {
                                    Text(i18n("recipes_archive"))
                                }
                            }
                            IconButton(onClick = { recipeToDelete = entry }) {
                                Icon(Icons.Outlined.Delete, contentDescription = i18n("recipes_delete"))
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
    recipeToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { recipeToDelete = null },
            title = { Text(i18n("recipes_delete")) },
            text = { Text(i18n("recipes_delete_confirmation")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(entry.recipe)
                        recipeToDelete = null
                    }
                ) {
                    Text(i18n("recipes_delete"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { recipeToDelete = null }) {
                    Text(i18n("common_cancel"))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeEditor(
    recipe: RecipeWithItems?,
    customUnits: List<CustomUnit>,
    onCancel: () -> Unit,
    onSave: (String, List<RecipeItem>) -> Unit
) {
    var name by remember(recipe?.recipe?.id) { mutableStateOf(recipe?.recipe?.name.orEmpty()) }
    val items = remember(recipe?.recipe?.id) {
        mutableStateListOf<RecipeItem>().also {
            it.addAll(recipe?.items.orEmpty().sortedBy(RecipeItem::position))
        }
    }
    val activeCustomUnits = remember(customUnits) { customUnits.filterNot { it.isArchived } }
    var substanceName by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var units by remember { mutableStateOf("") }
    var isEstimate by remember { mutableStateOf(false) }
    var selectedRoute by remember { mutableStateOf(AdministrationRoute.ORAL) }
    var routeExpanded by remember { mutableStateOf(false) }
    var customUnitExpanded by remember { mutableStateOf(false) }
    var selectedCustomUnitId by remember(activeCustomUnits) {
        mutableIntStateOf(activeCustomUnits.firstOrNull()?.id ?: -1)
    }
    var customUnitDose by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(i18n("recipes_editor_title")) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(i18n("recipes_name")) },
                singleLine = true
            )
            Text(i18n("recipes_items"), style = MaterialTheme.typography.titleMedium)
            items.forEachIndexed { index, item ->
                ListItem(
                    headlineContent = { Text(item.substanceName) },
                    supportingContent = {
                        Text("${item.dose ?: "—"} ${item.units.orEmpty()} · ${item.administrationRoute.displayText}")
                    },
                    trailingContent = {
                        TextButton(onClick = { items.removeAt(index) }) {
                            Text(i18n("recipes_remove"))
                        }
                    }
                )
            }

            HorizontalDivider()
            Text(i18n("recipes_add_ingredient"), style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = substanceName,
                onValueChange = { substanceName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(i18n("recipes_substance")) },
                singleLine = true
            )
            ExposedDropdownMenuBox(
                expanded = routeExpanded,
                onExpandedChange = { routeExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedRoute.displayText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(i18n("recipes_route")) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = routeExpanded,
                    onDismissRequest = { routeExpanded = false }
                ) {
                    AdministrationRoute.values().forEach { route ->
                        DropdownMenuItem(
                            text = { Text(route.displayText) },
                            onClick = { selectedRoute = route; routeExpanded = false }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = dose,
                onValueChange = { dose = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(i18n("recipes_dose")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = units,
                onValueChange = { units = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(i18n("recipes_units")) },
                singleLine = true
            )
            Row {
                Checkbox(checked = isEstimate, onCheckedChange = { isEstimate = it })
                Text(i18n("recipes_estimated_dose"), modifier = Modifier.padding(top = 12.dp))
            }
            val parsedDose = dose.toDoubleOrNull()
            val isDoseValid = if (dose.isBlank()) {
                !isEstimate
            } else {
                parsedDose?.let { it.isFinite() && it > 0.0 } == true
            }
            Button(
                onClick = {
                    val cleanName = substanceName.trim()
                    if (cleanName.isNotEmpty()) {
                        items.add(
                            RecipeItem(
                                recipeId = recipe?.recipe?.id ?: 0,
                                position = items.size,
                                substanceName = cleanName,
                                administrationRoute = selectedRoute,
                                dose = parsedDose,
                                isDoseAnEstimate = isEstimate,
                                units = units.trim().ifEmpty { null }
                            )
                        )
                        substanceName = ""
                        dose = ""
                        units = ""
                        isEstimate = false
                    }
                },
                enabled = substanceName.isNotBlank() && isDoseValid,
                modifier = Modifier.fillMaxWidth()
            ) { Text(i18n("recipes_add_ingredient")) }

            if (activeCustomUnits.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = customUnitExpanded,
                    onExpandedChange = { customUnitExpanded = it }
                ) {
                    OutlinedTextField(
                        value = activeCustomUnits.firstOrNull { it.id == selectedCustomUnitId }
                            ?.let { "${it.name} (${it.substanceName})" }.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(i18n("recipes_custom_unit")) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = customUnitExpanded,
                        onDismissRequest = { customUnitExpanded = false }
                    ) {
                        activeCustomUnits.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text("${unit.name} (${unit.substanceName})") },
                                onClick = {
                                    selectedCustomUnitId = unit.id
                                    customUnitExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = customUnitDose,
                    onValueChange = { customUnitDose = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(i18n("recipes_custom_unit_quantity")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                val parsedCustomUnitDose = customUnitDose.toDoubleOrNull()
                val isCustomUnitDoseValid = customUnitDose.isBlank() ||
                    parsedCustomUnitDose?.let { it.isFinite() && it > 0.0 } == true
                Button(
                    onClick = {
                        val unit = activeCustomUnits.firstOrNull { it.id == selectedCustomUnitId }
                            ?: return@Button
                        items.add(
                            RecipeItem(
                                recipeId = recipe?.recipe?.id ?: 0,
                                position = items.size,
                                substanceName = unit.substanceName,
                                administrationRoute = unit.administrationRoute,
                                dose = customUnitDose.toDoubleOrNull(),
                                isDoseAnEstimate = false,
                                estimatedDoseStandardDeviation = null,
                                units = unit.unit,
                                customUnitId = unit.id
                            )
                        )
                        customUnitDose = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isCustomUnitDoseValid
                ) { Text(i18n("recipes_add_custom_unit")) }
            }

            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text(i18n("common_cancel"))
                }
                Button(
                    onClick = { if (name.isNotBlank() && items.size >= 2) onSave(name, items.toList()) },
                    enabled = name.isNotBlank() && items.size >= 2,
                    modifier = Modifier.weight(1f)
                ) { Text(i18n("common_save")) }
            }
            if (items.size < 2) Text(i18n("recipes_minimum_items"))
        }
    }
}
