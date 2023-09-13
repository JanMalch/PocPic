package io.github.janmalch.pocpic.widget.configuration

import androidx.annotation.Px

data class WidgetConfiguration(
    @Px val width: Int,
    @Px val height: Int,
    val shape: Shape,
) {

    enum class Shape {
        Circle,
        CenterCropRectangle,
        FitCenterRectangle
        ;

        companion object {
            fun find(name: String): Shape? = enumValues<Shape>().firstOrNull { it.name == name }
        }
    }
}
