package io.github.janmalch.pocpic.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.domain.Picture
import io.github.janmalch.pocpic.ui.destinations.SourcesScreenDestination


sealed interface MainScreenIntent {
    object OpenList : MainScreenIntent
    data class ChangePicture(val current: Picture?) : MainScreenIntent
    data class SharePicture(val picture: Picture) : MainScreenIntent
}

@RootNavGraph(start = true)
@Destination
@Composable
fun MainScreen(
    navigator: DestinationsNavigator,
    vm: MainViewModel,
) {
    val picture by vm.currentSource.collectAsState(initial = null)
    MainScreen(
        picture = picture,
        dispatch = {
            when (it) {
                MainScreenIntent.OpenList -> navigator.navigate(SourcesScreenDestination)
                is MainScreenIntent.ChangePicture -> vm.changePicture()
                is MainScreenIntent.SharePicture -> vm.shareCurrentSource()
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    picture: Picture?,
    dispatch: (MainScreenIntent) -> Unit,
) {

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { dispatch(MainScreenIntent.OpenList) }) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.open_list)
                        )
                    }
                }
            )
        },

        bottomBar = {
            MainBottomBar(picture = picture, dispatch = dispatch)
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .clickable {
                dispatch(MainScreenIntent.ChangePicture(picture))
            }
        ) {
            GlideImage(
                imageModel = { picture?.uri },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Inside,
                    alignment = Alignment.Center,
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}