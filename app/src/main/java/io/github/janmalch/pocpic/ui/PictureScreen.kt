package io.github.janmalch.pocpic.ui

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.google.protobuf.Timestamp
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.models.Picture
import io.github.janmalch.pocpic.models.fileDateOrNull
import io.github.janmalch.pocpic.ui.theme.Typography
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private infix fun DateTimeFormatter.format(timestamp: Timestamp): String =
    format(Instant.fromEpochSeconds(timestamp.seconds, timestamp.nanos).toJavaInstant())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PictureScreen(
    picture: Picture?,
    onPictureClicked: () -> Unit,
    onDirectorySelected: (Uri) -> Unit,
    onGoToLicenses: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val contentResolver = remember(context) { context.contentResolver }
    val formatter = remember {
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withZone(ZoneId.systemDefault())
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    MoreMenu(
                        onDirectorySelected = onDirectorySelected,
                        onGoToLicenses = onGoToLicenses,
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        bottomBar = {
            BottomBar(
                picture = picture,
                formatter = formatter,
                onShareClick = { picture ->
                    try {
                        val uri = Uri.parse(picture.fileUri)
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_TITLE, picture.fileName)
                            picture.fileDateOrNull?.also {
                                putExtra(Intent.EXTRA_TEXT, formatter format it)
                            }
                            type = contentResolver.getType(uri) ?: "image/jpeg"
                        }
                        context.startActivity(Intent.createChooser(intent, null))
                    } catch (e: Exception) {
                        Log.e("PictureScreen", "Error while sharing picture.", e)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.error_while_sharing_picture))
                        }
                    }
                }
            )
        },
        modifier = modifier,
    ) { contentPadding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .clickable {
                    onPictureClicked()
                },
        ) {
            AsyncImage(
                model = picture?.fileUri,
                contentDescription = picture?.fileName,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun MoreMenu(
    onDirectorySelected: (Uri) -> Unit,
    onGoToLicenses: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val pickDirectory = rememberDirectoryPicker { uri ->
        if (uri != null) {
            onDirectorySelected(uri)
        }
    }
    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopStart)
    ) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.open_menu)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.change_directory)) },
                onClick = {
                    pickDirectory()
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.view_licenses)) },
                onClick = {
                    onGoToLicenses()
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun BottomBar(
    formatter: DateTimeFormatter,
    picture: Picture?,
    onShareClick: (Picture) -> Unit,
) {
    val formattedDate = remember(picture) {
        if (picture?.fileDateOrNull == null) return@remember null
        formatter format picture.fileDate
    }
    BottomAppBar(
        actions = {
            if (picture != null) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = picture.fileName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = Typography.titleMedium
                    )
                    if (formattedDate != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formattedDate,
                            style = Typography.labelSmall
                        )
                    }
                }
            }
        },
        floatingActionButton = {

            FloatingActionButton(
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                onClick = {
                    if (picture != null) {
                        onShareClick(picture)
                    }
                },
            ) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = stringResource(R.string.share_picture)
                )
            }
        }
    )
}
