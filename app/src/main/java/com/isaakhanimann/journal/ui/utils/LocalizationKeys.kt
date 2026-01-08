package com.isaakhanimann.journal.ui.utils

fun categoryNameKey(name: String): String {
    val normalized = name.lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
    return "category_$normalized"
}
