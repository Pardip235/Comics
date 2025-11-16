package com.bpn.comics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Simple cancellable interface for Swift interop
 * This allows Swift to cancel StateFlow observations
 */
interface Cancellable {
    fun cancel()
}

/**
 * Helper to observe StateFlow from iOS Swift
 * Provides a simple callback-based interface for Swift interop
 * 
 * Usage in Swift:
 * ```swift
 * let scope = KoinIOS.shared.getScope()
 * cancellable = StateFlowExtensionsKt.observe(viewModel.uiState, scope: scope) { newState in
 *     self.uiState = newState
 * }
 * ```
 */
fun <T> StateFlow<T>.observe(
    scope: CoroutineScope,
    callback: (T) -> Unit
): Cancellable {
    var job: Job? = null
    job = scope.launch {
        collect { value ->
            callback(value)
        }
    }
    return object : Cancellable {
        override fun cancel() {
            job?.cancel()
        }
    }
}

/**
 * Helper to get current StateFlow value from Swift
 * This provides a simple way to get the initial state
 * 
 * Usage in Swift:
 * ```swift
 * let initialState = StateFlowExtensionsKt.getValue(viewModel.uiState)
 * ```
 * 
 * Note: Extension functions in Kotlin/Native are exposed as static functions
 * in a class named after the file (StateFlowExtensionsKt)
 */
fun <T> StateFlow<T>.getValue(): T = this.value

