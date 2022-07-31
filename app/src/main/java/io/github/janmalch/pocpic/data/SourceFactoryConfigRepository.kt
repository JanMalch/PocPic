package io.github.janmalch.pocpic.data

import android.content.Context
import androidx.annotation.WorkerThread
import javax.inject.Inject

class SourceFactoryConfigRepository @Inject constructor(private val dao: SourceFactoryConfigDao) {

    @WorkerThread
    fun watchAll() = dao.watchAll()

    @WorkerThread
    suspend fun getAll() = dao.getAll()

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
