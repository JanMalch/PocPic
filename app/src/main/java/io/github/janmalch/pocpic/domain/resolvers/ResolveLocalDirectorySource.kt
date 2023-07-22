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
import kotlin.random.Random


class ResolveLocalDirectorySource @Inject constructor(
    private val random: Random,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context
) {

    suspend operator fun invoke(source: Source): Picture {
        require(source.type == SourceEntity.Type.LOCAL_DIRECTORY)
        return withContext(ioDispatcher) {
            val file = DocumentFile.fromTreeUri(context, source.uri)
                ?.listFiles()
                ?.filter { it.isFile }
                ?.randomOrNull(random)
                ?: // TODO: error handling?
                return@withContext Picture(
                    uri = source.uri,
                    label = "⚠️ " + source.label,
                    sourceId = source.id,
                    date = null,
                )
            return@withContext Picture(
                uri = file.uri,
                label = source.label,
                sourceId = source.id,
                date = file.lastModified().toLocalDateTime { e ->
                    Log.e(
                        "ResolveLocalDirectorySource",
                        "Failed to parse modified date to LocalDateTime.",
                        e
                    )
                    null
                }
            )
        }
    }

}