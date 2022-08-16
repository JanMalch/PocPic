package io.github.janmalch.pocpic.data

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.map
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class SourceFactoryConfigRepository @Inject constructor(private val dao: SourceFactoryConfigDao) {

    @WorkerThread
    fun watchAll() = dao.watchAll().map { it.toImmutableList() }

    @WorkerThread
    suspend fun getAll() = dao.getAll().toImmutableList()

    @WorkerThread
    suspend fun insertOrReplace(vararg configs: SourceFactoryConfig) = dao.insertOrReplace(*configs)

    @WorkerThread
    suspend fun remove(vararg configs: SourceFactoryConfig) = dao.remove(*configs)

    companion object {
        fun createInstance(context: Context) = SourceFactoryConfigRepository(
            AppDatabase.getDatabase(context).configsDao()
        )
    }
}
