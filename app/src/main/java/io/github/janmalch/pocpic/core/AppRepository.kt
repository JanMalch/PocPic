package io.github.janmalch.pocpic.core

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import androidx.glance.appwidget.updateAll
import io.github.janmalch.pocpic.models.AppData
import io.github.janmalch.pocpic.models.Picture
import io.github.janmalch.pocpic.models.PictureState
import io.github.janmalch.pocpic.models.WidgetConfiguration
import io.github.janmalch.pocpic.models.pictureOrNull
import io.github.janmalch.pocpic.widget.PocPicWidget
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface AppRepository {

    fun watchSourceUri(): Flow<Uri?>

    fun watchSelectedPicture(): Flow<Picture?>

    fun watchWidgetConfiguration(): Flow<WidgetConfiguration?>

    suspend fun setSourceUri(uri: Uri)

    suspend fun reroll()

    suspend fun updateWidgetConfiguration(block: WidgetConfiguration.() -> WidgetConfiguration)

}

private val Context.appDataStore by dataStore(
    fileName = "app_data.pb",
    serializer = AppDataSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { AppDataSerializer.defaultValue },
)

private val Context.pictureStore by dataStore(
    fileName = "picture.pb",
    serializer = PictureStateSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { PictureStateSerializer.defaultValue },
)

private val Context.widgetStore by dataStore(
    fileName = "widget.pb",
    serializer = WidgetConfigurationSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { WidgetConfigurationSerializer.defaultValue },
)

private const val TAG = "AndroidAppRepository"

class AndroidAppRepository private constructor(
    private val context: Context,
    private val rerollPicture: RerollPicture,
) : AppRepository {

    override fun watchSourceUri(): Flow<Uri?> =
        context.appDataStore.data.map {
            it.directoryUri?.takeIf(String::isNotBlank)?.let(Uri::parse)
        }.distinctUntilChanged()

    override fun watchSelectedPicture(): Flow<Picture?> =
        context.pictureStore.data.map { it.pictureOrNull }.distinctUntilChanged()

    override fun watchWidgetConfiguration(): Flow<WidgetConfiguration?> =
        context.widgetStore.data.distinctUntilChanged()

    override suspend fun setSourceUri(uri: Uri) {
        val contentResolver = context.contentResolver
        getSourceUri()?.also { contentResolver.release(it) }
        contentResolver.persist(uri)

        context.appDataStore.updateData {
            AppData.newBuilder().setDirectoryUri(uri.toString()).build()
        }
        reroll()
    }

    override suspend fun updateWidgetConfiguration(block: WidgetConfiguration.() -> WidgetConfiguration) {
        context.widgetStore.updateData { it.block() }
    }

    override suspend fun reroll() {
        try {
            val source = getSourceUri() ?: return
            val previous = watchSelectedPicture().first()
            val next = rerollPicture(source, previous)
            Log.d(TAG, "Reroll successful: ${previous?.fileName} -> ${next?.fileName}")
            context.pictureStore.updateData { PictureState.newBuilder().setPicture(next).build() }
            PocPicWidget().updateAll(context)
        } catch (e: Exception) {
            if (e !is CancellationException) {
                Log.d(TAG, "Reroll failed.", e)
            }
            throw e
        }
    }

    private suspend fun getSourceUri() = watchSourceUri().first()

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: AndroidAppRepository? = null

        fun getInstance(context: Context, rerollPicture: RerollPicture): AndroidAppRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AndroidAppRepository(
                    context.applicationContext,
                    rerollPicture
                ).also { INSTANCE = it }
            }

    }
}
