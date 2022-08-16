package io.github.janmalch.pocpic.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.janmalch.pocpic.R

@Composable
fun WidgetConfigurationScreen(onSubmit: (WidgetConfiguration, AllWidgetsConfiguration) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = stringResource(id = R.string.configure_widget)) },
                )
            },
            content = { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    val widgetConfiguration = remember {
                        mutableStateOf(WidgetConfiguration.DEFAULT)
                    }
                    val allWidgetsConfiguration = remember {
                        mutableStateOf(AllWidgetsConfiguration.DEFAULT)
                    }
                    val refreshWidgets = remember {
                        mutableStateOf(AllWidgetsConfiguration.DEFAULT.refreshDurationInMinutes > 0)
                    }

                    Column {
                        Divider(color = DividerDefaults.color.copy(alpha = 0.12f))

                        Column(modifier = Modifier.padding(16.dp)) {

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1F)) {
                                    Text(text = stringResource(id = R.string.refresh_widgets_input))
                                    Text(
                                        text = stringResource(id = R.string.refresh_widgets_input_explanation),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Checkbox(
                                    checked = refreshWidgets.value,
                                    onCheckedChange = {
                                        refreshWidgets.value = it
                                    },
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            OutlinedTextField(
                                value = allWidgetsConfiguration.value.refreshDurationInMinutes.toString(
                                    10
                                ),
                                onValueChange = {
                                    val asLong = it.toLongOrNull(10)
                                    if (asLong != null) {
                                        allWidgetsConfiguration.value =
                                            allWidgetsConfiguration.value.copy(
                                                refreshDurationInMinutes = asLong
                                            )
                                    }
                                },
                                label = { Text(text = stringResource(id = R.string.refresh_duration_input)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    autoCorrect = false,
                                    keyboardType = KeyboardType.Number
                                ),
                                enabled = refreshWidgets.value
                            )
                            Text(
                                text = stringResource(id = R.string.refresh_duration_input_explanation),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        Divider(color = DividerDefaults.color.copy(alpha = 0.12f))

                        Column(modifier = Modifier.padding(16.dp)) {

                            Text(
                                text = stringResource(id = R.string.widget_shapes_headline),
                                style = MaterialTheme.typography.titleMedium,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            WidgetShape.values().forEach { shape ->

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = shape == widgetConfiguration.value.shape,
                                        onClick = {
                                            widgetConfiguration.value =
                                                widgetConfiguration.value.copy(
                                                    shape = shape
                                                )
                                        }
                                    )

                                    Column(modifier = Modifier.weight(1F)) {

                                        Text(text = stringResource(id = shape.label))
                                        Text(
                                            text = stringResource(id = shape.explanation),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            if (widgetConfiguration.value.shape == WidgetShape.FitCenterRectangle) {
                                OutlinedTextField(
                                    value = widgetConfiguration.value.cornerRadiusForFitCenter.toString(
                                        10
                                    ),
                                    onValueChange = {
                                        val asInt = it.toIntOrNull(10)
                                        if (asInt != null) {
                                            widgetConfiguration.value =
                                                widgetConfiguration.value.copy(
                                                    cornerRadiusForFitCenter = asInt
                                                )
                                        }
                                    },
                                    label = { Text(text = stringResource(id = R.string.corner_radius_input)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        autoCorrect = false,
                                        keyboardType = KeyboardType.Number
                                    ),
                                    enabled = refreshWidgets.value
                                )
                            }
                        }

                        Divider(color = DividerDefaults.color.copy(alpha = 0.12f))

                        Column(modifier = Modifier.padding(16.dp)) {

                            Text(text = stringResource(id = R.string.note_before_save))
                            Spacer(modifier = Modifier.height(24.dp))

                            Button(onClick = {
                                onSubmit(
                                    widgetConfiguration.value,
                                    allWidgetsConfiguration.value.copy(
                                        refreshDurationInMinutes =
                                        if (refreshWidgets.value)
                                            allWidgetsConfiguration.value.refreshDurationInMinutes
                                        else
                                            -1
                                    ),
                                )
                            }) {
                                Text(text = stringResource(id = R.string.save))
                            }
                        }
                    }
                }
            }
        )
    }
}
