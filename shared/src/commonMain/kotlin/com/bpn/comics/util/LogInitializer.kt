package com.bpn.comics.util

/**
 * Initialize Napier logging for the app.
 * This should be called once at app startup.
 * 
 * Platform-specific implementations set up the appropriate antilogger:
 * - Android: Uses DebugAntilog (Android Log)
 * - iOS: Uses DebugAntilog (NSLog for Xcode console)
 */
expect fun initializeLogging()

