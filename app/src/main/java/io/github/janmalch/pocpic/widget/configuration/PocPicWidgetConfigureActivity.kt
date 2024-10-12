package io.github.janmalch.pocpic.widget.configuration

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.janmalch.pocpic.ui.theme.PocPicTheme
import io.github.janmalch.pocpic.widget.PocPicWidget
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The configuration screen for the [PocPicWidget] AppWidget.
 */
@AndroidEntryPoint
class PocPicWidgetConfigureActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private val viewModel by viewModels<WidgetConfigurationViewModel>()

    @Inject
    lateinit var repository: WidgetConfigurationRepository

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                WidgetConfigurationScreen(
                    vm = viewModel,
                    onSelect = ::onFinish,
                )
            }
        }
    }

    private fun onFinish(shape: WidgetData.Shape) {
        val context = this@PocPicWidgetConfigureActivity

        lifecycleScope.launch {
            repository.set(shape)

            // It is the responsibility of the configuration activity to update the app widget
            PocPicWidget().updateAll(context)

            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}
