package io.github.janmalch.pocpic.extensions

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle

@Composable
fun enabledTextStyle(enabled: Boolean): TextStyle {
    val textStyle = LocalTextStyle.current
    val colors = TextFieldDefaults.outlinedTextFieldColors()
    val textColor = textStyle.color.takeOrElse {
        colors.textColor(enabled).value
    }
    return textStyle.merge(TextStyle(color = textColor))
}
