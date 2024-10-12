package io.github.janmalch.pocpic.ui.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.pocpic.core.Logger
import io.github.janmalch.pocpic.core.SourceRepository
import io.github.janmalch.pocpic.data.SourceEntity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SourcesViewModel @Inject constructor(
    private val sourceRepository: SourceRepository,
    private val logger: Logger,
) : ViewModel() {

    // FIXME: temporary
    val logs = suspend { logger.getAll() }.asFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = emptyList(),
        )

    val sources = sourceRepository.watchAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _closeScreen = Channel<Unit>()
    val closeScreen: Flow<Unit> = _closeScreen.receiveAsFlow()

    // FIXME: error feedback

    fun insert(source: SourceEntity) {
        viewModelScope.launch {
            sourceRepository.insert(source)
            _closeScreen.send(Unit)
        }
    }

    fun update(update: SourceEntity) {
        viewModelScope.launch {
            sourceRepository.update(update)
            _closeScreen.send(Unit)
        }
    }

    fun remove(source: SourceEntity) {
        viewModelScope.launch {
            sourceRepository.remove(source)
            _closeScreen.send(Unit)
        }
    }


}