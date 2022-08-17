package io.github.janmalch.pocpic.widget

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skydoves.landscapist.ShimmerParams
import com.skydoves.landscapist.glide.GlideImage
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.extensions.darken
import io.github.janmalch.pocpic.ui.photo.CurrentSourceViewModel

@Composable
fun WidgetConfigurationScreen(
    vm: CurrentSourceViewModel = viewModel(),
    onSubmit: (WidgetShape) -> Unit
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
                        IconButton(onClick = { vm.next() }) {
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

                        val source by vm.source.observeAsState()

                        WidgetShape.values().forEach { shape ->
                            ShapeDemo(shape = shape, imageModel = source?.imageModel) {
                                onSubmit(shape)
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ShapeDemo(
    shape: WidgetShape,
    imageModel: Uri?,
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
            GlideImage(
                imageModel = imageModel,
                previewPlaceholder = R.drawable.example_appwidget_preview,
                contentScale = when (shape) {
                    WidgetShape.FitCenterRectangle -> ContentScale.Fit
                    else -> ContentScale.Crop
                },
                modifier = if (shape == WidgetShape.Circle)
                    Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                else
                    Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(size = 24.dp)),
                shimmerParams = ShimmerParams(
                    baseColor = MaterialTheme.colorScheme.background.darken(0.2f),
                    highlightColor = MaterialTheme.colorScheme.background.darken(0.3f),
                    durationMillis = 500,
                    dropOff = 0.65f,
                    tilt = 20f
                ),
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
            text = stringResource(id = shape.label),
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = stringResource(id = shape.explanation),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
}
