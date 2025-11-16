package com.bpn.comics.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bpn.comics.util.ErrorType

/**
 * Common error screen component.
 * Can be reused across different screens with different retry actions.
 */
@Composable
fun ErrorScreen(
    errorType: ErrorType?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
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

                // Message based on error type - UI decides what message to show
                Text(
                    text = when (errorType) {
                        ErrorType.NETWORK_ERROR ->
                            "No internet connection. Please check your network settings and try again."
                        ErrorType.UNKNOWN_ERROR ->
                            "Something went wrong. Please try again."
                        null ->
                            "Something went wrong"
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

@Preview(showBackground = true)
@Composable
private fun ErrorScreenNetworkPreview() {
    MaterialTheme {
        ErrorScreen(
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
            errorType = ErrorType.UNKNOWN_ERROR,
            onRetry = {}
        )
    }
}

