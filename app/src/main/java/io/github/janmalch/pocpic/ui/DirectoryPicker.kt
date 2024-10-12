package io.github.janmalch.pocpic.ui

import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberDirectoryPicker(
    onResult: (Uri?) -> Unit
): () -> Unit {
    val pickDirectoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = onResult
    )
    val doPick = remember {
        fun() {
            pickDirectoryLauncher.launch(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        }
    }
    return doPick
}