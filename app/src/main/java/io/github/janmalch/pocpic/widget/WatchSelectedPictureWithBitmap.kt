package io.github.janmalch.pocpic.widget

import android.content.Context
import android.graphics.Bitmap
import io.github.janmalch.pocpic.domain.Picture
import io.github.janmalch.pocpic.domain.SelectedPicture
import io.github.janmalch.pocpic.widget.configuration.WidgetConfiguration
import io.github.janmalch.pocpic.widget.configuration.WidgetConfigurationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

class WatchSelectedPictureWithBitmap(
    private val widgetConfigurationRepository: WidgetConfigurationRepository,
    private val selectedPicture: SelectedPicture,
    private val bitmapRenderer: BitmapRenderer,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke() = widgetConfigurationRepository.watch()
        .distinctUntilChanged()
        .flatMapLatest { config ->
            selectedPicture.watch()
                .distinctUntilChanged()
                .mapLatest { picture ->
                    val bitmap = picture?.let {
                        bitmapRenderer(it.uri, config)
                    } ?: return@mapLatest null
                    PictureWithBitmap(
                        picture = picture,
                        bitmap = bitmap,
                        widgetMustCrop = config.shape == WidgetConfiguration.Shape.CenterCropRectangle
                    )
                }
        }

    companion object {
        fun create(context: Context) = WatchSelectedPictureWithBitmap(
            WidgetConfigurationRepository(context.applicationContext, Dispatchers.IO),
            SelectedPicture(context.applicationContext, Dispatchers.IO),
            BitmapRenderer.create(context),
        )
    }
}

data class PictureWithBitmap(
    val picture: Picture,
    val bitmap: Bitmap,
    val widgetMustCrop: Boolean,
)