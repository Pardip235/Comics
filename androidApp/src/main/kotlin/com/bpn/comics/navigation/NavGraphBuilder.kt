package com.bpn.comics.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bpn.comics.ui.screen.comic.ComicsScreen

/**
 * Composable function that sets up all navigation routes
 */
@Composable
fun NavGraphSetup(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavigationConfig.startDestination
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(ComicRoute.Comics.route) {
            ComicsScreen(
                onComicClick = {
                    // TODO Navigate to comic detail screen later
                }
            )
        }

        // TODO: Add favorites screen later
    }
}
