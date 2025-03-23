package io.github.janmalch.pocpic.core

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.IOException
import androidx.datastore.core.Serializer
import io.github.janmalch.pocpic.models.AppData
import io.github.janmalch.pocpic.models.PictureState
import io.github.janmalch.pocpic.models.WidgetConfiguration
import java.io.InputStream
import java.io.OutputStream
import kotlin.time.Duration.Companion.hours

object AppDataSerializer : Serializer<AppData> {
    override val defaultValue: AppData = AppData.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AppData {
        try {
            return AppData.parseFrom(input)
        } catch (exception: IOException) {
            throw CorruptionException("Cannot read proto of AppDataSerializer.", exception)
        }
    }

    override suspend fun writeTo(t: AppData, output: OutputStream) = t.writeTo(output)
}

object PictureStateSerializer : Serializer<PictureState> {
    override val defaultValue: PictureState = PictureState.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): PictureState {
        try {
            return PictureState.parseFrom(input)
        } catch (exception: IOException) {
            throw CorruptionException("Cannot read proto of PictureState.", exception)
        }
    }

    override suspend fun writeTo(t: PictureState, output: OutputStream) = t.writeTo(output)
}

object WidgetConfigurationSerializer : Serializer<WidgetConfiguration> {
    override val defaultValue: WidgetConfiguration = WidgetConfiguration.getDefaultInstance()
        .toBuilder()
        .setIntervalInMinutes(24.hours.inWholeMinutes)
        .setCornerRadius(32).build()

    override suspend fun readFrom(input: InputStream): WidgetConfiguration {
        try {
            return WidgetConfiguration.parseFrom(input)
        } catch (exception: IOException) {
            throw CorruptionException("Cannot read proto of WidgetConfiguration.", exception)
        }
    }

    override suspend fun writeTo(t: WidgetConfiguration, output: OutputStream) = t.writeTo(output)
}
