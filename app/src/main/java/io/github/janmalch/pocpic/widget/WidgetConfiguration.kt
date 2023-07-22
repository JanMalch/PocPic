package io.github.janmalch.pocpic.widget

import android.content.Context
import androidx.annotation.Px
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_configuration")

class WidgetConfiguration(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val MAX_WIDTH = intPreferencesKey("max_width")
    private val MAX_HEIGHT = intPreferencesKey("max_height")


    fun watch(): Flow<Pair<Int, Int>> {
        return context.dataStore.data.flowOn(ioDispatcher).map { it.read() }
    }

    suspend fun set(@Px width: Int, @Px height: Int) {
        if (width <= 0 || height <= 0) return
        withContext(ioDispatcher) {
            context.dataStore.edit {
                it[MAX_WIDTH] = width
                it[MAX_HEIGHT] = height
            }
        }
    }

    private fun Preferences.read(): Pair<Int, Int> {
        var width = this[MAX_WIDTH]?.takeIf { it > 0 }
        var height = this[MAX_HEIGHT]?.takeIf { it > 0 }
        if (width == null || height == null) {
            val dm = context.resources.displayMetrics
            width = dm.widthPixels
            height = dm.heightPixels
        }
        return width to height
    }
}