package com.bpn.comics.ui.screen.comics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bpn.comics.data.model.Comic
import com.bpn.comics.presentation.comics.ComicsViewModel
import com.bpn.comics.ui.common.EmptyScreen
import com.bpn.comics.ui.common.ErrorScreen
import com.bpn.comics.ui.common.LoadingScreen
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicsScreen(
    onComicClick: (Int) -> Unit,
    viewModel: ComicsViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Comics", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(
                    onClick = { viewModel.refresh() },
                    enabled = !uiState.isLoading
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        )

        when {
            uiState.isLoading && uiState.comics.isEmpty() -> {
                LoadingScreen(message = "Loading comics...")
            }
            uiState.errorType != null -> {
                ErrorScreen(
                    errorType = uiState.errorType,
                    onRetry = viewModel::retry
                )
            }
            uiState.comics.isEmpty() -> {
                EmptyScreen(message = "No comics available")
            }
            else -> {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        count = uiState.comics.size,
                        key = { index -> uiState.comics[index].num }
                    ) { index ->
                        val comic = uiState.comics[index]
                        ComicCard(
                            comic = comic,
                            onClick = { onComicClick(comic.num) },
                            onFavoriteClick = {
                                viewModel.toggleFavorite(comic.num)
                            }
                        )
                    }

                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        }
                    }
                }

                // Observe scroll position to trigger next page
                val shouldLoadMore = remember {
                    derivedStateOf {
                        val lastVisible = listState.layoutInfo
                            .visibleItemsInfo.lastOrNull()?.index ?: 0
                        lastVisible >= uiState.comics.size - 3 &&
                                !uiState.isLoadingMore &&
                                uiState.hasMore
                    }
                }

                LaunchedEffect(shouldLoadMore.value) {
                    if (shouldLoadMore.value) {
                        viewModel.loadMoreComics()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ComicCardListPreview() {
    MaterialTheme {
        val sampleComics = listOf(
            Comic(
                num = 1,
                title = "Barrel - Part 1",
                img = "https://imgs.xkcd.com/comics/barrel_cropped_(1).jpg",
                alt = "Don't we all.",
                year = "2006",
                month = "1",
                day = "1"
            ),
            Comic(
                num = 2,
                title = "Petit Trees (sketch)",
                img = "https://imgs.xkcd.com/comics/tree_cropped_(1).jpg",
                alt = "I don't know how to draw trees.",
                year = "2006",
                month = "1",
                day = "2"
            ),
            Comic(
                num = 3,
                title = "Island (sketch)",
                img = "https://imgs.xkcd.com/comics/island_color.jpg",
                alt = "Hello, island.",
                year = "2006",
                month = "1",
                day = "3"
            )
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                count = sampleComics.size,
                key = { index -> sampleComics[index].num }
            ) { index ->
                ComicCard(
                    comic = sampleComics[index],
                    onClick = {},
                    onFavoriteClick = {}
                )
            }
        }
    }
}
