package com.bpn.comics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpn.comics.util.ErrorType
import com.bpn.comics.util.Logger
import com.bpn.comics.util.isIOException
import com.bpn.comics.util.isKtorNetworkException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Extension functions for [ViewModel] to provide common error handling and Flow observation utilities.
 *
 * Determines the error type from a throwable.
 * 
 * @param throwable The exception to classify
 * @return [ErrorType.NETWORK_ERROR] for network-related exceptions, [ErrorType.UNKNOWN_ERROR] otherwise
 */
fun ViewModel.getErrorType(throwable: Throwable): ErrorType {
    val errorType = if (isKtorNetworkException(throwable) || isIOException(throwable)) {
        ErrorType.NETWORK_ERROR
    } else {
        ErrorType.UNKNOWN_ERROR
    }
    
    // Log error details for debugging
    val tag = this::class.simpleName ?: "ViewModel"
    Logger.e(
        tag = tag,
        message = "Error occurred: ${errorType.name}",
        throwable = throwable
    )
    
    return errorType
}

/**
 * Executes a suspend operation with error handling.
 * 
 * @param operation The suspend operation to execute
 * @param onError Callback invoked with the error type if operation fails
 */
suspend fun ViewModel.executeWithErrorHandling(
    operation: suspend () -> Unit,
    onError: (ErrorType) -> Unit
) {
    try {
        operation()
    } catch (t: Throwable) {
        val errorType = getErrorType(t)
        onError(errorType)
    }
}

/**
 * Observes a Flow with error handling.
 * 
 * @param flow The Flow to observe
 * @param onError Callback invoked when an error occurs
 * @param onSuccess Callback invoked for each emitted value
 * @return The Job that can be cancelled
 */
fun <T> ViewModel.observeFlow(
    flow: Flow<T>,
    onError: (ErrorType) -> Unit,
    onSuccess: (T) -> Unit
): Job {
    return viewModelScope.launch {
        flow.catch { throwable ->
            onError(getErrorType(throwable))
        }.collect { value ->
            onSuccess(value)
        }
    }
}

