package io.github.janmalch.pocpic.domain

import io.github.janmalch.pocpic.data.SourceEntity
import javax.inject.Inject

class IsRemoteRedirect @Inject constructor(
    private val getRedirectLocation: GetRedirectLocation
) {
    suspend operator fun invoke(source: Source): Boolean {
        if (source.type != SourceEntity.Type.REMOTE) return false
        return getRedirectLocation(source) != null
    }
}