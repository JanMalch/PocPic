package io.github.janmalch.pocpic.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import io.github.janmalch.pocpic.data.AppDatabase
import io.github.janmalch.pocpic.data.SourceFactoryConfigRepository
import io.github.janmalch.pocpic.models.PictureSource
import io.github.janmalch.pocpic.ui.theme.PocPicTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var repository: SourceFactoryConfigRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = SourceFactoryConfigRepository(
            AppDatabase.getDatabase(this.applicationContext).configsDao()
        )

        val initialSource = intent.getParcelableExtra<PictureSource>(EXTRA_INITIAL_SOURCE)
        // FIXME: use initialSource if provided

        setContent {
            PocPicTheme {
                MainScreen()
            }
        }
    }

    companion object {
        const val EXTRA_INITIAL_SOURCE = "MainActivity_EXTRA_INITIAL_SOURCE"
    }
}
