package com.example.trackify2

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun ExpensesScreen(
    viewModel: TransactionViewModel = viewModel(),
    navController: NavController
) {
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val errorState by viewModel.error.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val windowInfo = rememberWindowInfo()

    // Fetch transactions on load
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val sharedPrefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
            val accessToken = sharedPrefs.getString("ACCESS_TOKEN", "") ?: ""
            if (accessToken.isNotEmpty()) {
                viewModel.fetchTransactions(accessToken)
            } else {
                viewModel.setError("No access token found.")
            }
        }
    }

    if (windowInfo.screenWidthInfo is WindowInfo.WindowType.Expanded) {
        // Landscape layout
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Left column with chart and add button
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                // Chart Placeholder
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Chart Placeholder", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Transaction +")
                }
            }

            // Right column with transactions
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { navController.navigate("allTransactionsScreen") }) {
                            Text(
                                text = "See All",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (recentTransactions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No transactions yet", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(recentTransactions) { transaction ->
                                TransactionItem(transaction)
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Portrait layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Chart Placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chart Placeholder", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Transaction +")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { navController.navigate("allTransactionsScreen") }) {
                    Text(
                        text = "See All",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentTransactions) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }

    // Add Transaction Dialog
    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onAddTransaction = { description, amount, category ->
                viewModel.addTransaction(description, amount, category)
                showAddDialog = false
            }
        )
    }

    // Display Error
    errorState?.let {
        Text(text = it, color = MaterialTheme.colorScheme.error)
    }
}

// Add this class to detect window size
data class WindowInfo(
    val screenWidthInfo: WindowType,
    val screenHeightInfo: WindowType
) {
    sealed class WindowType {
        object Compact: WindowType()
        object Medium: WindowType()
        object Expanded: WindowType()
    }
}

@Composable
fun rememberWindowInfo(): WindowInfo {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        val screenWidth = configuration.screenWidthDp
        val screenHeight = configuration.screenHeightDp

        val widthInfo = when {
            screenWidth < 600 -> WindowInfo.WindowType.Compact
            screenWidth < 840 -> WindowInfo.WindowType.Medium
            else -> WindowInfo.WindowType.Expanded
        }

        val heightInfo = when {
            screenHeight < 480 -> WindowInfo.WindowType.Compact
            screenHeight < 900 -> WindowInfo.WindowType.Medium
            else -> WindowInfo.WindowType.Expanded
        }

        WindowInfo(widthInfo, heightInfo)
    }
}

@Composable
fun AllTransactionsScreen(
    viewModel: TransactionViewModel = viewModel(),
    navController: NavController
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Fetch transactions when screen loads
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val sharedPrefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
            val accessToken = sharedPrefs.getString("ACCESS_TOKEN", "") ?: ""
            if (accessToken.isNotEmpty()) {
                viewModel.fetchTransactions(accessToken)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "All Transactions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (transactions.isEmpty()) {
            Text("No transactions found.", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn {
                items(transactions) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onAddTransaction: (String, String, String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(StandardCategory.OTHER.displayName) }
    val categories = StandardCategory.values().map { it.displayName }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Transaction") },
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Dropdown Menu for Categories
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddTransaction(description, amount, selectedCategory)
                },
                enabled = description.isNotBlank() && amount.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



