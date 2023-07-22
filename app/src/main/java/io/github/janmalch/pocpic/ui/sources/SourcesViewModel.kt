package io.github.janmalch.pocpic.ui.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.pocpic.domain.InsertSources
import io.github.janmalch.pocpic.domain.RemoveSource
import io.github.janmalch.pocpic.domain.Source
import io.github.janmalch.pocpic.domain.UpdateSource
import io.github.janmalch.pocpic.domain.WatchSources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SourcesViewModel @Inject constructor(
    watchSources: WatchSources,
    private val insertSources: InsertSources,
    private val updateSource: UpdateSource,
    private val removeSource: RemoveSource,
) : ViewModel() {

    val sources = watchSources()

    private val _closeScreen = MutableSharedFlow<Unit>(replay = 0)
    val closeScreen: Flow<Unit> = _closeScreen

    fun insert(sources: List<Source>) {
        viewModelScope.launch {
            insertSources.invoke(sources)
            _closeScreen.emit(Unit)
        }
    }

    fun update(update: Source, previous: Source) {
        viewModelScope.launch {
            updateSource.invoke(update, previous)
            _closeScreen.emit(Unit)
        }
    }

    fun remove(source: Source) {
        viewModelScope.launch {
            removeSource.invoke(source)
            _closeScreen.emit(Unit)
        }
    }


}