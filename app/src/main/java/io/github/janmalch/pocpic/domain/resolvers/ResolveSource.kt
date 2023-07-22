package io.github.janmalch.pocpic.domain.resolvers

import io.github.janmalch.pocpic.data.SourceEntity
import io.github.janmalch.pocpic.domain.Picture
import io.github.janmalch.pocpic.domain.Source
import javax.inject.Inject

class ResolveSource @Inject constructor(
    private val resolveRemoteSource: ResolveRemoteSource,
    private val resolveLocalFileSource: ResolveLocalFileSource,
    private val resolveLocalDirectorySource: ResolveLocalDirectorySource,
) {

    suspend operator fun invoke(source: Source): Picture = when (source.type) {
        SourceEntity.Type.REMOTE -> resolveRemoteSource(source)
        SourceEntity.Type.LOCAL_FILE -> resolveLocalFileSource(source)
        SourceEntity.Type.LOCAL_DIRECTORY -> resolveLocalDirectorySource(source)
    }
}