package io.github.janmalch.pocpic.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.core.Picture
import io.github.janmalch.pocpic.ui.theme.Typography
import kotlinx.datetime.toJavaLocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun MainBottomBar(
    picture: Picture?,
    dispatch: (MainScreenIntent) -> Unit,
) {
    val formatter = remember {
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withZone(ZoneId.systemDefault())
    }
    BottomAppBar(
        actions = {
            if (picture != null) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(text = picture.label, maxLines = 1, style = Typography.titleMedium)
                    if (picture.date != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatter.format(picture.date.toJavaLocalDateTime()),
                            style = Typography.labelSmall
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            // TODO: find a way to share web images
            val enabled = remember(picture) { picture?.uri?.scheme == "content" }

            // FIXME: ripple was removed after dependency updates
            FloatingActionButton(
                containerColor = if (enabled) BottomAppBarDefaults.bottomAppBarFabColor
                else BottomAppBarDefaults.bottomAppBarFabColor.copy(alpha = 0.4f),
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                onClick = {
                    if (enabled && picture != null) {
                        dispatch(MainScreenIntent.SharePicture(picture))
                    }
                },
            ) {
                Icon(
                    Icons.Outlined.Share,
                    modifier = if (enabled) Modifier else Modifier.alpha(0.4f),
                    contentDescription = stringResource(R.string.open_list)
                )
            }
        }
    )
}
