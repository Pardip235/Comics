package com.bpn.comics.navigation

/**
 * Sealed class representing all possible navigation routes in the app.
 */
sealed class ComicRoute(val route: String) {
    object Comics : ComicRoute("comics")
    object Favorites : ComicRoute("favorites")
    object ComicDetail : ComicRoute("comicDetail/{comicNumber}") {
        fun createRoute(comicNumber: Int) = "comicDetail/$comicNumber"
    }
}
