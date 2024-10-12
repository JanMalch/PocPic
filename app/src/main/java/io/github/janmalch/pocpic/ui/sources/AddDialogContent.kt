package io.github.janmalch.pocpic.ui.sources

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.data.SourceEntity
import io.github.janmalch.pocpic.shared.getDocumentFileOrThrow
import io.github.janmalch.pocpic.ui.theme.Typography


@Composable
fun AddDialogContent(
    dispatch: (SourceDialogIntent) -> Unit
) {
    val context = LocalContext.current
    var uri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var label by rememberSaveable { mutableStateOf("") }

    val pickFolderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) {
        // FIXME: on cancel this is null, then we have to exit dialog
        uri = it
    }

    LaunchedEffect(Unit) {
        if (uri == null) {
            pickFolderLauncher.launch(null)
        }
    }

    LaunchedEffect(uri) {
        label = when (val u = uri) {
            null -> ""
            else -> context.getDocumentFileOrThrow(u).name ?: "PocPic"
        }
    }

    Text(stringResource(R.string.new_source), style = Typography.titleLarge)
    Spacer(Modifier.height(16.dp))

    LabelInput(value = label, onChange = { label = it })
    Spacer(Modifier.height(16.dp))

    var weight by rememberSaveable { mutableIntStateOf(1) }
    WeightInput(value = weight, onChange = { weight = it })
    Spacer(Modifier.height(16.dp))

    val isValid = rememberSaveable(uri, label, weight) {
        uri != null && label.isNotBlank() && weight > 0
    }

    Spacer(Modifier.height(16.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(
            enabled = isValid,
            onClick = {
                val u = uri ?: return@TextButton
                val source = SourceEntity(
                    id = 0L,
                    label = label.trim(),
                    uri = u,
                    weight = weight,
                )
                dispatch(SourceDialogIntent.InsertSources(source))
            }) {
            Text(stringResource(R.string.save))
        }
    }
}