package io.github.janmalch.pocpic.ui.main

import android.content.Intent
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.core.Picture
import io.github.janmalch.pocpic.ui.CollectAsEvent
import io.github.janmalch.pocpic.ui.destinations.SourcesScreenDestination


sealed interface MainScreenIntent {
    data object OpenList : MainScreenIntent
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
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val picture by vm.currentSource.collectAsState(initial = null)

    CollectAsEvent(vm.share) {
        context.startActivity(Intent.createChooser(it, null))
    }

    CollectAsEvent(vm.changeError) {
        snackbarHostState.showSnackbar(context.getString(R.string.error_while_changing_picture))
    }

    CollectAsEvent(vm.shareError) {
        snackbarHostState.showSnackbar(context.getString(R.string.error_while_sharing_picture))
    }

    MainScreen(
        picture = picture,
        snackbarHostState = snackbarHostState,
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
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
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
        snackbarHost = {
            SnackbarHost(snackbarHostState)
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
        ) { // FIXME: replace with coil
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