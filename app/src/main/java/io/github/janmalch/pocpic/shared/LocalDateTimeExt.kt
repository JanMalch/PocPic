package io.github.janmalch.pocpic.shared

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

inline fun Long.toLocalDateTime(onException: (Exception) -> LocalDateTime?): LocalDateTime? =
    try {
        Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    } catch (e: Exception) {
        onException(e)
    }