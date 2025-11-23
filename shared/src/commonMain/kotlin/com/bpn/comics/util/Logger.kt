package com.bpn.comics.util

import io.github.aakira.napier.Napier

/**
 * Logging utility using Napier (KMM-friendly logging library).
 * 
 * Napier automatically handles platform-specific logging:
 * - Android: Uses Android Log
 * - iOS: Uses NSLog (visible in Xcode console)
 * 
 * Usage in Xcode:
 * 1. Open Xcode Console (View > Debug Area > Activate Console)
 * 2. Filter by "ComicsApp" or search for log tags
 * 3. Logs will appear with timestamps and log levels
 * 
 * Usage in Android:
 * - Logs appear in Logcat with tag "ComicsApp"
 * - Filter by tag or use log level filters
 */
object Logger {
    private const val TAG = "ComicsApp"
    
    fun d(tag: String, message: String) {
        Napier.d(tag = TAG, message = "[$tag] $message")
    }
    
    fun i(tag: String, message: String) {
        Napier.i(tag = TAG, message = "[$tag] $message")
    }
    
    fun w(tag: String, message: String) {
        Napier.w(tag = TAG, message = "[$tag] $message")
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Napier.e(
                tag = TAG,
                message = "[$tag] $message",
                throwable = throwable
            )
        } else {
            Napier.e(tag = TAG, message = "[$tag] $message")
        }
    }
}
