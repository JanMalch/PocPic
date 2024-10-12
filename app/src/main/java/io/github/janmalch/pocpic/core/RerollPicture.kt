package io.github.janmalch.pocpic.core

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import com.google.protobuf.Timestamp
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.janmalch.pocpic.models.Picture
import javax.inject.Inject
import kotlin.random.Random

interface RerollPicture {
    operator fun invoke(source: Uri, previous: Picture?): Picture?
}

class AndroidRerollPicture @Inject constructor(
    @ApplicationContext private val context: Context,
    private val random: Random,
) : RerollPicture {

    override fun invoke(source: Uri, previous: Picture?): Picture? {
        val files = context.getDocumentFileOrThrow(source)
            .listFiles()
            .filter { it.isFile }

        if (files.isEmpty()) return null
        if (files.size == 1) return files[0].toPicture()

        val previousUri = previous?.fileUri?.let(Uri::parse)
        if (files.size == 2) {
            return (if (files[0].uri == previousUri) files[1] else files[0]).toPicture()
        }

        var next: DocumentFile
        var attempts = ATTEMPTS
        do {
            next = files.random(random)
            attempts--
        } while (next.uri == previousUri && attempts > 0)

        return next.toPicture()
    }

    private fun DocumentFile.toPicture() = Picture.newBuilder()
        .setFileUri(uri.toString())
        .setFileName(name ?: uri.toString().substringAfter('/'))
        .setFileDate(Timestamp.newBuilder().setSeconds(lastModified() / 1000).build())
        .build()


    companion object {
        private const val ATTEMPTS = 5
    }
}

private fun Context.getDocumentFileOrThrow(uri: Uri): DocumentFile {
    val document = DocumentFile.fromTreeUri(this, uri)
    checkNotNull(document) {
        "Got null for document file from tree uri, which should be impossible on SDK version ${Build.VERSION.SDK_INT}"
    }
    return document
}
