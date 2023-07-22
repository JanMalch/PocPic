package io.github.janmalch.pocpic.domain.resolvers

import io.github.janmalch.pocpic.data.SourceEntity
import io.github.janmalch.pocpic.domain.Picture
import io.github.janmalch.pocpic.domain.Source
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ResolveRemoteSource @Inject constructor() {

    operator fun invoke(source: Source): Picture {
        require(source.type == SourceEntity.Type.REMOTE)

        // avoid Glide cache by appending some #fragment, that is ignored by the server
        val uri = if (source.isRemoteRedirect) source.uri.buildUpon()
            .fragment(System.currentTimeMillis().toString())
            .build()
        else source.uri

        return Picture(
            uri = uri,
            label = source.label,
            sourceId = source.id,
            // TODO: check Last-Modified here or in UI via Glide hook?
            date = null,
        )
    }
}