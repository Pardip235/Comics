package com.bpn.comics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bpn.comics.data.repository.ComicRepositoryFactory
import com.bpn.comics.domain.repository.ComicRepository
import com.bpn.comics.platform.DatabaseDriverFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ComicsApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Initialize repository once
    val repository: ComicRepository? = remember {
        try {
            println("🔄 Initializing repository...")
            val driverFactory = DatabaseDriverFactory(context)
            val repo = ComicRepositoryFactory.createRepository(driverFactory)
            println("✅ Repository initialized successfully")
            repo
        } catch (e: Exception) {
            println("❌ Failed to initialize repository: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    // State for UI
    var resultText by remember { mutableStateOf("Click button to test API call") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Comic Repository Test",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Test Latest Comic Button
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        isLoading = true
                        errorMessage = null
                        try {
                            println("🔄 Starting API call to get latest comic...")
                            val comic = repository?.getLatestComic()
                            if (comic != null) {
                                resultText = "#${comic.num}: ${comic.title}"
                                println("✅ Success! Latest comic: #${comic.num} - ${comic.title}")
                                println("   Image URL: ${comic.img}")
                                println("   Alt text: ${comic.alt}")
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message}"
                            resultText = "Error occurred"
                            println("❌ Error fetching comic: ${e.message}")
                            e.printStackTrace()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = repository != null && !isLoading
            ) {
                Text(if (isLoading) "Loading..." else "Get Latest Comic")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Test Cached Comics Button
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        try {
                            println("📦 Fetching cached comics from database...")
                            val cached = repository?.getAllCachedComics()
                            resultText = "Cached: ${cached?.size ?: 0} comics"
                            println("✅ Found ${cached?.size ?: 0} cached comics")
                            cached?.forEach { comic ->
                                println("   - #${comic.num}: ${comic.title}")
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message}"
                            println("❌ Error fetching cached comics: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                },
                enabled = repository != null && !isLoading
            ) {
                Text("Get Cached Comics")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Display results
            if (resultText.isNotEmpty()) {
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            if (repository == null) {
                Text(
                    text = "Repository not initialized",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

