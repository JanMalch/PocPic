package io.github.janmalch.pocpic.ui.photo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.pocpic.data.SourceProvider
import javax.inject.Inject

@HiltViewModel
class CurrentSourceViewModel @Inject constructor(
    private val sourceProvider: SourceProvider,
) : ViewModel() {
    private val nextSourceTrigger = MutableLiveData(Unit)
    private var isFirstEmit = true

    val source = nextSourceTrigger.switchMap {
        liveData {
            val next = sourceProvider.yieldSource(useStoredSource = isFirstEmit)
            isFirstEmit = false
            emit(next)
        }
    }

    fun next() {
        nextSourceTrigger.value = Unit
    }
}
