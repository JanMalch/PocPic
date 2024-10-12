package io.github.janmalch.pocpic.widget.configuration

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import io.github.janmalch.pocpic.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigurationScreen(
    vm: WidgetConfigurationViewModel,
    onSelect: (WidgetData.Shape) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = stringResource(id = R.string.select_shape)) },
                    actions = {
                        IconButton(onClick = { vm.reroll() }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = stringResource(id = R.string.swap_picture)
                            )
                        }
                    }
                )
            },
            content = { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .verticalScroll(state = scrollState)
                            .padding(16.dp)
                    ) {

                        val uri by vm.selectedUri.collectAsState(initial = null)

                        WidgetData.Shape.entries.forEach { shape ->
                            ShapeDemo(shape = shape, uri = uri?.let(Uri::parse), onClick = {
                                onSelect(shape)
                            })
                        }
                    }
                }
            }
        )
    }
}


@Composable
fun ShapeDemo(
    shape: WidgetData.Shape,
    uri: Uri?,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(size = 24.dp))
            .clickable { onClick() }
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            // FIXME: replace with coil
            GlideImage(
                imageModel = { uri },
                // previewPlaceholder = R.drawable.example_appwidget_preview,
                imageOptions = ImageOptions(
                    contentScale = when (shape) {
                        WidgetData.Shape.FitCenterRectangle -> ContentScale.Fit
                        else -> ContentScale.Crop
                    },
                    alignment = Alignment.Center
                ),
                modifier = if (shape == WidgetData.Shape.Circle)
                    Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                else
                    Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(size = 24.dp)),
            )
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = onSurfaceColor,
                    style = Stroke(
                        width = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )
            }
        }

        Text(
            text = stringResource(
                when (shape) {
                    WidgetData.Shape.Circle -> R.string.shape_circle
                    WidgetData.Shape.CenterCropRectangle -> R.string.shape_center_crop_rectangle
                    WidgetData.Shape.FitCenterRectangle -> R.string.shape_fit_center_rectangle
                }
            ),
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = stringResource(
                when (shape) {
                    WidgetData.Shape.Circle -> R.string.shape_circle_explanation
                    WidgetData.Shape.CenterCropRectangle -> R.string.shape_center_crop_rectangle_explanation
                    WidgetData.Shape.FitCenterRectangle -> R.string.shape_fit_center_rectangle_explanation
                }
            ),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
}


@Preview
@Composable
internal fun ShapeDemoCircle() {
    ShapeDemo(
        shape = WidgetData.Shape.Circle,
        uri = Uri.parse("https://images.unsplash.com/photo-1633722715463-d30f4f325e24?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=774&q=80"),
        onClick = {}
    )
}


@Preview
@Composable
internal fun ShapeDemoCenterCropRectangle() {
    ShapeDemo(
        shape = WidgetData.Shape.CenterCropRectangle,
        uri = Uri.parse("https://images.unsplash.com/photo-1633722715463-d30f4f325e24?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=774&q=80"),
        onClick = {}
    )
}

@Preview
@Composable
internal fun ShapeDemoFitCenterRectangle() {
    ShapeDemo(
        shape = WidgetData.Shape.FitCenterRectangle,
        uri = Uri.parse("https://images.unsplash.com/photo-1633722715463-d30f4f325e24?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=774&q=80"),
        onClick = {}
    )
}