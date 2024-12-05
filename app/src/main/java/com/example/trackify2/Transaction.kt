package com.example.trackify2

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trackify2.ui.theme.Trackify2Theme


@Composable
fun TransactionApp() {
    val viewModel: AppSettingsViewModel = viewModel()
    val isDarkMode = viewModel.isDarkMode.collectAsState()

    Trackify2Theme(darkTheme = isDarkMode.value) {
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
                    val context = LocalContext.current
                    val sharedPrefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
                    val storedAccessToken = sharedPrefs.getString("ACCESS_TOKEN", null)

                    if (storedAccessToken == null) {

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No bank account linked.",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { navController.navigate("profile") }) {
                                Text("Go to Profile")
                            }
                        }
                    } else {

                        val transactionViewModel: TransactionViewModel = viewModel()

                        TransactionScreen(
                            viewModel = transactionViewModel,
                            accessToken = storedAccessToken
                        )
                    }
                }

                composable("profile") { ProfileScreen(navController, viewModel) }
                composable("displaySettings") { DisplaySettingsScreen(navController, viewModel) }
            }
        }
    }
}


@Composable
fun TransactionScreen(viewModel: TransactionViewModel, accessToken: String) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    val accountId = sharedPrefs.getString("ACCOUNT_ID", null)

    LaunchedEffect(Unit) {
        if (accountId == null) {

            viewModel.fetchAccountId(accessToken) { fetchedAccountId ->
                sharedPrefs.edit().putString("ACCOUNT_ID", fetchedAccountId).apply()
                viewModel.fetchTransactions(accessToken, fetchedAccountId)
            }
        } else {
            viewModel.fetchTransactions(accessToken, accountId)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Transactions", fontSize = MaterialTheme.typography.titleLarge.fontSize, fontWeight = FontWeight.Bold)

        when {
            error != null -> {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }
            transactions.isEmpty() -> {
                Text("Loading transactions...")
            }
            else -> {
                LazyColumn {
                    items(transactions) { transaction ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(transaction.name, fontWeight = FontWeight.Bold)
                                    Text(transaction.date, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(transaction.amount, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
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
                        // Ensure only one instance of the destination exists in the back stack
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

