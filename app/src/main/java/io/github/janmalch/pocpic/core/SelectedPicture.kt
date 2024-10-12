package io.github.janmalch.pocpic.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.janmalch.pocpic.shared.toLocalDateTimeOrNull
import io.github.janmalch.pocpic.shared.toUriOrNull
import io.github.janmalch.pocpic.widget.PocPicWidget
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "history")

@Singleton
class SelectedPicture @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: Logger,
) {

    companion object {
        private val SOURCE_ID = longPreferencesKey("source_id")
        private val URI = stringPreferencesKey("uri")
        private val LABEL = stringPreferencesKey("label")
        private val DATE = stringPreferencesKey("date")
    }

    suspend fun get(): Picture? {
        return context.dataStore.data.firstOrNull()?.read()
    }

    fun watch(): Flow<Picture?> {
        return context.dataStore.data.map { it.read() }
    }

    private fun Preferences.read(): Picture? {
        val uri = this[URI].toUriOrNull() ?: return null
        val label = this[LABEL] ?: return null
        val sourceId = this[SOURCE_ID] ?: return null
        val date = this[DATE]?.takeIf(String::isNotEmpty).toLocalDateTimeOrNull()
        return Picture(uri, label, sourceId, date)
    }

    suspend fun set(picture: Picture): Unit = try {
        context.dataStore.edit {
            it[SOURCE_ID] = picture.sourceId
            it[URI] = picture.uri.toString()
            it[LABEL] = picture.label
            it[DATE] = picture.date?.toString() ?: ""
        }
        PocPicWidget().updateAll(context)
        logger.info("Notified widgets of update.")
    } catch (e: Exception) {
        if (e !is CancellationException) {
            logger.error("Error while setting current picture and notifying widget.", e)
        }
        throw e
    }
}
