package io.github.janmalch.pocpic.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import io.github.janmalch.pocpic.models.DiskCacheStrategyEnum
import io.github.janmalch.pocpic.models.PictureSource

sealed class PictureSourceFactory {
    abstract fun nextPictureSource(): PictureSource?

    companion object {
        fun fromConfig(config: SourceFactoryConfig, context: Context): PictureSourceFactory =
            when (config.sourceType) {
                SourceFactoryConfig.SourceType.REMOTE -> RemotePictureSourceFactory(
                    config.label,
                    config.uri,
                    config.remoteCacheable ?: false,
                    config.remoteSeedQueryParam,
                )
                SourceFactoryConfig.SourceType.LOCAL_DIRECTORY -> LocalDirectorySourceFactory(
                    config.label, config.uri, context
                )
                SourceFactoryConfig.SourceType.LOCAL_FILE -> LocalPictureSourceFactory(
                    config.label, config.uri
                )
            }
    }
}

class RemotePictureSourceFactory(
    private val label: String,
    private val uri: Uri,
    private val cacheable: Boolean,
    private val seedQueryParam: String?
) : PictureSourceFactory() {

    override fun nextPictureSource(): PictureSource {
        val uriBuilder = uri.buildUpon()
        val seed = System.currentTimeMillis().toString(10)
        if (!cacheable) {
            uriBuilder.fragment(seed)
        }
        if (seedQueryParam != null) {
            uriBuilder.appendQueryParameter(seedQueryParam, seed)
        }
        return PictureSource(
            label = label,
            imageModel = uriBuilder.build(),
            cacheStrategy = if (cacheable) DiskCacheStrategyEnum.AUTOMATIC else DiskCacheStrategyEnum.NONE
        )
    }
}

class LocalPictureSourceFactory(private val label: String, private val filePath: Uri) :
    PictureSourceFactory() {
    override fun nextPictureSource(): PictureSource = PictureSource(
        label, filePath
    )
}

class LocalDirectorySourceFactory(
    private val label: String,
    private val filePath: Uri,
    private val context: Context
) :
    PictureSourceFactory() {
    override fun nextPictureSource(): PictureSource? =
        DocumentFile.fromTreeUri(context, filePath)
            ?.listFiles()
            ?.randomOrNull()
            ?.let {
                PictureSource(label + " " + it.name, it.uri)
            }
}
