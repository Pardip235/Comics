package com.bpn.comics.platform

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * iOS implementation of getCurrentTimestampSeconds.
 */
actual fun getCurrentTimestampSeconds(): Long {
    return (NSDate().timeIntervalSince1970).toLong()
}

