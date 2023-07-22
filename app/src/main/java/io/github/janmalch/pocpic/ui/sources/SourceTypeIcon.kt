package io.github.janmalch.pocpic.ui.sources

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PhotoCameraBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.data.SourceEntity


@Composable
fun SourceTypeIcon(type: SourceEntity.Type) {
    val icon = remember(type) {
        when (type) {
            SourceEntity.Type.REMOTE -> Icons.Outlined.CloudDownload
            SourceEntity.Type.LOCAL_FILE -> Icons.Outlined.PhotoCameraBack
            SourceEntity.Type.LOCAL_DIRECTORY -> Icons.Outlined.Folder
        }
    }
    val resId = remember(type) {
        when (type) {
            SourceEntity.Type.REMOTE -> R.string.type_remote
            SourceEntity.Type.LOCAL_FILE -> R.string.type_local_file
            SourceEntity.Type.LOCAL_DIRECTORY -> R.string.type_local_directory
        }
    }
    Icon(icon, contentDescription = stringResource(resId))
}