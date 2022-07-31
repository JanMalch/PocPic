package io.github.janmalch.pocpic.ui.photo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.janmalch.pocpic.AppViewModel
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.ui.components.ImageWithSource

@Composable
fun PhotoScreen(
    vm: AppViewModel,
    goToConfig: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { goToConfig() }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(id = R.string.sources)
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            val source by vm.source.observeAsState()

            if (source != null) {

                Box(
                    modifier = Modifier
                        .clickable {
                            vm.nextSource()
                        }
                        .padding(innerPadding)
                ) {
                    ImageWithSource(
                        source = source!!,
                        modifier = Modifier
                            .padding(
                                top = 8.dp,
                                bottom = 24.dp,
                                start = 24.dp,
                                end = 24.dp,
                            ),
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(id = R.string.nothing_here),
                    )
                }
            }
        }
    )
}
