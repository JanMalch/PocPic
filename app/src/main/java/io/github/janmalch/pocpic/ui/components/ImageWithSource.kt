package io.github.janmalch.pocpic.ui.components

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.skydoves.landscapist.ShimmerParams
import com.skydoves.landscapist.glide.GlideImage
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.extensions.darken
import io.github.janmalch.pocpic.models.PictureSource
import timber.log.Timber

@Composable
fun ImageWithSource(
    source: PictureSource,
    modifier: Modifier = Modifier,
    cornerSize: Dp = 24.dp,
    @DrawableRes previewPlaceholder: Int = 0,
) {
    Column(modifier = modifier) {
        GlideImage(
            imageModel = source.imageModel,
            previewPlaceholder = previewPlaceholder,
            contentScale = ContentScale.Crop,
            requestListener = object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Timber.e(e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            },
            modifier = Modifier
                .weight(1F, true)
                .clip(RoundedCornerShape(size = cornerSize)),
            // shows a shimmering effect when loading an image.
            shimmerParams = ShimmerParams(
                baseColor = MaterialTheme.colorScheme.background.darken(0.2f),
                highlightColor = MaterialTheme.colorScheme.background.darken(0.3f),
                durationMillis = 500,
                dropOff = 0.65f,
                tilt = 20f
            ),
            // shows an error text message when request failed.
            failure = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.error,
                            RoundedCornerShape(size = cornerSize)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.failed_to_load_image),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }
            },
        )
        Text(
            text = source.label,
            style = MaterialTheme.typography.titleSmall.copy(
                color = MaterialTheme.colorScheme.outline
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 8.dp,
                    bottom = 0.dp,
                    start = cornerSize,
                    end = cornerSize,
                )
        )
    }
}
