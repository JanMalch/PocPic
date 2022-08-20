package io.github.janmalch.pocpic.ui.config

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.request.RequestOptions
import com.google.modernstorage.photopicker.PhotoPicker
import com.skydoves.landscapist.ShimmerParams
import com.skydoves.landscapist.glide.GlideImage
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.data.SourceFactoryConfig
import io.github.janmalch.pocpic.extensions.darken
import io.github.janmalch.pocpic.extensions.getFileName
import io.github.janmalch.pocpic.models.PictureSource
import io.github.janmalch.pocpic.ui.components.FullScreenDialog

@Composable
fun ConfigScreen(
    vm: FactoriesViewModel,
    goBack: () -> Unit
) {
    var openDialog by remember {
        mutableStateOf(false)
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { goBack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_back)
                        )
                    }
                },
                title = { Text(text = stringResource(id = R.string.sources)) },

            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                openDialog = true
            }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.add)
                )
            }
        },
        content = { innerPadding ->
            val allConfigs by vm.factories.observeAsState(initial = emptyMap())

            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(allConfigs.entries.toList()) { (config, factory) ->
                    SourceListItem(
                        config = config,
                        source = remember {
                            factory.nextPictureSource()
                        },
                        onRemove = {
                            vm.remove(config)
                        }
                    )
                }
            }

            if (openDialog) {
                val newConfig = remember {
                    mutableStateOf(
                        SourceFactoryConfig(
                            label = "",
                            uri = Uri.EMPTY,
                            sourceType = SourceFactoryConfig.SourceType.REMOTE,
                            remoteCacheable = null,
                            remoteSeedQueryParam = null,
                        )
                    )
                }

                FullScreenDialog(
                    title = { Text(text = stringResource(id = R.string.new_source)) },
                    actions = {
                        TextButton(onClick = {
                            // TODO: prevent from leaving when not saved
                            vm.insertOrReplace(newConfig.value)
                            openDialog = false
                        }) {
                            Text(text = stringResource(id = R.string.save))
                        }
                    },
                    onDismissRequest = { openDialog = false }
                ) {

                    ConfigEditor(data = newConfig.value, onChange = { newConfig.value = it })
                }
            }
        }
    )
}

@SuppressLint("UnsafeOptInUsageError", "Range")
@Composable
fun ConfigEditor(
    data: SourceFactoryConfig,
    onChange: (SourceFactoryConfig) -> Unit,
) {

    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {

        OutlinedTextField(
            value = data.label,
            onValueChange = {
                onChange(data.copy(label = it.trim()))
            },
            label = { Text(text = stringResource(id = R.string.required_label_input)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(24.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                readOnly = true,
                value = stringResource(id = data.sourceType.translationRes),
                onValueChange = {},
                label = { Text(text = stringResource(id = R.string.required_type_dropdown)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(),
            ) {
                SourceFactoryConfig.SourceType.values().forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(stringResource(id = selectionOption.translationRes)) },
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onChange(data.copy(sourceType = selectionOption))
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        when (data.sourceType) {
            SourceFactoryConfig.SourceType.REMOTE -> {

                OutlinedTextField(
                    value = data.uri.toString(),
                    onValueChange = {
                        onChange(data.copy(uri = Uri.parse(it.trim())))
                    },
                    label = { Text(text = stringResource(id = R.string.required_url_input)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(autoCorrect = false),
                )

                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = data.remoteSeedQueryParam ?: "",
                    onValueChange = {
                        onChange(
                            data.copy(
                                remoteSeedQueryParam = it.trim().takeIf(String::isNotEmpty)
                            )
                        )
                    },
                    label = { Text(text = stringResource(id = R.string.query_param_name_input)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Text(
                    text = stringResource(id = R.string.query_param_name_input_explanation),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1F)) {

                        Text(text = stringResource(id = R.string.cacheable_url_input))
                        Text(
                            text = stringResource(id = R.string.cacheable_url_input_explanation),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Checkbox(
                        checked = data.remoteCacheable ?: true,
                        onCheckedChange = {
                            onChange(data.copy(remoteCacheable = it))
                        },
                    )
                }
            }
            SourceFactoryConfig.SourceType.LOCAL_FILE -> {
                val contentResolver = LocalContext.current.applicationContext.contentResolver
                val photoPicker = rememberLauncherForActivityResult(PhotoPicker()) { uris ->
                    val uri = uris.firstOrNull() ?: return@rememberLauncherForActivityResult
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    onChange(data.copy(uri = uri))
                }
                Row {
                    OutlinedTextField(
                        value = data.uri.getFileName(contentResolver) ?: "",
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 1,
                        modifier = Modifier.weight(1F)
                    )
                    IconButton(onClick = {
                        photoPicker.launch(PhotoPicker.Args(PhotoPicker.Type.IMAGES_ONLY, 1))
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(id = R.string.select_file)
                        )
                    }
                }
            }
            SourceFactoryConfig.SourceType.LOCAL_DIRECTORY -> {
                val contentResolver = LocalContext.current.applicationContext.contentResolver
                val directoryPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
                    if (uri == null) {
                        return@rememberLauncherForActivityResult
                    }
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    onChange(data.copy(uri = uri))
                }
                Row {
                    OutlinedTextField(
                        value = data.uri.getFileName(contentResolver) ?: data.uri.toString(),
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 1,
                        modifier = Modifier.weight(1F)
                    )
                    IconButton(onClick = {
                        directoryPicker.launch(null)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(id = R.string.select_directory)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SourceListItem(
    config: SourceFactoryConfig,
    source: PictureSource?,
    onRemove: () -> Unit
) {
    val openDialog = remember {
        mutableStateOf(false)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                openDialog.value = true
            }
            .padding(16.dp)
    ) {
        GlideImage(
            imageModel = source?.imageModel,
            requestOptions = {
                RequestOptions()
                    .override(40, 40)
                    .centerCrop()
            },
            shimmerParams = ShimmerParams(
                baseColor = MaterialTheme.colorScheme.background.darken(0.2f),
                highlightColor = MaterialTheme.colorScheme.background.darken(0.3f),
                durationMillis = 500,
                dropOff = 0.65f,
                tilt = 20f
            ),
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
        )

        Spacer(modifier = Modifier.size(16.dp))

        Column(modifier = Modifier.weight(1F)) {
            Text(text = config.label)

            if (config.sourceType == SourceFactoryConfig.SourceType.REMOTE) {
                Text(
                    text = config.uri.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                val contentResolver = LocalContext.current.applicationContext.contentResolver
                Text(
                    text = config.uri.getFileName(contentResolver) ?: config.uri.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = {
                Text(text = stringResource(id = R.string.delete_source))
            },
            text = {
                Text(text = stringResource(id = R.string.delete_source_confirmation_text))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                        onRemove()
                    },
                ) {
                    Text(text = stringResource(id = R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}
