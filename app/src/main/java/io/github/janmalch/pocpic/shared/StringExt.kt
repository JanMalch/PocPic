package io.github.janmalch.pocpic.shared

import android.net.Uri
import kotlinx.datetime.LocalDateTime

fun String?.toUriOrNull(): Uri? = if (this == null) null else try {
    Uri.parse(this)
} catch (e: Exception) {
    null
}

fun String?.toLocalDateTimeOrNull(): LocalDateTime? = if (this == null) null else try {
    LocalDateTime.parse(this)
} catch (e: Exception) {
    null
}