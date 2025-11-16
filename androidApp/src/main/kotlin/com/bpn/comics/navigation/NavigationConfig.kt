package com.bpn.comics.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Configuration for bottom navigation bar items
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
)

/**
 * Configuration object for navigation graph
 */
object NavigationConfig {
    /**
     * Bottom navigation items configuration
     */
    val bottomNavItems = listOf(
        BottomNavItem(
            route = ComicRoute.Comics.route,
            label = "Comics",
            icon = Icons.Default.Home,
            contentDescription = "Comics"
        ),
        BottomNavItem(
            route = ComicRoute.Favorites.route,
            label = "Favorites",
            icon = Icons.Default.Favorite,
            contentDescription = "Favorites"
        )
    )

    /**
     * Routes that should show the bottom navigation bar
     */
    val routesWithBottomNav = setOf(
        ComicRoute.Comics.route,
        ComicRoute.Favorites.route
    )

    /**
     * Start destination route
     */
    val startDestination = ComicRoute.Comics.route
}

