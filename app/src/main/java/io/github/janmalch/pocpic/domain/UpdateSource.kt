package io.github.janmalch.pocpic.domain

import android.content.ContentResolver
import android.util.Log
import io.github.janmalch.pocpic.data.SourceDao
import io.github.janmalch.pocpic.data.SourceEntity
import io.github.janmalch.pocpic.shared.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateSource @Inject constructor(
    private val dao: SourceDao,
    private val contentResolver: ContentResolver,
    private val isRemoteRedirect: IsRemoteRedirect,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    suspend operator fun invoke(update: Source, previous: Source) {
        if (update == previous) {
            return
        }

        withContext(ioDispatcher) {
            dao.update(
                SourceEntity(
                    id = update.id,
                    label = update.label,
                    uri = update.uri,
                    type = update.type,
                    weight = update.weight,
                    isRemoteRedirect = isRemoteRedirect(update),
                )
            )

            if (update.uri != previous.uri) {
                Log.d("UpdateSource", "Releasing previous URI and persisting new URI.")
                contentResolver.persist(update.uri)
                contentResolver.release(previous.uri)
            }
        }
    }
}