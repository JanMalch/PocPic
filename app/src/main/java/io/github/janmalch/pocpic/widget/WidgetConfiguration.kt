package io.github.janmalch.pocpic.widget

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.edit
import io.github.janmalch.pocpic.R

private const val PREFS_NAME = "io.github.janmalch.pocpic.widget.PocPicWidget"
private const val PREF_PREFIX_KEY = "appwidget_"

enum class WidgetShape(@StringRes val label: Int, @StringRes val explanation: Int) {
    Circle(R.string.shape_circle, R.string.shape_circle_explanation),
    // Square(R.string.shape_square, R.string.shape_square_explanation),
    CenterCropRectangle(R.string.shape_center_crop_rectangle, R.string.shape_center_crop_rectangle_explanation),
    FitCenterRectangle(R.string.shape_fit_center_rectangle, R.string.shape_fit_center_rectangle_explanation);

    companion object {
        private val DEFAULT = FitCenterRectangle

        private fun keyFor(appWidgetId: Int) =
            PREF_PREFIX_KEY + appWidgetId + "_shape"

        private fun getPrefs(context: Context) = context.getSharedPreferences(PREFS_NAME, 0)

        fun load(context: Context, appWidgetId: Int): WidgetShape {
            val prefs = getPrefs(context)
            return try {
                valueOf(
                    prefs.getString(
                        keyFor(appWidgetId),
                        DEFAULT.name
                    ) ?: DEFAULT.name
                )
            } catch (e: Exception) {
                DEFAULT
            }
        }

        fun store(context: Context, appWidgetId: Int, widgetShape: WidgetShape) {
            getPrefs(context).edit {
                putString(keyFor(appWidgetId), widgetShape.name)
            }
        }

        fun delete(context: Context, appWidgetId: Int) {
            getPrefs(context).edit {
                remove(keyFor(appWidgetId))
            }
        }
    }
}
