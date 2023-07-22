package io.github.janmalch.pocpic.domain

import android.content.ContentResolver
import io.github.janmalch.pocpic.data.SourceDao
import io.github.janmalch.pocpic.shared.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoveSource @Inject constructor(
    private val dao: SourceDao,
    private val contentResolver: ContentResolver,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    suspend operator fun invoke(source: Source): Unit = withContext(ioDispatcher) {
        dao.remove(source.id)
        contentResolver.release(source.uri)
    }

}