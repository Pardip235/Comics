package com.bpn.comics.ui.screen.comicdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.bpn.comics.data.model.Comic
import com.bpn.comics.presentation.comicdetail.ComicDetailViewModel
import com.bpn.comics.ui.common.EmptyScreen
import com.bpn.comics.ui.common.ErrorScreen
import com.bpn.comics.ui.common.LoadingScreen
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicDetailScreen(
    comicNumber: Int,
    onBackClick: () -> Unit,
    viewModel: ComicDetailViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(comicNumber) {
        viewModel.loadComicDetail(comicNumber)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comic #$comicNumber") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.comic != null) {
                        FavoriteIconButton(
                            isFavorite = uiState.comic!!.isFavorite,
                            onToggle = { viewModel.toggleFavorite() }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingScreen(message = "Loading comic...")
            uiState.errorType != null -> ErrorScreen(
                errorType = uiState.errorType,
                onRetry = { viewModel.retry(comicNumber) }
            )
            uiState.comic != null -> ComicDetailContent(
                comic = uiState.comic!!,
                modifier = Modifier.padding(paddingValues)
            )
            else -> EmptyScreen(message = "Comic not found")
        }
    }
}

@Composable
private fun FavoriteIconButton(
    isFavorite: Boolean,
    onToggle: () -> Unit
) {
    IconButton(onClick = onToggle) {
        val icon = if (isFavorite) {
            Icons.Default.Favorite
        } else {
            Icons.Default.FavoriteBorder
        }
        val contentDesc = if (isFavorite) {
            "Remove from favorites"
        } else {
            "Add to favorites"
        }
        val tint = if (isFavorite) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurface
        }
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = tint
        )
    }
}

@Composable
private fun ComicDetailContent(
    comic: Comic,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Comic Image
        SubcomposeAsyncImage(
            model = comic.img,
            contentDescription = comic.alt,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Image unavailable",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Comic Title
        Text(
            text = comic.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Comic Number and Date
        Text(
            text = "Comic #${comic.num} • " +
                    "${comic.month}/${comic.day}/${comic.year}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Alt Text
        if (comic.alt.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Alt Text",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = comic.alt,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Transcript
        if (comic.transcript.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Transcript",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = comic.transcript,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Link
        val link = comic.link
        if (link != null && link.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { /* TODO: Open link */ }
            ) {
                Text("View Related Link")
            }
        }
    }
}

