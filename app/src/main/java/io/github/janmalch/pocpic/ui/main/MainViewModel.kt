package io.github.janmalch.pocpic.ui.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.pocpic.domain.GetRandomPicture
import io.github.janmalch.pocpic.domain.Picture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getRandomPicture: GetRandomPicture
) : ViewModel() {

    private val trigger = MutableStateFlow(0)

    val currentSource: Flow<Picture?> = trigger.map { getRandomPicture(retrieveCurrent = it == 0) }

    fun changePicture() {
        trigger.value++
    }

}