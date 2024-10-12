package io.github.janmalch.pocpic.core

import io.github.janmalch.pocpic.data.SourceEntity
import kotlinx.coroutines.flow.Flow

interface SourceRepository {

    fun watchAll(): Flow<List<SourceEntity>>

    suspend fun insert(source: SourceEntity)

    suspend fun update(source: SourceEntity)

    suspend fun remove(source: SourceEntity)

}
