package io.github.janmalch.pocpic.ui.sources

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyle
import io.github.janmalch.pocpic.data.SourceEntity
import io.github.janmalch.pocpic.ui.CollectAsEvent

sealed interface SourceDialogIntent {
    data class DeleteSource(val source: SourceEntity) : SourceDialogIntent
    data class InsertSources(val source: SourceEntity) : SourceDialogIntent
    data class UpdateSource(val update: SourceEntity) : SourceDialogIntent
}

@Destination(style = DestinationStyle.Dialog::class)
@Composable
fun SourceDialog(
    navigator: DestinationsNavigator,
    source: SourceEntity? = null,
    vm: SourcesViewModel = hiltViewModel()
) {
    CollectAsEvent(vm.closeScreen) { navigator.navigateUp() }
    SourceDialog(source = source, dispatch = {
        when (it) {
            is SourceDialogIntent.DeleteSource -> vm.remove(it.source)
            is SourceDialogIntent.InsertSources -> vm.insert(it.source)
            is SourceDialogIntent.UpdateSource -> vm.update(it.update)
        }
    })
}

@Composable
fun SourceDialog(
    source: SourceEntity?,
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