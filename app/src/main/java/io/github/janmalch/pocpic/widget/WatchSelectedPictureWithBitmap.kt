package io.github.janmalch.pocpic.widget

import android.content.Context
import android.graphics.Bitmap
import io.github.janmalch.pocpic.domain.Picture
import io.github.janmalch.pocpic.domain.SelectedPicture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

class WatchSelectedPictureWithBitmap(
    private val widgetConfiguration: WidgetConfiguration,
    private val selectedPicture: SelectedPicture,
    private val bitmapRenderer: BitmapRenderer,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke() = widgetConfiguration.watch().flatMapLatest { (width, height) ->
        selectedPicture.watch().mapLatest { picture ->
            val bitmap = picture?.let {
                bitmapRenderer(it.uri, width, height)
            } ?: return@mapLatest null
            PictureWithBitmap(picture, bitmap)
        }
    }

    companion object {
        fun create(context: Context) = WatchSelectedPictureWithBitmap(
            WidgetConfiguration(context.applicationContext, Dispatchers.IO),
            SelectedPicture(context.applicationContext, Dispatchers.IO),
            BitmapRenderer.create(context),
        )
    }
}

data class PictureWithBitmap(
    val picture: Picture,
    val bitmap: Bitmap,
)