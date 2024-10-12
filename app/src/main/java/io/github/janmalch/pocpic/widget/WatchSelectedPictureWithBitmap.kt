package io.github.janmalch.pocpic.widget

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
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
            .onCompletion { Log.w(TAG, "Widget data flow has completed.") }
            .distinctUntilChanged()
            .onEach { Log.d(TAG, "Widget data changed: $it") }
            .flatMapLatest { config ->
                repository.watchSelectedPicture()
                    .onCompletion { Log.w(TAG, "Selected picture flow has completed.") }
                    .distinctUntilChanged { old, new -> old?.fileUri == new?.fileUri }
                    .onEach { Log.d(TAG, "Picture changed: ${it?.fileName}") }
                    .mapLatest { picture ->
                        if (picture == null) {
                            Log.d(TAG, "Got not picture to try to render for widget.")
                            return@mapLatest null
                        }
                        Log.d(TAG, "Got picture to try to render for widget: ${picture.fileName}")
                        val bitmap = try {
                            bitmapRenderer(Uri.parse(picture.fileUri), config).also {
                                Log.d(
                                    TAG,
                                    "Finished rendering bitmap for widget: ${picture.fileName}"
                                )
                            }
                        } catch (e: Exception) {
                            if (e !is CancellationException) {
                                Log.e(TAG, "Error while rendering to bitmap.", e)
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
                Log.e(TAG, "Error occurred inside widget flow.", it)
                throw it
            }
            .onCompletion { Log.w(TAG, "Combined widget flow has completed.") }
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