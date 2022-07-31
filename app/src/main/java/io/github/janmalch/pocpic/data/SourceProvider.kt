package io.github.janmalch.pocpic.data

import android.content.Context
import io.github.janmalch.pocpic.models.PictureSource

class SourceProvider(
    private val repository: SourceFactoryConfigRepository,
    context: Context
) {

    private val applicationContext = context.applicationContext

    suspend fun yieldSource(): PictureSource? {
        return repository.getAll()
            .map { PictureSourceFactory.fromConfig(it, applicationContext) }
            .randomOrNull()
            ?.nextPictureSource()
    }

    companion object {
        fun createInstance(context: Context) = SourceProvider(SourceFactoryConfigRepository.createInstance(context.applicationContext), context)
    }
}
