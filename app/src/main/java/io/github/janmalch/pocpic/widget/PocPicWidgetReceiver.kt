package io.github.janmalch.pocpic.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import io.github.janmalch.pocpic.core.AndroidWidgetRepository
import io.github.janmalch.pocpic.widget.configuration.WidgetConfigurationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

private const val TAG = "PocPicWidgetReceiver"

class PocPicWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PocPicWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        runBlocking {
            AndroidWidgetRepository.getInstance(context).enqueueWork()
        }
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
        Timber.d("Widget size changed to %dx%d.", width, height)
        CoroutineScope(Dispatchers.Unconfined).launch {
            WidgetConfigurationRepository(context).set(
                width = width,
                height = height,
            )
        }
    }

    override fun onDisabled(context: Context) {
        UpdateWidgetWorker.cancel(context)
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