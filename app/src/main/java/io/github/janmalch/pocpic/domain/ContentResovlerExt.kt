package io.github.janmalch.pocpic.domain

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri

fun ContentResolver.persist(uri: Uri) {
    // ignore non-content URIs (like Remote)
    if (uri.scheme != "content") return
    takePersistableUriPermission(
        uri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION
    )
}

fun ContentResolver.release(uri: Uri) {
    // ignore non-content URIs (like Remote)
    if (uri.scheme != "content") return
    releasePersistableUriPermission(
        uri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION
    )
}
