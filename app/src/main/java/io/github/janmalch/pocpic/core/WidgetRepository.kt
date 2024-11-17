package io.github.janmalch.pocpic.core

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import io.github.janmalch.pocpic.models.WidgetConfiguration
import io.github.janmalch.pocpic.widget.UpdateWidgetWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

interface WidgetRepository {
    fun watchWidgetConfiguration(): Flow<WidgetConfiguration>

    suspend fun updateWidgetConfiguration(block: WidgetConfiguration.() -> WidgetConfiguration)

    suspend fun enqueueWork()
}

private val Context.widgetStore by dataStore(
    fileName = "widget.pb",
    serializer = WidgetConfigurationSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { WidgetConfigurationSerializer.defaultValue },
)

private const val TAG = "AndroidWidgetRepository"

class AndroidWidgetRepository private constructor(
    private val context: Context
) : WidgetRepository {

    override fun watchWidgetConfiguration(): Flow<WidgetConfiguration> =
        context.widgetStore.data.distinctUntilChanged()

    override suspend fun updateWidgetConfiguration(block: WidgetConfiguration.() -> WidgetConfiguration) {
        val previousInterval = watchWidgetConfiguration().first().intervalInMinutes.minutes
        context.widgetStore.updateData { it.block() }
        val newInterval = watchWidgetConfiguration().first().intervalInMinutes.minutes
        if (newInterval != previousInterval) {
            UpdateWidgetWorker.enqueue(context, newInterval.toJavaDuration())
        }
    }

    override suspend fun enqueueWork() {
        val interval = watchWidgetConfiguration().first().intervalInMinutes.minutes
        UpdateWidgetWorker.enqueue(context, interval.toJavaDuration())
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: AndroidWidgetRepository? = null

        fun getInstance(context: Context): AndroidWidgetRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AndroidWidgetRepository(
                    context.applicationContext,
                ).also { INSTANCE = it }
            }
    }

}