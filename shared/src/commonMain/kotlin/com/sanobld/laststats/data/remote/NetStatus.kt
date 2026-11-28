package com.sanobld.laststats.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Network state for the whole app: the last visible error (shown as a small
 * button in the top bar) and how many requests are running.
 */
object NetStatus {
    private val _error = MutableStateFlow<String?>(null)

    /** Last network error, null when everything is fine. */
    val error: StateFlow<String?> = _error

    private val _running = MutableStateFlow(0)
    val running: StateFlow<Int> = _running

    fun report(message: String) {
        _error.value = message
    }

    fun clear() {
        _error.value = null
    }

    /** Runs [block], counts it as a running request and reports a failure. */
    suspend fun <T> track(block: suspend () -> T): T {
        _running.update { it + 1 }
        try {
            val r = block()
            _error.value = null
            return r
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _error.value = e.message ?: e.toString()
            throw e
        } finally {
            _running.update { it - 1 }
        }
    }
}
