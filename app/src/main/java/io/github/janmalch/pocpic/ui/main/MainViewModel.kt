package io.github.janmalch.pocpic.ui.main

import android.content.ContentResolver
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.pocpic.core.Picture
import io.github.janmalch.pocpic.core.PictureRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val pictureRepository: PictureRepository,
    private val contentResolver: ContentResolver,
) : ViewModel() {

    val currentSource: StateFlow<Picture?> = pictureRepository.watch()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null,
        )

    private val _changeError = Channel<Unit>()
    val changeError = _changeError.receiveAsFlow()

    private val _shareError = Channel<Unit>()
    val shareError = _shareError.receiveAsFlow()

    private val _share = Channel<Intent>()
    val share = _share.receiveAsFlow()

    fun shareCurrentSource() {
        val uri = currentSource.value?.uri?.takeIf { it.scheme == "content" } ?: return
        viewModelScope.launch {
            try {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = contentResolver.getType(uri) ?: "image/jpeg"
                }
                _share.send(intent)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("MainViewModel", "Failed to share picture.", e)
                _shareError.send(Unit)
            }
        }
    }

    fun changePicture() {
        viewModelScope.launch {
            try {
                pictureRepository.reroll()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("MainViewModel", "Failed to change picture.", e)
                _changeError.send(Unit)
            }
        }
    }

}