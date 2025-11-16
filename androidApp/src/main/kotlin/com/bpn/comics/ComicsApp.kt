package com.bpn.comics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bpn.comics.data.model.Comic
import com.bpn.comics.domain.usecase.GetInitialComicsUseCase
import com.bpn.comics.domain.usecase.HasMoreComicsUseCase
import com.bpn.comics.domain.usecase.LoadMoreComicsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicsApp() {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    // Inject use cases from Koin
    val getInitialComicsUseCase: GetInitialComicsUseCase = koinInject()
    val loadMoreComicsUseCase: LoadMoreComicsUseCase = koinInject()
    val hasMoreComicsUseCase: HasMoreComicsUseCase = koinInject()
    
    // State for comics list
    var comics by remember { mutableStateOf<List<Comic>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasMore by remember { mutableStateOf(true) }
    
    // Load initial comics when composable is first displayed
    LaunchedEffect(Unit) {
        if (comics.isEmpty() && !isLoading) {
            scope.launch(Dispatchers.IO) {
                isLoading = true
                errorMessage = null
                try {
                    println("🔄 Loading initial comics...")
                    val initialComics = getInitialComicsUseCase(10)
                    comics = initialComics
                    hasMore = hasMoreComicsUseCase.invoke(initialComics.minOfOrNull { it.num } ?: 0)
                    println("✅ Loaded ${initialComics.size} initial comics")
                } catch (e: Exception) {
                    errorMessage = "Error: ${e.message}"
                    println("❌ Error loading initial comics: ${e.message}")
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }
        }
    }
    
    // Detect when user scrolls near the end and load more comics
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val totalItems = comics.size
                // Load more when user scrolls to within 3 items of the end
                if (lastVisibleIndex != null && 
                    lastVisibleIndex >= totalItems - 3 && 
                    hasMore && 
                    !isLoadingMore) {
                    
                    val oldestComicNumber = comics.minOfOrNull { it.num } ?: 0
                    if (hasMoreComicsUseCase.invoke(oldestComicNumber)) {
                        scope.launch(Dispatchers.IO) {
                            isLoadingMore = true
                            try {
                                println("🔄 Loading more comics... (oldest: $oldestComicNumber)")
                                val moreComics = loadMoreComicsUseCase(oldestComicNumber, 10)
                                if (moreComics.isNotEmpty()) {
                                    comics = comics + moreComics
                                    val newOldest = comics.minOfOrNull { it.num } ?: 0
                                    hasMore = hasMoreComicsUseCase.invoke(newOldest)
                                    println("✅ Loaded ${moreComics.size} more comics. Total: ${comics.size}")
                                } else {
                                    hasMore = false
                                    println("📦 No more comics available")
                                }
                            } catch (e: Exception) {
                                println("❌ Error loading more comics: ${e.message}")
                                e.printStackTrace()
                            } finally {
                                isLoadingMore = false
                            }
                        }
                    } else {
                        hasMore = false
                    }
                }
            }
    }
    
    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            TopAppBar(
                title = { Text("XKCD Comics") }
            )
            
            // Error message
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Loading indicator for initial load
            if (isLoading && comics.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Comics list
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(comics) { comic ->
                        ComicItem(comic = comic)
                    }
                    
                    // Loading indicator for pagination
                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    
                    // End of list message
                    if (!hasMore && comics.isNotEmpty()) {
                        item {
                            Text(
                                text = "No more comics available",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComicItem(comic: Comic) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "#${comic.num}: ${comic.title}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${comic.year}-${comic.month}-${comic.day}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (comic.alt.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comic.alt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

