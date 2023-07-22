package io.github.janmalch.pocpic.domain

import io.github.janmalch.pocpic.data.SourceDao
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchSources @Inject constructor(private val dao: SourceDao) {

    operator fun invoke() = dao.watchAll().map { list ->
        list.map {
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