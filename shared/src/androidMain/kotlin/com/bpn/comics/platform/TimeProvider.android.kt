package com.bpn.comics.platform

/**
 * Android implementation of getCurrentTimestampSeconds.
 */
actual fun getCurrentTimestampSeconds(): Long {
    return System.currentTimeMillis() / 1000
}

