package io.github.janmalch.pocpic.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.annotation.Px
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class BitmapRenderer(
    private val context: Context,
    private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(uri: Uri, @Px width: Int, @Px height: Int): Bitmap? =
        withContext(defaultDispatcher) {
            suspendCancellableCoroutine { cont ->
                Glide
                    .with(context.applicationContext)
                    .asBitmap()
                    .load(uri)
                    .into(object : CustomTarget<Bitmap>(width, height) {

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