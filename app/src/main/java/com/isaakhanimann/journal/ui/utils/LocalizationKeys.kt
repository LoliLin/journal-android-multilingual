package com.isaakhanimann.journal.ui.utils

import com.isaakhanimann.journal.data.substances.AdministrationRoute

fun categoryNameKey(name: String): String {
    return "categories.$name"
}

fun administrationRouteKey(route: AdministrationRoute): String {
    return "route_${route.name.lowercase()}"
}
