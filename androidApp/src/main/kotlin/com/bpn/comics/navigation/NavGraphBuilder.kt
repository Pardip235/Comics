package com.bpn.comics.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bpn.comics.ui.screen.comicdetail.ComicDetailScreen
import com.bpn.comics.ui.screen.comics.ComicsScreen
import com.bpn.comics.ui.screen.favorites.FavoritesScreen

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
                onComicClick = { comicNumber ->
                    navController.navigate(ComicRoute.ComicDetail.createRoute(comicNumber))
                }
            )
        }

        composable(ComicRoute.Favorites.route) {
            FavoritesScreen(
                onComicClick = { comicNumber ->
                    navController.navigate(ComicRoute.ComicDetail.createRoute(comicNumber))
                }
            )
        }

        composable(
            route = ComicRoute.ComicDetail.route,
            arguments = listOf(
                navArgument("comicNumber") {
                    type = androidx.navigation.NavType.IntType
                }
            )
        ) { backStackEntry ->
            val comicNumber = backStackEntry.arguments?.getInt("comicNumber") ?: 0
            ComicDetailScreen(
                comicNumber = comicNumber,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
