package io.github.janmalch.pocpic.core

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import javax.inject.Inject

interface Logger {

    suspend fun info(message: String)
    suspend fun error(message: String, throwable: Throwable)
    suspend fun getAll(): List<Entry>

    @Serializable
    data class Entry(
        val timestamp: LocalDateTime,
        val message: String,
        val stackTrace: String?
    )

}

@OptIn(ExperimentalSerializationApi::class)
class PersistentLogger @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clock: Clock
) : Logger {

    private val mutex = Mutex()
    private val file = File(context.cacheDir, "log.json")
    private lateinit var log: List<Logger.Entry>

    private fun prepare() {
        if (::log.isInitialized) return
        if (file.exists()) {
            log = Json.decodeFromStream(file.inputStream())
        } else {
            file.createNewFile()
            log = emptyList()
        }
    }

    private suspend fun doLog(message: String, stackTrace: String?) = mutex.withLock {
        try {
            prepare()
            val entry = Logger.Entry(
                timestamp = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                message = message,
                stackTrace = stackTrace,
            )
            log = buildList(log.size + 1) {
                add(entry)
                addAll(log)
            }.take(LIMIT)
            Json.encodeToStream(log, file.outputStream())
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.w("PersistentLogger", "Error while adding log entry.", e)
        }
    }

    override suspend fun info(message: String) {
        Log.i("PocPic", message)
        doLog(message, null)
    }

    override suspend fun error(message: String, throwable: Throwable) {
        Log.e("PocPic", message, throwable)
        doLog(message, throwable.stackTraceToString())
    }

    override suspend fun getAll(): List<Logger.Entry> = mutex.withLock {
        prepare()
        log.toList()
    }

    companion object {
        private const val LIMIT = 100
    }

}
