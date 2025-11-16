package com.bpn.comics.util

import platform.Foundation.NSDate

/**
 * iOS implementation of getCurrentTimestampSeconds.
 */
actual fun getCurrentTimestampSeconds(): Long {
    return (NSDate().timeIntervalSince1970).toLong()
}

