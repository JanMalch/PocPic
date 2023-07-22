package io.github.janmalch.pocpic.ui.sources

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.data.SourceEntity
import io.github.janmalch.pocpic.domain.Source
import io.github.janmalch.pocpic.ui.theme.Typography


@Composable
fun ColumnScope.AddDialogContent(
    dispatch: (SourceDialogIntent) -> Unit
) {
    Text(stringResource(R.string.new_source), style = Typography.titleLarge)
    Spacer(Modifier.height(16.dp))


    var type by rememberSaveable { mutableStateOf(SourceEntity.Type.REMOTE) }
    var uris by rememberSaveable { mutableStateOf(listOf<Uri>()) }
    MultiUriInput(type = type, uris = uris, onChange = { t, u ->
        type = t
        uris = u
    })
    Spacer(Modifier.height(16.dp))

    var label by rememberSaveable { mutableStateOf("") }
    LabelInput(value = label, onChange = { label = it })
    Spacer(Modifier.height(16.dp))

    var weight by rememberSaveable { mutableIntStateOf(1) }
    WeightInput(value = weight, onChange = { weight = it })
    Spacer(Modifier.height(16.dp))

    val isValid = rememberSaveable(uris, label, weight) {
        uris.isNotEmpty() && uris.all {
            it.toString().isNotBlank()
        } && label.isNotBlank() && weight > 0
    }

    Spacer(Modifier.height(16.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(
            enabled = isValid,
            onClick = {
                val sources = uris.mapIndexed { index, uri ->
                    Source(
                        label = (label + " ${"#${index + 1}".takeIf { uris.size > 1 } ?: ""}").trim(),
                        uri = uri,
                        type = type,
                        weight = weight,
                        isRemoteRedirect = false,
                    )
                }
                dispatch(SourceDialogIntent.InsertSources(sources))
            }) {
            Text(stringResource(R.string.save))
        }
    }
}