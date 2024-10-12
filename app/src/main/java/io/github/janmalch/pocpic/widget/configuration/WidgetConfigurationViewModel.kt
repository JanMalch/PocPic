package io.github.janmalch.pocpic.widget.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.pocpic.core.AppRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetConfigurationViewModel @Inject constructor(
    private val repository: AppRepository,
) : ViewModel() {
    val selectedUri = repository.watchSelectedPicture().map { it?.fileUri }

    fun reroll() {
        viewModelScope.launch {
            // FIXME: error handling
            repository.reroll()
        }
    }
}