package com.bpn.comics.ui.screen.comic

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bpn.comics.data.model.Comic
import com.bpn.comics.presentation.ComicsUiState
import com.bpn.comics.presentation.ComicsViewModel
import com.bpn.comics.util.ErrorType
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
            uiState.isLoading && uiState.comics.isEmpty() -> LoadingScreen()
            uiState.errorMessage != null -> ErrorScreen(
                errorMessage = uiState.errorMessage,
                errorType = uiState.errorType,
                onRetry = viewModel::retry
            )
            uiState.comics.isEmpty() -> EmptyScreen()
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
                            onClick = { onComicClick(comic.num) }
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
                        val lastVisible =
                            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        lastVisible >= uiState.comics.size - 3 && !uiState.isLoadingMore && uiState.hasMore
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


@Composable
private fun EmptyScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No comics available",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorScreen(
    errorMessage: String?,
    errorType: ErrorType?,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title based on error type
                Text(
                    text = when (errorType) {
                        ErrorType.NETWORK_ERROR -> "No Internet Connection"
                        ErrorType.UNKNOWN_ERROR -> "Error"
                        null -> "Error"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Message based on error type - UI has freedom to show appropriate message
                Text(
                    text = when (errorType) {
                        ErrorType.NETWORK_ERROR -> 
                            "No internet connection. Please check your network settings and try again."
                        ErrorType.UNKNOWN_ERROR -> 
                            errorMessage ?: "Something went wrong. Please try again."
                        null -> 
                            errorMessage ?: "Something went wrong"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading comics...")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyScreenPreview() {
    MaterialTheme {
        EmptyScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorScreenNetworkPreview() {
    MaterialTheme {
        ErrorScreen(
            errorMessage = "Unable to resolve host",
            errorType = ErrorType.NETWORK_ERROR,
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorScreenUnknownPreview() {
    MaterialTheme {
        ErrorScreen(
            errorMessage = "Failed to parse response",
            errorType = ErrorType.UNKNOWN_ERROR,
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingScreenPreview() {
    MaterialTheme {
        LoadingScreen()
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
                    onClick = {}
                )
            }
        }
    }
}
