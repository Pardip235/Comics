package com.bpn.comics.util

/**
 * Android implementation of getCurrentTimestampSeconds.
 */
actual fun getCurrentTimestampSeconds(): Long {
    return System.currentTimeMillis() / 1000
}

