package io.github.janmalch.pocpic.widget.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.pocpic.core.PictureRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetConfigurationViewModel @Inject constructor(
    private val pictureRepository: PictureRepository,
) : ViewModel() {
    val selectedUri = pictureRepository.watch().map { it?.uri }

    fun reroll() {
        viewModelScope.launch {
            // FIXME: error handling
            pictureRepository.reroll()
        }
    }
}