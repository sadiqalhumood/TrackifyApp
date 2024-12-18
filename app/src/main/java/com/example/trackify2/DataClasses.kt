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
    val description: String,
    val amount: String,
    val date: String,
    val account_id: String = "", // Default empty string
    val links: Map<String, String> = emptyMap(), // Default empty map
    val runningBalance: String = "", // Default empty string
    val status: String = "", // Default empty string
    val type: String = "", // Default empty string
    val details: TransactionDetails
)

data class TransactionDetails(
    val counterparty: Counterparty,
    val category: String = "", // Default empty string
    val subcategory: String = "" // Default empty string
)

data class Counterparty(
    val name: String
)


data class TransactionLinks(
    val self: String,
    val account: String
)



