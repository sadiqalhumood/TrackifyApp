package com.example.trackify2

import androidx.compose.ui.graphics.vector.ImageVector
import com.squareup.moshi.Json

data class Account(
    @Json(name = "account_id") val accountId: String,
    val name: String,
    val subtype: String,
    val currency: String
)

data class TransactionResponse(
    @Json(name = "transactions") val transactions: List<Transaction>
)

data class Transaction(
    val id: String,
    val name: String,
    val date: String,
    val amount: String
)

data class BottomNavItem(val label: String, val route: String, val icon: ImageVector)
