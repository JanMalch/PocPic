package io.github.janmalch.pocpic.shared

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient

@InstallIn(SingletonComponent::class)
@Module
object HttpModule {
    @Provides
    fun providesOkHttpClient() = OkHttpClient()
}

