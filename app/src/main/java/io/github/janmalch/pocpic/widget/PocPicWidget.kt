package io.github.janmalch.pocpic.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import io.github.janmalch.pocpic.core.Logger
import io.github.janmalch.pocpic.core.PersistentLogger
import io.github.janmalch.pocpic.ui.MainActivity
import kotlinx.datetime.Clock

class PocPicWidget : GlanceAppWidget() {

    private lateinit var logger: Logger

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        logger = PersistentLogger(context, Clock.System)
        logger.info("Providing Glance widget.")
        provideContent {
            val watchSelectedPicture =
                remember { WatchSelectedPictureWithBitmap.create(context).invoke() }
            val current by watchSelectedPicture.collectAsState(initial = null)
            WidgetImage(picture = current)
        }
    }

    @Composable
    fun WidgetImage(picture: PictureWithBitmap?) {
        // FIXME: only do this, if there is actually no picture.
        // Try to retain current picture on error.

        LaunchedEffect(picture) {
            logger.info("Rendering widget: ${picture?.picture?.uri}")
        }

        if (picture == null) {
            Button(
                text = "Open PocPic to get started!",
                onClick = actionStartActivity<MainActivity>()
            )
            return
        }

        Image(
            provider = ImageProvider(picture.bitmap),
            contentDescription = picture.picture.label,
            contentScale = if (picture.widgetMustCrop) ContentScale.Crop else ContentScale.Fit,
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(32.dp)
                .clickable(actionStartActivity<MainActivity>())
        )

    }
}