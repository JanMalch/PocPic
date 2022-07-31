package io.github.janmalch.pocpic.extensions

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

@SuppressLint("Range")
fun Uri.getFileName(contentResolver: ContentResolver): String? {
    return try {
        val cursor = contentResolver.query(this, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            } else {
                null
            }
        }
    } catch (e: Exception) {
        null
    }
}
