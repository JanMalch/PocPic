package io.github.janmalch.pocpic.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.janmalch.pocpic.models.PictureSource
import io.github.janmalch.pocpic.models.PictureSource.Preferences.getPictureSource
import io.github.janmalch.pocpic.models.PictureSource.Preferences.putPictureSource
import io.github.janmalch.pocpic.models.PictureSource.Preferences.removePictureSource
import io.github.janmalch.pocpic.widget.sendUpdateWidgetIntent
import javax.inject.Inject

internal fun <T> retryUntilNotEqual(
    current: T,
    attempts: Int,
    producer: () -> T,
): T {
    var next: T
    var remaining = attempts
    do {
        next = producer()
        remaining--
    } while (remaining > 0 && next == current)
    return next
}

class SourceProvider @Inject constructor(
    private val repository: SourceFactoryConfigRepository,
    @ApplicationContext private val appContext: Context
) {

    suspend fun yieldSource(useStoredSource: Boolean): PictureSource? {
        val prefs = appContext.getSharedPreferences(this::class.java.name, MODE_PRIVATE)
        val current = prefs.getStoredSource()

        if (useStoredSource && current != null) {
            sendUpdateWidgetIntent(appContext, current)
            return current
        }
        val factories = repository.getAll()
            .map { PictureSourceFactory.fromConfig(it, appContext) }

        val next = retryUntilNotEqual(
            current = current,
            attempts = 10,
        ) { getSourceFromFactories(factories) }

        prefs.storeSource(next)
        if (next != null) {
            sendUpdateWidgetIntent(appContext, next)
        }
        return next
    }

    private fun getSourceFromFactories(factories: List<PictureSourceFactory>): PictureSource? {
        return factories
            .randomOrNull()
            ?.nextPictureSource()
    }

    private fun SharedPreferences.getStoredSource(): PictureSource? {
        return this.getPictureSource(RECENT_KEY)
    }

    private fun SharedPreferences.deleteStoredSource() {
        this.edit {
            removePictureSource(RECENT_KEY)
        }
    }

    private fun SharedPreferences.storeSource(source: PictureSource?) {
        if (source == null) {
            this.deleteStoredSource()
            return
        }
        this.edit {
            putPictureSource(RECENT_KEY, source)
        }
    }

    companion object {
        private const val RECENT_KEY = "recent"

        fun createInstance(context: Context) = SourceProvider(
            SourceFactoryConfigRepository.createInstance(context.applicationContext),
            context.applicationContext,
        )
    }
}
