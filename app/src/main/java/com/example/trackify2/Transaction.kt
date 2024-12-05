package com.example.trackify2

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trackify2.ui.theme.Trackify2Theme

@Composable
fun TransactionApp() {
    val transactionViewModel: TransactionViewModel = viewModel()
    val settingsViewModel: AppSettingsViewModel = viewModel()

    Trackify2Theme(darkTheme = settingsViewModel.isDarkMode.collectAsState().value) {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController)
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") {
                    TransactionScreens(navController, transactionViewModel)
                }
                composable("profile") {
                    ProfileScreen(navController, settingsViewModel)
                }
                composable("transactionHistory") {
                    TransactionHistoryScreen(navController, transactionViewModel)
                }
                composable("displaySettings") {
                    DisplaySettingsScreen(navController, settingsViewModel)
                }
            }

        }
        }
    }

@Composable
fun TransactionScreens(
    navController: NavController,
    transactionViewModel: TransactionViewModel
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("Expenses", "Report", "Leaderboard").forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTabIndex) {
            0 -> TransactionScreen(navController, transactionViewModel)
            1 -> ReportScreen(navController)
            2 -> LeaderboardScreen(navController)
        }
    }
}

@Composable
fun TransactionHistoryScreen(navController: NavController, transactionViewModel: TransactionViewModel) {
    val transactions by transactionViewModel.transactions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Transaction History",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions found.", color = Color.Gray)
            }
        } else {
            LazyColumn {
                items(transactions) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(label = "Home", route = "home", icon = Icons.Default.Home),
        BottomNavItem(label = "Profile", route = "profile", icon = Icons.Default.Person)
    )

    val currentDestination = navController.currentBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(val label: String, val route: String, val icon: ImageVector)
