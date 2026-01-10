package com.isaakhanimann.journal.ui.utils

import com.isaakhanimann.journal.data.substances.AdministrationRoute

fun categoryNameKey(name: String): String {
    val normalized = name.lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
    return "category_$normalized"
}

fun administrationRouteKey(route: AdministrationRoute): String {
    return "route_${route.name.lowercase()}"
}
