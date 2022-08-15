package io.github.janmalch.pocpic.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.widget.RemoteViews
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.AppWidgetTarget
import com.bumptech.glide.request.target.Target
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.models.PictureSource
import io.github.janmalch.pocpic.ui.main.MainActivity
import timber.log.Timber
import java.time.Duration

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [PocPicWidgetConfigureActivity]
 */
class PocPicWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, null)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            WidgetShape.delete(context, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context != null && intent != null && intent.action.equals(ACTION_CHANGE_IMAGE)) {
            val source = intent.getParcelableExtra<PictureSource>(EXTRA_PICTURE_SOURCE)

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidgetComponentName = ComponentName(context.packageName, javaClass.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, source)
            }
        }
    }

    override fun onEnabled(context: Context) {
        // FIXME: interval from preferences
        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(UpdateWidgetWorker::class.java, Duration.ofMinutes(30L))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .setInitialDelay(Duration.ZERO)
                .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )
    }

    override fun onDisabled(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    companion object {
        const val ACTION_CHANGE_IMAGE = "PocPicWidget_ACTION_CHANGE_IMAGE"
        const val EXTRA_PICTURE_SOURCE = "PocPicWidget_EXTRA_PICTURE_SOURCE"
        private const val UNIQUE_WORK_NAME = "PocPicWidget_PeriodicWorker"
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    source: PictureSource?,
    shape: WidgetShape = WidgetShape.load(context, appWidgetId)
) {
    Timber.d("Update widget with $source and $shape")
    if (source == null) {
        return
    }

    val views = RemoteViews(
        context.packageName,
        // apparently Glide's scale type does not work in RemoteViews
        when (shape) {
            WidgetShape.Circle -> R.layout.poc_pic_widget_fit_center
            WidgetShape.CenterCropRectangle -> R.layout.poc_pic_widget_center_crop
            else -> R.layout.poc_pic_widget_fit_center
        }.also {
            Timber.d("Using $it (${R.layout.poc_pic_widget_center_crop})")
        }
    ).apply {
        setOnClickPendingIntent(
            R.id.appwidget_image,
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).apply {
                    putExtra(MainActivity.EXTRA_INITIAL_SOURCE, source)
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

        // On lower version setClipToOutline is not a RemotableViewMethod.
        // So for these low versions the CenterCropRectangle won't have rounded corners.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setBoolean(R.id.appwidget_image, "setClipToOutline", true)
        }
    }

    val appWidgetTarget = AppWidgetTarget(context, R.id.appwidget_image, views, appWidgetId)

    try {
        Glide
            .with(context.applicationContext)
            .asBitmap()
            .diskCacheStrategy(source.cacheStrategy.strategy)
            .useCircleCrop(use = shape == WidgetShape.Circle)
            .useRoundedCorners(
                use = shape == WidgetShape.FitCenterRectangle,
                roundingRadius = 64 // FIXME: 24.dp.px
            )
            .load(source.imageModel)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Timber.e(e, "Drawable cannot be loaded and set in widget image view")
                    return !isFirstResource // return true so previous image is kept
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Set contentDescription in order to enable accessibility and be usable with TalkBack
                    views.setContentDescription(R.id.appwidget_image, source.label)
                    return false
                }
            })
            .into(appWidgetTarget)
    } catch (e: Exception) {
        Timber.e(e, "Error while loading ${source.imageModel} into image view.")
    }
}

private fun <T> RequestBuilder<T>.useCircleCrop(use: Boolean) = if (use) this.circleCrop() else this

private fun <T> RequestBuilder<T>.useRoundedCorners(
    use: Boolean,
    roundingRadius: Int
) = if (use && roundingRadius > 0) this.apply(RequestOptions.bitmapTransform(RoundedCorners(roundingRadius))) else this
