package io.github.janmalch.pocpic.core

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.random.Random

@InstallIn(SingletonComponent::class)
@Module
abstract class CoreModule {

    @Binds
    abstract fun bindsRerollPicture(impl: AndroidRerollPicture): RerollPicture

    companion object {
        @Provides
        fun provideDefaultRandom(): Random = Random.Default

        @Provides
        @Singleton
        fun providesAppRepository(
            rerollPicture: RerollPicture,
            @ApplicationContext context: Context,
        ): AppRepository = AndroidAppRepository.getInstance(context, rerollPicture)

        @Provides
        @Singleton
        fun providesWidgetRepository(
            @ApplicationContext context: Context,
        ): WidgetRepository = AndroidWidgetRepository.getInstance(context)
    }
}