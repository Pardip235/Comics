package com.bpn.comics.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * Main navigation graph composable
 *
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if we should show the bottom navigation based on configuration
    val shouldShowBottomNav = currentRoute.matchesAnyRoute(NavigationConfig.routesWithBottomNav)

    Scaffold(
        bottomBar = {
            if (shouldShowBottomNav) {
                BottomNavBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { paddingValues ->
        NavGraphSetup(
            navController = navController,
            modifier = modifier.padding(bottom = paddingValues.calculateBottomPadding())
        )
    }
}
