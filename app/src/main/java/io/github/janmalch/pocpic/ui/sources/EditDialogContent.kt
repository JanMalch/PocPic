package io.github.janmalch.pocpic.ui.sources

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.domain.Source
import io.github.janmalch.pocpic.ui.theme.Typography


@Composable
fun ColumnScope.EditDialogContent(
    source: Source,
    dispatch: (SourceDialogIntent) -> Unit
) {

    Row(verticalAlignment = Alignment.CenterVertically) {
        SourceTypeIcon(source.type)
        Spacer(Modifier.width(12.dp))
        Text(stringResource(R.string.edit_source), style = Typography.titleLarge)
    }
    Spacer(Modifier.height(16.dp))

    var type by rememberSaveable { mutableStateOf(source.type) }
    var uri by rememberSaveable { mutableStateOf(source.uri) }
    UriInput(type = type, uri = uri, onChange = { t, u ->
        type = t
        uri = u
    })
    Spacer(Modifier.height(16.dp))

    var label by rememberSaveable { mutableStateOf(source.label) }
    LabelInput(value = label, onChange = { label = it })
    Spacer(Modifier.height(16.dp))

    var weight by rememberSaveable { mutableIntStateOf(source.weight) }
    WeightInput(value = weight, onChange = { weight = it })
    Spacer(Modifier.height(16.dp))

    val isValid = rememberSaveable(uri, label, weight) {
        uri.toString().isNotBlank() && label.isNotBlank() && weight > 0
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { dispatch(SourceDialogIntent.DeleteSource(source)) }) {
            Text(stringResource(R.string.delete))
        }

        TextButton(
            onClick = {
                val update = source.copy(
                    type = type,
                    label = label.trim(),
                    weight = weight,
                    uri = uri,
                )

                dispatch(SourceDialogIntent.UpdateSource(update, source))
            },
            enabled = isValid
        ) {
            Text(stringResource(R.string.save))
        }
    }
}