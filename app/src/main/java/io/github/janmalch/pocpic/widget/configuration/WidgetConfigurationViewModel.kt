package io.github.janmalch.pocpic.widget.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.pocpic.domain.GetRandomPicture
import io.github.janmalch.pocpic.domain.SelectedPicture
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetConfigurationViewModel @Inject constructor(
    selectedPicture: SelectedPicture,
    private val getRandomPicture: GetRandomPicture,
) : ViewModel() {
    val selectedUri = selectedPicture.watch().map { it?.uri }

    fun reroll() {
        viewModelScope.launch {
            getRandomPicture()
        }
    }
}