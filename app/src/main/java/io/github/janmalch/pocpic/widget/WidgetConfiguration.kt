package io.github.janmalch.pocpic.widget

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.edit
import io.github.janmalch.pocpic.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val PREFS_NAME = "io.github.janmalch.pocpic.widget.PocPicWidget"

@Serializable
data class AllWidgetsConfiguration(
    val refreshDurationInMinutes: Long,
) {
    companion object {
        private const val KEY = "all_widgets_configuration"
        val DEFAULT = AllWidgetsConfiguration(
            refreshDurationInMinutes = 60
        )

        private fun getPrefs(context: Context) = context.getSharedPreferences(PREFS_NAME, 0)

        fun load(context: Context): AllWidgetsConfiguration {
            val prefs = getPrefs(context)
            return try {
                val json = prefs.getString(KEY, null) ?: return DEFAULT
                return Json.decodeFromString(json)
            } catch (e: Exception) {
                DEFAULT
            }
        }

        fun store(context: Context, allWidgetsConfiguration: AllWidgetsConfiguration) {
            getPrefs(context).edit {
                val json = Json.encodeToString(allWidgetsConfiguration)
                putString(KEY, json)
            }
        }

        fun delete(context: Context) {
            getPrefs(context).edit {
                remove(KEY)
            }
        }
    }
}

@Serializable
data class WidgetConfiguration(
    val shape: WidgetShape,
    val cornerRadiusForFitCenter: Int,
) {
    companion object {
        val DEFAULT = WidgetConfiguration(
            shape = WidgetShape.FitCenterRectangle,
            cornerRadiusForFitCenter = 32,
        )

        private fun keyFor(appWidgetId: Int) = "${appWidgetId}_individual_configuration"

        private fun getPrefs(context: Context) = context.getSharedPreferences(PREFS_NAME, 0)

        fun load(context: Context, appWidgetId: Int): WidgetConfiguration {
            val prefs = getPrefs(context)
            return try {
                val json = prefs.getString(keyFor(appWidgetId), null) ?: return DEFAULT
                return Json.decodeFromString(json)
            } catch (e: Exception) {
                DEFAULT
            }
        }

        fun store(context: Context, appWidgetId: Int, widgetConfiguration: WidgetConfiguration) {
            getPrefs(context).edit {
                val json = Json.encodeToString(widgetConfiguration)
                putString(keyFor(appWidgetId), json)
            }
        }

        fun delete(context: Context, appWidgetId: Int) {
            getPrefs(context).edit {
                remove(keyFor(appWidgetId))
            }
        }
    }
}

@Serializable
enum class WidgetShape(@StringRes val label: Int, @StringRes val explanation: Int) {
    Circle(R.string.shape_circle, R.string.shape_circle_explanation),
    CenterCropRectangle(
        R.string.shape_center_crop_rectangle,
        R.string.shape_center_crop_rectangle_explanation
    ),
    FitCenterRectangle(
        R.string.shape_fit_center_rectangle,
        R.string.shape_fit_center_rectangle_explanation
    );
}
