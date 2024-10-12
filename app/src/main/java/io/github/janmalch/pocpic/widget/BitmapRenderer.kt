package io.github.janmalch.pocpic.widget

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.TypedValue
import coil3.ImageLoader
import coil3.executeBlocking
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.transformations
import coil3.toBitmap
import coil3.transform.CircleCropTransformation
import coil3.transform.RoundedCornersTransformation
import io.github.janmalch.pocpic.widget.configuration.WidgetData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BitmapRenderer(
    private val context: Context,
    private val imageLoader: ImageLoader,
    private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(uri: Uri, config: WidgetData): Bitmap =
        withContext(defaultDispatcher) {
            val image = imageLoader.executeBlocking(
                ImageRequest.Builder(context)
                    .data(uri)
                    .allowHardware(false)
                    .size(width = config.width, height = config.height)
                    .applyShape(config.shape, context)
                    .build()
            ).image
            checkNotNull(image) { "Failed to load image." }
            image.toBitmap()
        }

    companion object {
        fun create(context: Context) = BitmapRenderer(
            context.applicationContext,
            context.imageLoader,
            Dispatchers.Default,
        )
    }
}


private fun ImageRequest.Builder.applyShape(
    shape: WidgetData.Shape,
    context: Context,
): ImageRequest.Builder =
    when (shape) {
        // Cropping or Fitting is done in PocPicWidget itself
        WidgetData.Shape.CenterCropRectangle -> this
        WidgetData.Shape.Circle -> transformations(CircleCropTransformation())
        WidgetData.Shape.FitCenterRectangle -> transformations(
            RoundedCornersTransformation(32.dpToPx(context))
        )
    }

private fun Number.dpToPx(context: Context) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    context.resources.displayMetrics
)

