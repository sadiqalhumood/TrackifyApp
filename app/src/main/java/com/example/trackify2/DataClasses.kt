package com.example.trackify2

import androidx.compose.ui.graphics.vector.ImageVector
import com.squareup.moshi.Json
import io.teller.connect.sdk.Institution

data class Account(
    val id: String,
    @Json(name = "account_id") val accountId: String? = null, // Remove this if not needed
    val name: String,
    val type: String,
    val subtype: String,
    val status: String,
    @Json(name = "last_four") val lastFour: String,
    val currency: String,
    @Json(name = "enrollment_id") val enrollmentId: String,
    val institution: Institution,
    val links: AccountLinks
)

data class AccountLinks(
    val self: String,
    val transactions: String,
    val details: String,
    val balances: String
)


data class Transaction(
    val id: String,
    val account_id: String,
    val type: String,
    val status: String,
    val amount: String,
    val date: String,
    val description: String,
    @Json(name = "running_balance") val runningBalance: String?,
    val details: TransactionDetails,
    val links: TransactionLinks
)

data class TransactionDetails(
    @Json(name = "processing_status") val processingStatus: String,
    val counterparty: Counterparty,
    val category: String
)

data class Counterparty(
    val type: String,
    val name: String
)

data class TransactionLinks(
    val self: String,
    val account: String
)

data class BottomNavItem(val label: String, val route: String, val icon: ImageVector)
