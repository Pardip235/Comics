package com.bpn.comics.util

import java.io.IOException

/**
 * Android implementation: Check if exception is an IOException.
 */
actual fun isIOException(exception: Throwable): Boolean {
    return exception is IOException
}

