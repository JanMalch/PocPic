package io.github.janmalch.pocpic.domain

import io.github.janmalch.pocpic.data.SourceDao
import io.github.janmalch.pocpic.shared.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetSources @Inject constructor(
    private val dao: SourceDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() = withContext(ioDispatcher) {
        dao.findAll().map {
            Source(
                id = it.id,
                label = it.label,
                uri = it.uri,
                type = it.type,
                weight = it.weight,
                isRemoteRedirect = it.isRemoteRedirect,
            )
        }
    }

}