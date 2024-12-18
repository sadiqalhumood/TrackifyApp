package com.example.trackify2

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trackify2.ui.theme.Trackify2Theme
import com.google.firebase.FirebaseApp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            // Check login state using SharedPreferences
            val sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

            val viewModel: AppSettingsViewModel = viewModel(
                factory = AppSettingsViewModel.provideFactory(this)
            )
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            Trackify2Theme(darkTheme = isDarkMode) {
                AppNavigation(isLoggedIn, viewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(isLoggedIn: Boolean, viewModel: AppSettingsViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "main" else "login"
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

