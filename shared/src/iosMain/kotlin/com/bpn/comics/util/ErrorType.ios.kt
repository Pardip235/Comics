package com.bpn.comics.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorNotConnectedToInternet
import platform.Foundation.NSURLErrorTimedOut
import platform.Foundation.NSURLErrorCannotConnectToHost
import platform.Foundation.NSURLErrorNetworkConnectionLost

/**
 * iOS implementation: Check if exception is an IOException (network-related error).
 * On iOS, we check for NSError with network-related error codes or IOException in class name.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun isIOException(exception: Throwable): Boolean {
    // Check if it's an NSError with network-related error codes
    val cause = exception.cause
    @Suppress("USELESS_IS_CHECK")
    if (cause is NSError) {
        if (cause.domain == NSURLErrorDomain) {
            val code = cause.code.toInt()
            return when (code) {
                NSURLErrorNotConnectedToInternet.toInt(),
                NSURLErrorTimedOut.toInt(),
                NSURLErrorCannotConnectToHost.toInt(),
                NSURLErrorNetworkConnectionLost.toInt() -> true
                else -> false
            }
        }
    }
    
    // Check class name for IOException-related exceptions
    val className = exception::class.simpleName?.lowercase() ?: ""
    val qualifiedName = exception::class.qualifiedName?.lowercase() ?: ""
    
    return className.contains("ioexception") || qualifiedName.contains("ioexception")
}

