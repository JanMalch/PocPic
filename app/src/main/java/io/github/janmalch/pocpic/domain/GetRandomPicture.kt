package io.github.janmalch.pocpic.domain

import android.net.Uri
import io.github.janmalch.pocpic.domain.resolvers.ResolveSource
import io.github.janmalch.pocpic.shared.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

class GetRandomPicture @Inject constructor(
    private val resolveSource: ResolveSource,
    private val random: Random,
    private val selectedPicture: SelectedPicture,
    private val getSources: GetSources,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {

    suspend operator fun invoke(retrieveCurrent: Boolean = false): Picture? {
        val prev = selectedPicture.get()
        if (retrieveCurrent && prev != null) return prev

        val sources = getSources()
        if (sources.isEmpty()) return null

        return withContext(defaultDispatcher) {
            val nextSource = findNextSource(sources, prev?.sourceId)
            findNextPicture(nextSource, prev?.uri).also { selectedPicture.set(it) }
        }
    }

    private fun findNextSource(sources: List<Source>, previousSourceId: Long?): Source {
        if (sources.size == 1) {
            return sources[0]
        }

        val nextWeighted = random.prepareNextWeighted(sources, Source::weight)
        var attempts = minOf(sources.size * 3, 20)

        var source = nextWeighted()
        while (attempts > 0 && source.id == previousSourceId) {
            source = nextWeighted()
            attempts--
        }

        return source
    }

    private suspend fun findNextPicture(source: Source, previousUri: Uri?): Picture {
        // This is basically only relevant if nextSource is the same directory as before.
        // Try a few times to avoid the same file from the directory.
        var attempts = 5

        var next = resolveSource(source)
        while (attempts > 0 && next.uri == previousUri) {
            next = resolveSource(source)
            attempts--
        }

        return next
    }

}