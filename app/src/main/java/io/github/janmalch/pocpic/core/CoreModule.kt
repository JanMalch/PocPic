package io.github.janmalch.pocpic.core

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface CoreModule {

    @Singleton
    @Binds
    fun bindsLogger(impl: PersistentLogger): Logger

    @Binds
    fun bindsSourceRepository(impl: AndroidPictureRepository): SourceRepository

    @Binds
    fun bindsPictureRepository(impl: AndroidPictureRepository): PictureRepository
}