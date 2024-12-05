package com.example.trackify2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TransactionViewModel : ViewModel() {
    private val functions = FirebaseFunctions.getInstance()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    companion object {
        const val FETCH_ACCOUNT_ID_FUNCTION = "fetchAccountId"
        const val FETCH_TRANSACTIONS_FUNCTION = "fetchTransactions"
    }

    fun fetchAccountId(accessToken: String, onAccountIdFetched: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val data = hashMapOf("accessToken" to accessToken)
                val result = functions
                    .getHttpsCallable(FETCH_ACCOUNT_ID_FUNCTION)
                    .call(data)
                    .await()

                val accountId = result.getData() as? String
                if (accountId.isNullOrEmpty()) {
                    _error.value = "Failed to fetch account ID: Empty response"
                } else {
                    onAccountIdFetched(accountId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Failed to fetch account ID: ${e.localizedMessage}"
            }
        }
    }

    fun fetchTransactions(accessToken: String, accountId: String) {
        viewModelScope.launch {
            try {
                val data = hashMapOf(
                    "accessToken" to accessToken,
                    "accountId" to accountId
                )

                val result = functions
                    .getHttpsCallable(FETCH_TRANSACTIONS_FUNCTION)
                    .call(data)
                    .await()

                @Suppress("UNCHECKED_CAST")
                val transactionsList = result.getData() as? List<Map<String, Any>>
                if (transactionsList.isNullOrEmpty()) {
                    _error.value = "No transactions found"
                } else {
                    _transactions.value = transactionsList.map { transactionData ->
                        Transaction(
                            id = transactionData["id"] as String,
                            name = transactionData["name"] as String,
                            date = transactionData["date"] as String,
                            amount = transactionData["amount"] as String
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = e.localizedMessage ?: "An error occurred"
            }
        }
    }
}

