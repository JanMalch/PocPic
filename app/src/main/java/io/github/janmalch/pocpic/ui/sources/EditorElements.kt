package io.github.janmalch.pocpic.ui.sources

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.data.SourceEntity
import io.github.janmalch.pocpic.shared.toUriOrNull
import io.github.janmalch.pocpic.ui.theme.Typography

@Composable
fun LabelInput(value: String, onChange: (String) -> Unit) {
    val labelIsError = remember(value) { value.isBlank() }

    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it) },
        label = { Text(text = stringResource(id = R.string.required_label_input)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = labelIsError
    )
}

@Composable
fun ColumnScope.WeightInput(value: Int, onChange: (Int) -> Unit) {
    OutlinedTextField(
        value = value.toString(10),
        onValueChange = { onChange(it.trim().toIntOrNull(10)?.takeIf { n -> n > 0 } ?: value) },
        label = { Text(text = stringResource(id = R.string.required_weight_input)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    Spacer(Modifier.height(4.dp))
    Text(stringResource(R.string.weight_explanation), style = Typography.labelSmall)
}


@Composable
fun TypeSelection(
    type: SourceEntity.Type?,
    onChange: (SourceEntity.Type) -> Unit
) {
    // TODO: wait for segmented button group
    //   https://developer.android.com/jetpack/androidx/compose-roadmap
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (type == SourceEntity.Type.LOCAL_FILE) {
            Button(onClick = { onChange(SourceEntity.Type.LOCAL_FILE) }) {
                Text(stringResource(R.string.type_local_file))
            }
        } else {
            TextButton(onClick = { onChange(SourceEntity.Type.LOCAL_FILE) }) {
                Text(stringResource(R.string.type_local_file))
            }
        }
        if (type == SourceEntity.Type.LOCAL_DIRECTORY) {
            Button(onClick = { onChange(SourceEntity.Type.LOCAL_DIRECTORY) }) {
                Text(stringResource(R.string.type_local_directory))
            }
        } else {
            TextButton(onClick = { onChange(SourceEntity.Type.LOCAL_DIRECTORY) }) {
                Text(stringResource(R.string.type_local_directory))
            }
        }
        if (type == SourceEntity.Type.REMOTE) {
            Button(onClick = {
                onChange(SourceEntity.Type.REMOTE)
            }) {
                Text(stringResource(R.string.type_remote))
            }
        } else {
            TextButton(onClick = {
                onChange(SourceEntity.Type.REMOTE)
            }) {
                Text(stringResource(R.string.type_remote))
            }
        }
    }
}

@Composable
fun RemoteUrlInput(uriString: String, onChange: (Uri) -> Unit) {


    if (uriString.startsWith("https://source.unsplash.com/random")) {

        OutlinedTextField(
            value = uriString.removePrefix("https://source.unsplash.com/random")
                .removePrefix("?"),
            onValueChange = { text ->
                val parsed = if (text.isNotBlank()) try {
                    Uri.parse("https://source.unsplash.com/random?${text.trim().lowercase()}")
                } catch (e: Exception) {
                    null
                } else Uri.parse("https://source.unsplash.com/random")
                if (parsed != null) {
                    onChange(parsed)
                }
            },
            label = { Text(text = stringResource(id = R.string.required_unsplash_input)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { onChange(Uri.EMPTY) }) {
                    Icon(
                        Icons.Outlined.Clear,
                        contentDescription = stringResource(R.string.clear_url_input)
                    )
                }
            }
        )

    } else {

        OutlinedTextField(
            value = uriString,
            onValueChange = { text ->
                val parsed = text.takeIf { it.isNotBlank() }.toUriOrNull()
                if (parsed != null) {
                    onChange(parsed)
                }
            },
            label = { Text(text = stringResource(id = R.string.required_url_input)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    onChange(
                        Uri.parse("https://source.unsplash.com/random")
                    )
                }) {
                    Icon(
                        Icons.Outlined.PhotoLibrary,
                        contentDescription = stringResource(R.string.unsplash)
                    )
                }
            }
        )
    }
}


@Composable
fun ColumnScope.UriInput(
    type: SourceEntity.Type?,
    uri: Uri,
    onChange: (SourceEntity.Type, Uri) -> Unit
) {
    val pickPictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { imageUri ->
        if (imageUri != null) {
            onChange(SourceEntity.Type.LOCAL_FILE, imageUri)
        }
    }

    val pickFolderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            onChange(SourceEntity.Type.LOCAL_DIRECTORY, treeUri)
        }
    }

    TypeSelection(type = type, onChange = {
        when (it) {
            SourceEntity.Type.REMOTE -> onChange(SourceEntity.Type.REMOTE, Uri.EMPTY)
            SourceEntity.Type.LOCAL_FILE -> pickPictureLauncher.launch(arrayOf("image/*"))
            SourceEntity.Type.LOCAL_DIRECTORY -> pickFolderLauncher.launch(null)
        }
    })


    val uriString = rememberSaveable(uri) { uri.toString() }

    if (type == SourceEntity.Type.REMOTE) {
        Spacer(Modifier.height(8.dp))

        RemoteUrlInput(uriString = uriString, onChange = {
            onChange(
                SourceEntity.Type.REMOTE,
                it
            )
        })

    }
}


@Composable
fun ColumnScope.MultiUriInput(
    type: SourceEntity.Type?,
    uris: List<Uri>,
    onChange: (SourceEntity.Type, List<Uri>) -> Unit
) {
    val pickPictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { imageUris -> onChange(SourceEntity.Type.LOCAL_FILE, imageUris) }

    val pickFolderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            onChange(SourceEntity.Type.LOCAL_DIRECTORY, listOf(treeUri))
        }
    }

    TypeSelection(type = type, onChange = {
        when (it) {
            SourceEntity.Type.REMOTE -> onChange(SourceEntity.Type.REMOTE, listOf(Uri.EMPTY))
            SourceEntity.Type.LOCAL_FILE -> pickPictureLauncher.launch(arrayOf("image/*"))
            SourceEntity.Type.LOCAL_DIRECTORY -> pickFolderLauncher.launch(null)
        }
    })

    val uriString = rememberSaveable(uris) { uris.firstOrNull()?.toString() ?: "" }

    if (type == SourceEntity.Type.REMOTE) {
        Spacer(Modifier.height(8.dp))

        RemoteUrlInput(uriString = uriString, onChange = {
            onChange(
                SourceEntity.Type.REMOTE,
                listOf(it)
            )
        })

    }
}
