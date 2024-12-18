package com.example.trackify2

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen(viewModel: AppSettingsViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(navController)
            }
            composable("profile") {
                ProfileScreen(navController, viewModel)
            }
            composable("displaySettings") {
                DisplaySettingsScreen(navController, viewModel)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val homeNavController = rememberNavController()

    Column {
        TopAppBar(
            title = { Text("Hi, User") },
            actions = {
                IconButton(onClick = { navController.navigate("profile") }) {
                    Icon(Icons.Default.Person, "Profile")
                }
            }
        )

        TabRow(homeNavController)

        NavHost(
            navController = homeNavController,
            startDestination = "expenses"
        ) {
            composable("expenses") {
                ExpensesScreen(
                    onNavigateToAllTransactions = { homeNavController.navigate("allTransactions") }
                )
            }
            composable("allTransactions") {
                AllTransactionsScreen()
            }
            composable("report") {
                ReportScreen()
            }
            composable("leaderboard") {
                LeaderboardScreen()
            }
        }
    }
}