package io.github.janmalch.pocpic.widget

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import io.github.janmalch.pocpic.core.AndroidAppRepository
import io.github.janmalch.pocpic.core.AndroidRerollPicture
import io.github.janmalch.pocpic.core.AppRepository
import io.github.janmalch.pocpic.models.Picture
import io.github.janmalch.pocpic.widget.configuration.WidgetConfigurationRepository
import io.github.janmalch.pocpic.widget.configuration.WidgetData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import kotlin.random.Random


private const val TAG = "WatchSelectedPictureWithBitmap"

class WatchSelectedPictureWithBitmap(
    private val widgetConfigurationRepository: WidgetConfigurationRepository,
    private val repository: AppRepository,
    private val bitmapRenderer: BitmapRenderer,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<PictureWithBitmap?> {
        return widgetConfigurationRepository.watch()
            .onCompletion { Timber.w("Widget data flow has completed.") }
            .distinctUntilChanged()
            .onEach { Timber.d("Widget data changed: %s", it) }
            .flatMapLatest { config ->
                repository.watchSelectedPicture()
                    .onCompletion { Timber.w("Selected picture flow has completed.") }
                    .distinctUntilChanged { old, new -> old?.fileUri == new?.fileUri }
                    .onEach { Timber.d("Picture changed: ${it?.fileName}") }
                    .mapLatest { picture ->
                        if (picture == null) {
                            Timber.d("Got not picture to try to render for widget.")
                            return@mapLatest null
                        }
                        Timber.d("Got picture to try to render for widget: %s", picture.fileName)
                        val bitmap = try {
                            bitmapRenderer(Uri.parse(picture.fileUri), config).also {
                                Timber.d(
                                    "Finished rendering bitmap for widget: %s",
                                    picture.fileName
                                )
                            }
                        } catch (e: Exception) {
                            if (e !is CancellationException) {
                                Timber.e(e, "Error while rendering to bitmap.")
                            }
                            throw e
                        }
                        PictureWithBitmap(
                            picture = picture,
                            bitmap = bitmap,
                            widgetMustCrop = config.shape == WidgetData.Shape.CenterCropRectangle
                        )
                    }
            }
            .catch {
                Timber.e(it, "Error occurred inside widget flow.")
                throw it
            }
            .onCompletion { Timber.d("Combined widget flow has completed.") }
    }

    companion object {
        fun create(context: Context) = WatchSelectedPictureWithBitmap(
            WidgetConfigurationRepository(context.applicationContext),
            AndroidAppRepository.getInstance(context, AndroidRerollPicture(context, Random)),
            BitmapRenderer.create(context),
        )
    }
}

data class PictureWithBitmap(
    val picture: Picture,
    val bitmap: Bitmap,
    val widgetMustCrop: Boolean,
)