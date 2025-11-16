package com.bpn.comics.util

/**
 * Types of errors that can occur in the app.
 */
enum class ErrorType {
    NETWORK_ERROR,
    UNKNOWN_ERROR
}

/**
 * Platform-specific function to check if an exception is an IOException.
 * This uses expect/actual mechanism for KMM compatibility.
 */
expect fun isIOException(exception: Throwable): Boolean

/**
 * Check if exception is a Ktor client network exception by checking class names.
 * This works across platforms without requiring platform-specific imports.
 */
fun isKtorNetworkException(exception: Throwable): Boolean {
    // Check the exception and its cause chain for Ktor network exceptions
    var current: Throwable? = exception
    while (current != null) {
        val className = current::class.simpleName?.lowercase() ?: ""
        val qualifiedName = current::class.qualifiedName?.lowercase() ?: ""
        
        // Check for Ktor client exceptions
        if (qualifiedName.contains("ktor.client")) {
            // Ktor client exceptions are typically network-related
            if (qualifiedName.contains("call") || 
                qualifiedName.contains("network") || 
                qualifiedName.contains("timeout") ||
                qualifiedName.contains("connection")) {
                return true
            }
        }
        
        // Check for common network-related exception patterns
        if (className.contains("httpclientcall") ||
            className.contains("network") ||
            className.contains("timeout") ||
            className.contains("connection")) {
            return true
        }
        
        current = current.cause
    }
    
    return false
}

