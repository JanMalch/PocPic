package io.github.janmalch.pocpic.extensions

import android.content.Context
import android.util.TypedValue

fun Number.dpToPx(context: Context) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    context.resources.displayMetrics
).toInt()
