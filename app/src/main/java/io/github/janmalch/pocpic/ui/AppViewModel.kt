package io.github.janmalch.pocpic.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.pocpic.core.AppRepository
import io.github.janmalch.pocpic.models.Picture
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed interface AppUiState {
    data object Initializing : AppUiState
    data object Onboarding : AppUiState
    data class Ready(val picture: Picture?) : AppUiState
}

private const val TAG = "MainViewModel"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: AppRepository,
) : ViewModel() {

    val appUiState = repository.watchSourceUri()
        .flatMapLatest { source ->
            if (source == null) {
                flowOf(AppUiState.Onboarding)
            } else {
                repository.watchSelectedPicture().map { picture ->
                    AppUiState.Ready(picture)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = AppUiState.Initializing,
        )

    private val _changeError = Channel<Unit>()
    val changeError = _changeError.receiveAsFlow() // FIXME: use

    fun changeDirectory(source: Uri) {
        viewModelScope.launch {
            try {
                repository.setSourceUri(source)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Failed to change directory.", e)
                // FIXME: feedback like _changeError.send(Unit)
            }
        }
    }

    fun changePicture() {
        viewModelScope.launch {
            try {
                repository.reroll()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Failed to change picture.", e)
                _changeError.send(Unit)
            }
        }
    }

}