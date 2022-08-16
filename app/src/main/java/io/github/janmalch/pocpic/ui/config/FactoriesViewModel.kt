package io.github.janmalch.pocpic.ui.config

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.janmalch.pocpic.data.PictureSourceFactory
import io.github.janmalch.pocpic.data.SourceFactoryConfig
import io.github.janmalch.pocpic.data.SourceFactoryConfigRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FactoriesViewModel @Inject constructor(
    private val repository: SourceFactoryConfigRepository,
    @ApplicationContext appContext: Context
) : ViewModel() {

    val factories = repository.watchAll().map { list ->
        list.associateWith { PictureSourceFactory.fromConfig(it, appContext) }
    }

    fun insertOrReplace(vararg config: SourceFactoryConfig) = viewModelScope.launch {
        repository.insertOrReplace(*config)
    }

    fun remove(vararg config: SourceFactoryConfig) = viewModelScope.launch {
        repository.remove(*config)
    }
}
