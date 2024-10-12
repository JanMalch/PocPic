package io.github.janmalch.pocpic.core

import android.net.Uri
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime

@Immutable
data class Picture(
    val uri: Uri,
    val label: String,
    val sourceId: Long,
    val date: LocalDateTime?,
)