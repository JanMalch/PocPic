package io.github.janmalch.pocpic.core

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.janmalch.pocpic.data.SourceDao
import io.github.janmalch.pocpic.data.SourceEntity
import io.github.janmalch.pocpic.shared.IoDispatcher
import io.github.janmalch.pocpic.shared.getDocumentFileOrThrow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

interface PictureRepository {
    fun watch(): Flow<Picture?>

    suspend fun reroll()
}

@Singleton
class AndroidPictureRepository @Inject constructor(
    private val dao: SourceDao,
    private val contentResolver: ContentResolver,
    @ApplicationContext private val context: Context,
    private val random: Random,
    private val logger: Logger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val selectedPicture: SelectedPicture,
) : PictureRepository, SourceRepository {


    override fun watchAll(): Flow<List<SourceEntity>> = dao.watchAll()

    override suspend fun insert(source: SourceEntity) {
        dao.insert(source)
        contentResolver.persist(source.uri)
        if (dao.watchAll().first().size == 1) {
            reroll()
        }
    }

    override suspend fun update(source: SourceEntity) {
        dao.update(source)
        reroll()
    }

    override suspend fun remove(source: SourceEntity) {
        dao.remove(source.id)
        contentResolver.release(source.uri)
        reroll()
    }

    override fun watch(): Flow<Picture?> = selectedPicture.watch()

    override suspend fun reroll() {
        logger.info("Getting new random picture.")
        withContext(ioDispatcher) {
            try {
                val current = selectedPicture.watch().first()
                val source = pickSource(
                    sources = watchAll().first(),
                    previousSourceId = current?.sourceId,
                )
                logger.info("Picked source: ${source.label}")
                val picture = pickPicture(source = source, previousUri = current?.uri)
                logger.info("Picked picture: ${picture.uri}")
                selectedPicture.set(picture)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    logger.error("Unknown error while getting random picture.", e)
                }
                throw e
            }
        }
    }

    private fun pickPicture(source: SourceEntity, previousUri: Uri?): Picture {
        // This is basically only relevant if nextSource is the same directory as before.
        // Try a few times to avoid the same file from the directory.
        var attempts = 5

        var next = pickPictureFromSource(source)
        if (next.second) {
            return next.first
        }
        while (attempts > 0 && next.first.uri == previousUri) {
            next = pickPictureFromSource(source)
            attempts--
        }

        return next.first
    }

    /**
     * @return the picked [Picture] and `true` if it's the only file in the source.
     */
    private fun pickPictureFromSource(source: SourceEntity): Pair<Picture, Boolean> {
        val document = context.getDocumentFileOrThrow(source.uri)
        val files = document
            .listFiles()
            .filter { it.isFile }
        val file = if (files.size == 1) {
            files.first()
        } else {
            files.random(random)
        }

        return Picture(
            uri = file.uri,
            label = source.label,
            sourceId = source.id,
            date = file.lastModified().toLocalDateTime()
        ) to (files.size == 1)
    }


    private fun pickSource(sources: List<SourceEntity>, previousSourceId: Long?): SourceEntity {
        if (sources.isEmpty()) {
            throw NoSuchElementException("No sources available to pick from.")
        }
        if (sources.size == 1) {
            return sources[0]
        }

        val nextWeighted = random.prepareNextWeighted(sources, SourceEntity::weight)
        var attempts = minOf(sources.size * 3, 20)

        var source = nextWeighted()
        while (attempts > 0 && source.id == previousSourceId) {
            source = nextWeighted()
            attempts--
        }

        return source
    }
}

private fun Long.toLocalDateTime(): LocalDateTime =
    try {
        Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .toKotlinLocalDateTime()
    } catch (e: Exception) {
        LocalDateTime(0, Month.JANUARY, 0, 0, 0)
    }