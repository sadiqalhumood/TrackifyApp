package com.example.trackify2

import android.app.Application
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val tellerApiService: TellerApiService = RetrofitInstance.getTellerApi(application)

    // MutableList to store transactions
    private val transactionList = mutableListOf<Transaction>()

    // Exposed StateFlow to observe changes
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Fetch transactions from the API and update the list.
     */
    suspend fun fetchTransactions(accessToken: String) {
        try {
            val authHeader = createAuthHeader(accessToken)
            val accounts = tellerApiService.getAccounts(authHeader)

            if (accounts.isNotEmpty()) {
                val accountId = accounts[0].id
                val apiTransactions = tellerApiService.getTransactions(
                    authorization = authHeader,
                    accountId = accountId
                )

                // Populate the transaction list with API data
                transactionList.clear()
                transactionList.addAll(apiTransactions)
                _transactions.value = transactionList // Notify the UI
                _error.value = null
            } else {
                setError("No accounts found.")
            }
        } catch (e: Exception) {
            setError("Error fetching transactions: ${e.message}")
        }
    }

    /**
     * Add a new transaction to the list.
     */
    fun addTransaction(description: String, amount: String, category: String) {
        val newTransaction = Transaction(
            id = "manual_${System.currentTimeMillis()}",
            description = description,
            amount = amount,
            date = "Manual Entry",
            details = TransactionDetails(counterparty = Counterparty(name = category))
        )
        transactionList.add(newTransaction)
        _transactions.value = transactionList // Notify the UI
    }

    /**
     * Delete a transaction from the list by ID.
     */
    fun deleteTransaction(transactionId: String) {
        transactionList.removeAll { it.id == transactionId }
        _transactions.value = transactionList // Notify the UI
    }

    /**
     * Set an error message.
     */
    fun setError(message: String) {
        _error.value = message
    }

    /**
     * Create authorization header for Teller API.
     */
    private fun createAuthHeader(accessToken: String): String {
        val credentials = "$accessToken:"
        return "Basic ${Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)}"
    }
}
