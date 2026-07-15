package com.isaakhanimann.journal.ui.utils

import com.isaakhanimann.journal.data.substances.AdministrationRoute

fun categoryNameKey(name: String): String = "categories.$name"

fun administrationRouteKey(route: AdministrationRoute): String = "route_${route.name.lowercase()}"

fun administrationRouteDescriptionKey(route: AdministrationRoute): String =
    "route_${route.name.lowercase()}_desc"

fun administrationRouteArticleKey(route: AdministrationRoute): String =
    "route_${route.name.lowercase()}_article"
