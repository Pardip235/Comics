package com.bpn.comics.ui.screen.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bpn.comics.domain.model.Comic
import com.bpn.comics.presentation.favorites.FavoritesViewModel
import com.bpn.comics.ui.common.EmptyScreen
import com.bpn.comics.ui.common.ErrorScreen
import com.bpn.comics.ui.common.LoadingScreen
import com.bpn.comics.ui.screen.comics.ComicCard
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onComicClick: (Int) -> Unit,
    viewModel: FavoritesViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Favorites", fontWeight = FontWeight.Bold) }
        )

        when {
            uiState.isLoading -> {
                LoadingScreen(message = "Loading favorites...")
            }
            uiState.errorType != null -> {
                ErrorScreen(
                    errorType = uiState.errorType,
                    onRetry = viewModel::retry
                )
            }
            uiState.favorites.isEmpty() -> {
                EmptyScreen(message = "No favorite comics yet")
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        count = uiState.favorites.size,
                        key = { index -> uiState.favorites[index].num }
                    ) { index ->
                        val comic = uiState.favorites[index]
                        ComicCard(
                            comic = comic,
                            onClick = { onComicClick(comic.num) },
                            onFavoriteClick = {
                                viewModel.toggleFavorite(comic.num)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    MaterialTheme {
        val sampleFavorites = listOf(
            Comic(
                num = 1,
                title = "Barrel - Part 1",
                img = "https://imgs.xkcd.com/comics/barrel_cropped_(1).jpg",
                alt = "Don't we all.",
                year = "2006",
                month = "1",
                day = "1",
                isFavorite = true
            ),
            Comic(
                num = 2,
                title = "Petit Trees (sketch)",
                img = "https://imgs.xkcd.com/comics/tree_cropped_(1).jpg",
                alt = "I don't know how to draw trees.",
                year = "2006",
                month = "1",
                day = "2",
                isFavorite = true
            )
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                count = sampleFavorites.size,
                key = { index -> sampleFavorites[index].num }
            ) { index ->
                ComicCard(
                    comic = sampleFavorites[index],
                    onClick = {},
                    onFavoriteClick = {}
                )
            }
        }
    }
}

