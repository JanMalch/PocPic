package io.github.janmalch.pocpic.ui.sources

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyle
import io.github.janmalch.pocpic.domain.Source
import kotlinx.coroutines.flow.collectLatest

sealed interface SourceDialogIntent {
    data class DeleteSource(val source: Source) : SourceDialogIntent
    data class InsertSources(val sources: List<Source>) : SourceDialogIntent
    data class UpdateSource(val update: Source, val previous: Source) : SourceDialogIntent
}

@Destination(style = DestinationStyle.Dialog::class)
@Composable
fun SourceDialog(
    navigator: DestinationsNavigator,
    source: Source? = null,
    vm: SourcesViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        vm.closeScreen.collectLatest { navigator.navigateUp() }
    }
    SourceDialog(source = source, dispatch = {
        when (it) {
            is SourceDialogIntent.DeleteSource -> vm.remove(it.source)
            is SourceDialogIntent.InsertSources -> vm.insert(it.sources)
            is SourceDialogIntent.UpdateSource -> vm.update(it.update, it.previous)
        }
    })
}

@Composable
fun SourceDialog(
    source: Source?,
    dispatch: (SourceDialogIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(24.dp),
    ) {
        if (source != null) {
            EditDialogContent(source = source, dispatch = dispatch)
        } else {
            AddDialogContent(dispatch = dispatch)
        }
    }
}