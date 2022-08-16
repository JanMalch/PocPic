package io.github.janmalch.pocpic

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.janmalch.pocpic.data.PictureSourceFactory
import io.github.janmalch.pocpic.data.SourceFactoryConfig
import io.github.janmalch.pocpic.data.SourceFactoryConfigRepository
import io.github.janmalch.pocpic.extensions.combineWith
import io.github.janmalch.pocpic.extensions.randomUnlikeOrNull
import io.github.janmalch.pocpic.models.PictureSource
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: SourceFactoryConfigRepository,
    @ApplicationContext appContext: Context
) : ViewModel() {

    private var previousFactory: PictureSourceFactory? = null
    val factories = repository.watchAll().map { list ->
        list.associateWith { PictureSourceFactory.fromConfig(it, appContext) }
    }

    private val nextSourceTrigger = MutableLiveData<Unit>()
    private val triggeredSources = factories.combineWith(nextSourceTrigger) { factories, _ ->
        previousFactory = factories
            ?.values
            ?.randomUnlikeOrNull(previousFactory)
        previousFactory?.nextPictureSource()
    }

    private var nextManualSource: PictureSource? = null

    val source: LiveData<PictureSource?> = liveData {
        val consumedSource = nextManualSource?.let { emit(it) } != null
        if (consumedSource) {
            nextManualSource = null
        } else {
            emitSource(triggeredSources)
        }
    }

    fun insertOrReplace(vararg config: SourceFactoryConfig) = viewModelScope.launch {
        repository.insertOrReplace(*config)
    }

    fun remove(vararg config: SourceFactoryConfig) = viewModelScope.launch {
        repository.remove(*config)
    }

    fun nextSource() {
        nextSourceTrigger.value = nextSourceTrigger.value
    }

    fun provideInitialSource(source: PictureSource?) {
        nextManualSource = source
    }
}
