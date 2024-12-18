package com.example.trackify2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trackify2.ui.theme.Trackify2Theme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            // Fetch dark mode preference from ViewModel
            val viewModel: AppSettingsViewModel = viewModel(
                factory = AppSettingsViewModel.provideFactory(this)
            )
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            // Apply the theme and navigate
            Trackify2Theme(darkTheme = isDarkMode) {
                AppNavigation(viewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: AppSettingsViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login" // Always start at the login screen
    ) {
        composable("login") {
            LoginScreen(navController)
        }
        composable("signup") {
            SignupScreen(navController)
        }
        composable("main") {
            MainScreen(viewModel = viewModel)
        }
        composable("displaySettings") {
            DisplaySettingsScreen(navController, viewModel)
        }
    }
}
