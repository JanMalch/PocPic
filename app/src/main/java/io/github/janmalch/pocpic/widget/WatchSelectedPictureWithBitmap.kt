package io.github.janmalch.pocpic.widget

import android.content.Context
import android.graphics.Bitmap
import io.github.janmalch.pocpic.core.Logger
import io.github.janmalch.pocpic.core.PersistentLogger
import io.github.janmalch.pocpic.core.Picture
import io.github.janmalch.pocpic.core.SelectedPicture
import io.github.janmalch.pocpic.widget.configuration.WidgetConfiguration
import io.github.janmalch.pocpic.widget.configuration.WidgetConfigurationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.Clock


class WatchSelectedPictureWithBitmap(
    private val widgetConfigurationRepository: WidgetConfigurationRepository,
    private val selectedPicture: SelectedPicture,
    private val bitmapRenderer: BitmapRenderer,
    private val logger: Logger,
) {


    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<PictureWithBitmap?> {
        return widgetConfigurationRepository.watch()
            .distinctUntilChanged()
            .flatMapLatest { config ->
                selectedPicture.watch()
                    .distinctUntilChanged { old, new -> old?.uri == new?.uri }
                    .mapLatest { picture ->
                        if (picture == null) {
                            logger.info("Got not picture to try to render for widget.")
                            return@mapLatest null
                        }
                        logger.info("Got picture to try to render for widget: ${picture.uri}")
                        val bitmap = try {
                            bitmapRenderer(picture.uri, config).also {
                                logger.info("Finished rendering bitmap for widget: ${picture.uri}")
                            }
                        } catch (e: Exception) {
                            logger.error("Error while rendering to bitmap.", e)
                            throw e
                        }
                        PictureWithBitmap(
                            picture = picture,
                            bitmap = bitmap,
                            widgetMustCrop = config.shape == WidgetConfiguration.Shape.CenterCropRectangle
                        )
                    }
            }
    }

    companion object {
        fun create(context: Context) = WatchSelectedPictureWithBitmap(
            WidgetConfigurationRepository(context.applicationContext),
            SelectedPicture(context.applicationContext, PersistentLogger(context, Clock.System)),
            BitmapRenderer.create(context),
            PersistentLogger(context, Clock.System)
        )
    }
}

data class PictureWithBitmap(
    val picture: Picture,
    val bitmap: Bitmap,
    val widgetMustCrop: Boolean,
)