package io.github.janmalch.pocpic.domain

import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import io.github.janmalch.pocpic.data.SourceEntity
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
@Immutable
data class Picture(
    val uri: Uri,
    val label: String,
    val sourceId: Long,
    val date: LocalDateTime?,
) : Parcelable

@Parcelize
@Immutable
data class Source(
    val id: Long = 0,
    val label: String,
    val uri: Uri,
    val type: SourceEntity.Type,
    val weight: Int,
    val isRemoteRedirect: Boolean,
) : Parcelable {
    init {
        require(weight > 0) { "Weight must be positive." }
    }
}