package io.github.janmalch.pocpic.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.github.janmalch.pocpic.widget.configuration.WidgetConfiguration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class BitmapRenderer(
    private val context: Context,
    private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(uri: Uri, config: WidgetConfiguration): Bitmap? =
        withContext(defaultDispatcher) {
            suspendCancellableCoroutine { cont ->
                Glide
                    .with(context.applicationContext)
                    .asBitmap()
                    .applyShape(config.shape, context)
                    .load(uri)
                    .into(object : CustomTarget<Bitmap>(config.width, config.height) {

                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            cont.resume(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            cont.resume(null)
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            Log.e("BitmapRenderer", "Failed to load picture.")
                            cont.resume(null)
                        }
                    })
            }
        }

    companion object {
        fun create(context: Context) = BitmapRenderer(
            context.applicationContext,
            Dispatchers.Default,
        )
    }
}


private fun <T> RequestBuilder<T>.applyShape(
    shape: WidgetConfiguration.Shape,
    context: Context,
): RequestBuilder<T> =
    when (shape) {
        // Cropping or Fitting is done in PocPicWidget itself
        WidgetConfiguration.Shape.CenterCropRectangle -> this
        WidgetConfiguration.Shape.Circle -> circleCrop()
        WidgetConfiguration.Shape.FitCenterRectangle -> this
            .apply(RequestOptions.bitmapTransform(RoundedCorners(32.dpToPx(context))))
    }

private fun Number.dpToPx(context: Context) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    context.resources.displayMetrics
).toInt()

