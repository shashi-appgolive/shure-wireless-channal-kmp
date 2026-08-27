package com.shure.wireless.channels.core.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LogEntry(
    val sequence: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
)

/** In-memory application log used by the demo console and platform loggers. */
object AppLogStore {
    private const val MAX_ENTRIES = 200

    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()

    fun record(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        val renderedMessage = buildString {
            append(message)
            throwable?.message?.takeIf { it.isNotBlank() }?.let { append(" — ").append(it) }
        }
        _entries.update { current ->
            val nextSequence = (current.lastOrNull()?.sequence ?: 0L) + 1L
            (current + LogEntry(nextSequence, level, tag, renderedMessage)).takeLast(MAX_ENTRIES)
        }
    }

    fun clear() {
        _entries.value = emptyList()
    }
}
