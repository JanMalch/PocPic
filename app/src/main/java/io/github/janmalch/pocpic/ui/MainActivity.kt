package io.github.janmalch.pocpic.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dagger.hilt.android.AndroidEntryPoint
import io.github.janmalch.pocpic.R
import io.github.janmalch.pocpic.ui.theme.PocPicTheme
import io.github.janmalch.shed.Shed


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mViewModel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            PocPicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                        val appUiState by mViewModel.appUiState.collectAsStateWithLifecycle()
                        val snackbarHostState = remember { SnackbarHostState() }

                        CollectAsEvent(mViewModel.changeError) {
                            snackbarHostState.showSnackbar(getString(R.string.error_while_changing_picture))
                        }

                        when (val uiState = appUiState) {
                            AppUiState.Initializing -> {
                                // FIXME: keep splashscreen
                            }

                            AppUiState.Onboarding -> OnboardingScreen(
                                onDirectorySelected = mViewModel::changeDirectory
                            )

                            is AppUiState.Ready -> PictureScreen(
                                picture = uiState.picture,
                                interval = { uiState.interval },
                                onDirectorySelected = mViewModel::changeDirectory,
                                onPictureClicked = mViewModel::changePicture,
                                onChangeInterval = mViewModel::changeInterval,
                                onGoToLicenses = {
                                    startActivity(
                                        Intent(
                                            this@MainActivity,
                                            OssLicensesMenuActivity::class.java
                                        )
                                    )
                                },
                                onGoToLogs = { Shed.startActivity(this@MainActivity) },
                                snackbarHostState = snackbarHostState,
                            )
                        }
                    }
                }
            }
        }
    }
}
