package io.github.janmalch.pocpic.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import io.github.janmalch.pocpic.widget.configuration.WidgetConfigurationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration

class PocPicWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PocPicWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(UpdateWidgetWorker::class.java, Duration.ofDays(1L))
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    Duration.ofMinutes(1L),
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        if (appWidgetIds.isEmpty()) return
        storeNewWidgetSize(context, appWidgetManager, appWidgetIds.first())
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        storeNewWidgetSize(context, appWidgetManager, appWidgetId)
    }

    private fun storeNewWidgetSize(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val (width, height) = WidgetSizeProvider(context, appWidgetManager).getWidgetsSize(
            appWidgetId
        )
        Log.d("PocPicWidgetReceiver", "Widget size changed to ${width}x$height.")
        CoroutineScope(Dispatchers.Unconfined).launch {
            WidgetConfigurationRepository(context).set(
                width = width,
                height = height,
            )
        }
    }

    override fun onDisabled(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "PocPicWidget_PeriodicWorker"
    }
}

/**
 * @author https://stackoverflow.com/a/58501760
 */
class WidgetSizeProvider(
    private val context: Context,
    private val appWidgetManager: AppWidgetManager
) {

    fun getWidgetsSize(widgetId: Int): Pair<Int, Int> {
        val isPortrait = context.resources.configuration.orientation == ORIENTATION_PORTRAIT
        val width = getWidgetWidth(isPortrait, widgetId)
        val height = getWidgetHeight(isPortrait, widgetId)
        val widthInPx = context.dip(width)
        val heightInPx = context.dip(height)
        return widthInPx to heightInPx
    }

    private fun getWidgetWidth(isPortrait: Boolean, widgetId: Int): Int =
        if (isPortrait) {
            getWidgetSizeInDp(widgetId, AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        } else {
            getWidgetSizeInDp(widgetId, AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        }

    private fun getWidgetHeight(isPortrait: Boolean, widgetId: Int): Int =
        if (isPortrait) {
            getWidgetSizeInDp(widgetId, AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
        } else {
            getWidgetSizeInDp(widgetId, AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        }

    private fun getWidgetSizeInDp(widgetId: Int, key: String): Int =
        appWidgetManager.getAppWidgetOptions(widgetId).getInt(key, 0)

    private fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()

}