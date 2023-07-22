package io.github.janmalch.pocpic.widget

import android.content.Context
import androidx.compose.runtime.Composable
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
import io.github.janmalch.pocpic.ui.MainActivity

class PocPicWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val watchSelectedPicture = remember { WatchSelectedPictureWithBitmap.create(context) }
            val current by watchSelectedPicture().collectAsState(initial = null)
            WidgetImage(picture = current)
        }
    }

    @Composable
    fun WidgetImage(picture: PictureWithBitmap?) {
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
            contentScale = ContentScale.Crop,
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(32.dp)
                .clickable(actionStartActivity<MainActivity>())
        )

    }
}