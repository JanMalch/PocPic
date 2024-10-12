package io.github.janmalch.pocpic.widget.configuration

import android.content.Context
import androidx.annotation.Px
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// FIXME: migrate!?
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_configuration")

class WidgetConfigurationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val MAX_WIDTH = intPreferencesKey("max_width")
        private val MAX_HEIGHT = intPreferencesKey("max_height")
        private val SHAPE = stringPreferencesKey("shape")
    }

    fun watch(): Flow<WidgetData> {
        return context.dataStore.data.map { it.read() }
    }

    suspend fun set(@Px width: Int, @Px height: Int) {
        if (width <= 0 || height <= 0) return
        context.dataStore.edit {
            it[MAX_WIDTH] = width
            it[MAX_HEIGHT] = height
        }
    }

    suspend fun set(shape: WidgetData.Shape) {
        context.dataStore.edit {
            it[SHAPE] = shape.name
        }
    }

    private fun Preferences.read(): WidgetData {
        var width = this[MAX_WIDTH]?.takeIf { it > 0 }
        var height = this[MAX_HEIGHT]?.takeIf { it > 0 }
        val shape = this[SHAPE]?.let(WidgetData.Shape.Companion::find)
            ?: WidgetData.Shape.CenterCropRectangle
        if (width == null || height == null) {
            val dm = context.resources.displayMetrics
            width = dm.widthPixels
            height = dm.heightPixels
        }
        return WidgetData(width, height, shape)
    }
}
