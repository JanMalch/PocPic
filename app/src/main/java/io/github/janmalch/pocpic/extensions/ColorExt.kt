package io.github.janmalch.pocpic.extensions

import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

fun Color.darken(@FloatRange(from = 0.0, to = 1.0) ratio: Float) =
    Color(ColorUtils.blendARGB(this.toArgb(), Color.Black.toArgb(), ratio))

fun Color.lighten(@FloatRange(from = 0.0, to = 1.0) ratio: Float) =
    Color(ColorUtils.blendARGB(this.toArgb(), Color.White.toArgb(), ratio))
