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
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

@Composable
fun ExpensesScreen(
    viewModel: TransactionViewModel = viewModel(),
    navController: NavController
) {
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val errorState by viewModel.error.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val windowInfo = rememberWindowInfo()
    val currentMonth = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val tokenManager = remember { TokenManager(context) }

    // Fetch transactions on load
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val accessToken = tokenManager.getAccessToken()
            val sharedPrefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
            if (accessToken != null) {
                viewModel.fetchTransactions(accessToken)
            } else {
                viewModel.setError("No bank account linked. Please link your account in the Profile section.")
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
                // Monthly Spending Chart Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$currentMonth Spending",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TransactionPieChart(
                                transactions = allTransactions,
                                radiusOuter = 120.dp,
                                chartBarWidth = 30.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Transaction +")
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
                            Text("See All")
                        }
                    }

                    if (recentTransactions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No transactions yet",
                                style = MaterialTheme.typography.bodyLarge
                            )
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
            // Monthly Spending Chart Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$currentMonth Spending",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TransactionPieChart(
                            transactions = allTransactions,
                            radiusOuter = 100.dp,
                            chartBarWidth = 25.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Transaction +")
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
                    Text("See All")
                }
            }

            if (recentTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No transactions yet",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentTransactions) { transaction ->
                        TransactionItem(transaction)
                    }
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
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(
    viewModel: TransactionViewModel = viewModel(),
    navController: NavController
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(emptySet<StandardCategory>()) }
    var showFilters by remember { mutableStateOf(false) }

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

    // Filter transactions based on search query and selected categories
    val filteredTransactions = transactions.filter { transaction ->
        val matchesSearch = transaction.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategories.isEmpty() ||
                selectedCategories.any { it.displayName == transaction.category?.primary }
        matchesSearch && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            placeholder = { Text("Search transactions") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                Row {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Filters",
                            tint = if (selectedCategories.isNotEmpty())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Category Filters
        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(StandardCategory.values().filter { it != StandardCategory.INCOME }) { category ->
                    FilterChip(
                        selected = category in selectedCategories,
                        onClick = {
                            selectedCategories = if (category in selectedCategories) {
                                selectedCategories - category
                            } else {
                                selectedCategories + category
                            }
                        },
                        label = { Text(category.displayName) },
                        leadingIcon = if (category in selectedCategories) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else null
                    )
                }
            }
        }

        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isEmpty() && selectedCategories.isEmpty())
                        "No transactions found."
                    else
                        "No matching transactions found.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTransactions) { transaction ->
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
    var amountError by remember { mutableStateOf<String?>(null) }
    val categories = StandardCategory.values().map { it.displayName }

    // Amount validation function
    fun validateAmount(input: String): Boolean {
        return try {
            if (input.isEmpty()) return true
            // Allow numbers with optional decimal point and up to 2 decimal places
            val regex = """^\d*\.?\d{0,2}$""".toRegex()
            regex.matches(input) && input.toDoubleOrNull() != null
        } catch (e: Exception) {
            false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Transaction") },
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (validateAmount(newValue)) {
                            amount = newValue
                            amountError = null
                        } else {
                            amountError = "Please enter a valid amount"
                        }
                    },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    isError = amountError != null,
                    supportingText = {
                        if (amountError != null) {
                            Text(
                                text = amountError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
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
                    val validAmount = amount.toDoubleOrNull()
                    if (validAmount != null) {
                        onAddTransaction(description, amount, selectedCategory)
                    }
                },
                enabled = description.isNotBlank() &&
                        amount.isNotBlank() &&
                        amountError == null &&
                        amount.toDoubleOrNull() != null
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

