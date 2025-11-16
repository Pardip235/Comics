package com.bpn.comics.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptionsBuilder

/**
 * Extension function for navigating to a main tab route with proper back stack handling
 */
fun NavController.navigateToTab(
    route: String,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(route) {
        // Pop up to the start destination to avoid building up a large stack
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies when reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
        builder()
    }
}

/**
 * Extension function to check if current destination matches any of the given routes
 */
fun String?.matchesAnyRoute(routes: Set<String>): Boolean {
    return this != null && routes.contains(this)
}





