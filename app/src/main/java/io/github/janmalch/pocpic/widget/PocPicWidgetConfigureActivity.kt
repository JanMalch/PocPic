package io.github.janmalch.pocpic.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import io.github.janmalch.pocpic.data.SourceProvider
import io.github.janmalch.pocpic.ui.theme.PocPicTheme
import kotlinx.coroutines.launch

/**
 * The configuration screen for the [PocPicWidget] AppWidget.
 */
class PocPicWidgetConfigureActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        // Find the widget id from the intent.
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            PocPicTheme {
                WidgetConfigurationScreen(onSubmit = ::onFinish)
            }
        }
    }

    private fun onFinish(widgetShape: WidgetShape) {
        val context = this@PocPicWidgetConfigureActivity

        WidgetShape.store(context, appWidgetId, widgetShape)

        lifecycleScope.launch {
            // It is the responsibility of the configuration activity to update the app widget
            updateAppWidget(
                context,
                appWidgetId,
                SourceProvider.createInstance(context).yieldSource(),
                widgetShape
            )

            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}
