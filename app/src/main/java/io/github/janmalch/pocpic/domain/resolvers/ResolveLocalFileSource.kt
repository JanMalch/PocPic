package io.github.janmalch.pocpic.domain.resolvers

import android.content.Context
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.janmalch.pocpic.data.SourceEntity
import io.github.janmalch.pocpic.domain.Picture
import io.github.janmalch.pocpic.domain.Source
import io.github.janmalch.pocpic.shared.IoDispatcher
import io.github.janmalch.pocpic.shared.toLocalDateTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject


class ResolveLocalFileSource @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context
) {

    suspend operator fun invoke(source: Source): Picture {
        require(source.type == SourceEntity.Type.LOCAL_FILE)
        return withContext(ioDispatcher) {
            val file = DocumentFile.fromSingleUri(context, source.uri)

            Picture(
                uri = file?.uri ?: source.uri,
                label = source.label,
                sourceId = source.id,
                date = file?.lastModified()?.toLocalDateTime { e ->
                    Log.e(
                        "ResolveLocalFileSource",
                        "Failed to parse modified date to LocalDateTime.",
                        e
                    )
                    null
                },
            )
        }
    }

}