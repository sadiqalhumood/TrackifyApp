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
    val details: TransactionDetails,
    val category: TransactionCategory? = null
)

data class TransactionCategory(
    val primary: String,
    val detailed: String
)

data class TransactionDetails(
    val counterparty: Counterparty
)

data class Counterparty(
    val name: String
)



data class TransactionLinks(
    val self: String,
    val account: String
)

enum class StandardCategory(val displayName: String) {
    FOOD_AND_DRINK("Food & Drink"),
    TRANSPORTATION("Transportation"),
    SHOPPING("Shopping"),
    UTILITIES("Utilities"),
    RENT_AND_MORTGAGE("Housing"),
    TRAVEL("Travel"),
    INCOME("Income"),
    TRANSFER("Transfer"),
    ENTERTAINMENT("Entertainment"),
    HEALTH_AND_FITNESS("Health"),
    EDUCATION("Education"),
    OTHER("Other");

    companion object {
        fun fromDisplayName(name: String): StandardCategory {
            return values().find { it.displayName == name } ?: OTHER
        }
    }
}



