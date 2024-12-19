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
    SHOPPING("Shopping"),
    TRANSPORTATION("Transportation"),
    BILLS_AND_UTILITIES("Bills & Utilities"),
    ENTERTAINMENT("Entertainment"),
    HEALTH("Health"),
    INCOME("Income"),
    TRANSFER("Transfer"),
    OTHER("Other");

    companion object {
        fun fromDisplayName(displayName: String): StandardCategory {
            return values().find { it.displayName.equals(displayName, ignoreCase = true) } ?: OTHER
        }

        fun fromDescription(description: String, amount: String): StandardCategory {
            // Check if it's income based on positive amount
            if (amount.toDoubleOrNull()?.let { it > 0 } == true) {
                return INCOME
            }

            val lowerDesc = description.lowercase()
            return when {
                // Food & Drink patterns
                lowerDesc.containsAny("restaurant", "cafe", "coffee", "food", "burger", "pizza", "doordash", "uber eats", "grubhub")
                -> FOOD_AND_DRINK

                // Shopping patterns
                lowerDesc.containsAny("amazon", "walmart", "target", "store", "market", "shop", "retail")
                -> SHOPPING

                // Transportation patterns
                lowerDesc.containsAny("uber", "lyft", "taxi", "gas", "fuel", "parking", "transit", "transport")
                -> TRANSPORTATION

                // Bills & Utilities patterns
                lowerDesc.containsAny("electric", "water", "utility", "utilities", "bill", "insurance", "rent", "mortgage")
                -> BILLS_AND_UTILITIES

                // Entertainment patterns
                lowerDesc.containsAny("movie", "theater", "netflix", "spotify", "hulu", "disney", "entertainment")
                -> ENTERTAINMENT

                // Health patterns
                lowerDesc.containsAny("pharmacy", "doctor", "medical", "health", "fitness", "gym")
                -> HEALTH

                // Transfer patterns
                lowerDesc.containsAny("transfer", "zelle", "venmo", "paypal", "cash app", "withdrawal", "deposit")
                -> TRANSFER

                // Default to OTHER if no patterns match
                else -> OTHER
            }
        }

        private fun String.containsAny(vararg keywords: String): Boolean {
            return keywords.any { this.contains(it) }
        }
    }
}




