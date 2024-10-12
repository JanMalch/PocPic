package io.github.janmalch.pocpic.shared

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile

fun Context.getDocumentFileOrThrow(uri: Uri): DocumentFile {
    val document = DocumentFile.fromTreeUri(this, uri)
    checkNotNull(document) {
        "Got null for document file from tree uri, which should be impossible on SDK version ${Build.VERSION.SDK_INT}"
    }
    return document
}