package io.github.janmalch.pocpic.domain

import android.content.ContentResolver
import android.util.Log
import io.github.janmalch.pocpic.data.SourceDao
import io.github.janmalch.pocpic.data.SourceEntity
import io.github.janmalch.pocpic.shared.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InsertSources @Inject constructor(
    private val dao: SourceDao,
    private val contentResolver: ContentResolver,
    private val isRemoteRedirect: IsRemoteRedirect,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    suspend operator fun invoke(sources: List<Source>): Unit =
        withContext(ioDispatcher) {
            val lookup = sources.map { async { isRemoteRedirect(it) } }.awaitAll()

            dao.insert(
                sources.mapIndexed { index, it ->
                    SourceEntity(
                        id = it.id,
                        label = it.label,
                        uri = it.uri,
                        type = it.type,
                        weight = it.weight,
                        isRemoteRedirect = lookup[index],
                    )
                }
            )

            Log.d("InsertSources", "Inserted ${sources.size} sources.")
            sources.forEach { contentResolver.persist(it.uri) }
        }
}