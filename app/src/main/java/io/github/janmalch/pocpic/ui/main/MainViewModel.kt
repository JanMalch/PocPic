package io.github.janmalch.pocpic.ui.main

import android.content.ContentResolver
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.pocpic.domain.GetRandomPicture
import io.github.janmalch.pocpic.domain.Picture
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getRandomPicture: GetRandomPicture,
    private val contentResolver: ContentResolver,
) : ViewModel() {

    private val trigger = MutableStateFlow(0)

    val currentSource: StateFlow<Picture?> = trigger
        .map { getRandomPicture(retrieveCurrent = it == 0) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null,
        )

    private val _share = MutableSharedFlow<Intent>()
    val share = _share.asSharedFlow()

    fun shareCurrentSource() {
        val uri = currentSource.value?.uri?.takeIf { it.scheme == "content" } ?: return
        viewModelScope.launch {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = contentResolver.getType(uri) ?: "image/jpeg"
            }

            _share.emit(intent)
        }
    }

    fun changePicture() {
        trigger.value++
    }

}