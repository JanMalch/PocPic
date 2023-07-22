package io.github.janmalch.pocpic.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.Px
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import io.github.janmalch.pocpic.domain.Picture
import io.github.janmalch.pocpic.domain.SelectedPicture
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class WatchSelectedPictureWithBitmap(
    private val widgetConfiguration: WidgetConfiguration,
    private val selectedPicture: SelectedPicture,
    private val context: Context,
    private val defaultDispatcher: CoroutineDispatcher,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke() = widgetConfiguration.watch().flatMapLatest { (width, height) ->
        selectedPicture.watch().mapLatest { picture ->
            picture?.withBitmap(width, height)
        }
    }

    private suspend fun Picture.withBitmap(@Px width: Int, @Px height: Int) =
        withContext(defaultDispatcher) {
            suspendCancellableCoroutine { cont ->
                Glide
                    .with(context.applicationContext)
                    .asBitmap()
                    .load(uri)
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e("WatchSelectedPictureWithBitmap", "Failed to load picture.", e)
                            return !isFirstResource // return true so previous image is kept
                        }

                        override fun onResourceReady(
                            resource: Bitmap?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean = false
                    })
                    .into(object : CustomTarget<Bitmap>(width, height) {

                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                        ) {
                            cont.resume(PictureWithBitmap(this@withBitmap, resource))
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            cont.resume(null)
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            cont.resume(null)
                        }
                    })

            }
        }

    companion object {
        fun create(context: Context) = WatchSelectedPictureWithBitmap(
            WidgetConfiguration(context.applicationContext, Dispatchers.IO),
            SelectedPicture(context.applicationContext, Dispatchers.IO),
            context.applicationContext,
            Dispatchers.Default,
        )
    }
}

data class PictureWithBitmap(
    val picture: Picture,
    val bitmap: Bitmap,
)