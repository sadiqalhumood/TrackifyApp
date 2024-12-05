import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ExpensesScreen(navController: NavController) {
    var transactions by remember { mutableStateOf(listOf<Transaction>()) }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chart Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for chart
            Text(
                text = "Chart Placeholder",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Gray
            )
        }

        // "Transaction +" Button
        Button(
            onClick = { showDialog = true }, // Show dialog when clicked
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Transaction +", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transactions Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Transactions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See all",
                fontSize = 14.sp,
                color = Color.Blue,
                modifier = Modifier.clickable {
                    navController.navigate("transactionHistory")
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transactions List
        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No transactions yet.", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn {
                items(transactions) { transaction ->
                    TransactionItem(transaction.name, transaction.time, transaction.amount)
                }
            }
        }
    }

    // Transaction Input Dialog
    if (showDialog) {
        TransactionInputDialog(
            onDismiss = { showDialog = false },
            onAddTransaction = { transaction ->
                transactions = transactions + transaction // Add the new transaction to the list
            }
        )
    }
}

// Transaction Data Class
data class Transaction(val name: String, val time: String, val amount: String)

// Transaction Input Dialog
@Composable
fun TransactionInputDialog(onDismiss: () -> Unit, onAddTransaction: (Transaction) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Transaction") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && amount.isNotBlank()) {
                        val transaction = Transaction(
                            name = name,
                            time = "Just now",
                            amount = "$$amount"
                        )
                        onAddTransaction(transaction)
                        onDismiss()
                    }
                }
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

// Transaction Item UI
@Composable
fun TransactionItem(name: String, time: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = time, fontSize = 14.sp, color = Color.Gray)
        }

        Text(text = amount, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
