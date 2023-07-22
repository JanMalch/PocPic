package io.github.janmalch.pocpic.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)


private val types = Typography::class.java.declaredFields.mapNotNull {
    it.isAccessible = true
    val style = it.get(Typography) as? TextStyle ?: return@mapNotNull null
    it.name to style
}.sortedBy { it.first }


@Preview
@Composable
internal fun TypographyPreview() {

    PocPicTheme(darkTheme = false) {
        Surface {
            Column {
                Text(text = "${types.size} Typographies:")
                Spacer(modifier = Modifier.height(8.dp))
                Divider(thickness = 1.dp, color = Color.LightGray)

                types.forEach { (name, style) ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = name,
                        style = style,
                    )
                }
            }
        }
    }
}