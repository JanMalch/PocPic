package io.github.janmalch.pocpic.ui.sources

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.domain.Source
import io.github.janmalch.pocpic.ui.destinations.SourceDialogDestination

sealed interface SourcesScreenIntent {
    object NavigateUp : SourcesScreenIntent
    object NewSource : SourcesScreenIntent
    data class EditSource(val source: Source) : SourcesScreenIntent
}

@Destination
@Composable
fun SourcesScreen(
    navigator: DestinationsNavigator,
    vm: SourcesViewModel = hiltViewModel()
) {
    val sources by vm.sources.collectAsState(initial = emptyList())
    SourcesScreen(
        sources = sources,
        dispatch = {
            when (it) {
                SourcesScreenIntent.NavigateUp -> navigator.navigateUp()
                is SourcesScreenIntent.NewSource -> navigator.navigate(SourceDialogDestination())
                is SourcesScreenIntent.EditSource -> navigator.navigate(SourceDialogDestination(it.source))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesScreen(
    sources: List<Source>,
    dispatch: (SourcesScreenIntent) -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { dispatch(SourcesScreenIntent.NavigateUp) }) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(
                            Intent(
                                context,
                                OssLicensesMenuActivity::class.java
                            )
                        )
                    }) {
                        Icon(
                            Icons.Outlined.Code,
                            contentDescription = stringResource(R.string.oss_licenses)
                        )
                    }

                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                dispatch(SourcesScreenIntent.NewSource)
            }) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.new_source))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                itemsIndexed(items = sources) { index, it ->
                    ListItem(
                        leadingContent = { SourceTypeIcon(it.type) },
                        headlineContent = { Text(it.label) },
                        modifier = Modifier.clickable {
                            dispatch(SourcesScreenIntent.EditSource(it))
                        }
                    )
                    if (index < sources.lastIndex) {
                        Divider()
                    }
                }
            }
        }
    }
}
