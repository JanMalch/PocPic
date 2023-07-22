package io.github.janmalch.pocpic.shared

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlin.random.Random

@InstallIn(SingletonComponent::class)
@Module
object RandomModule {

    @Provides
    fun provideDefaultRandom(): Random = Random.Default
}