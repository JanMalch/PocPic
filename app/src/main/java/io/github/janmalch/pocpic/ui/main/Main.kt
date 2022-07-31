package io.github.janmalch.pocpic.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.janmalch.pocpic.AppViewModel
import io.github.janmalch.pocpic.ui.config.ConfigScreen
import io.github.janmalch.pocpic.ui.photo.PhotoScreen

@Composable
fun MainScreen(
    vm: AppViewModel = viewModel()
) {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(navController = navController, startDestination = NavScreen.Photo.route) {
            composable(NavScreen.Photo.route) {
                PhotoScreen(vm = vm) {
                    navController.navigate(NavScreen.Config.route)
                }
            }
            composable(NavScreen.Config.route) {
                ConfigScreen(vm = vm) {
                    navController.navigateUp()
                }
            }
        }
    }
}

sealed class NavScreen(val route: String) {
    object Photo : NavScreen("Photo")
    object Config : NavScreen("Config")
}
