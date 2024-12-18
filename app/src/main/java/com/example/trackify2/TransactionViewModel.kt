package com.example.trackify2

import android.app.Application
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackify2.data.local.AppDatabase
import com.example.trackify2.data.local.TransactionEntity
import com.example.trackify2.data.remote.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val tellerApiService: TellerApiService = RetrofitInstance.getTellerApi(application)
    private val database = AppDatabase.getDatabase(application)
    private val transactionDao = database.transactionDao()
    private val firestoreService = FirestoreService()

    private val _allTransactions = MutableStateFlow<MutableList<Transaction>>(mutableListOf())
    val allTransactions: StateFlow<List<Transaction>> = _allTransactions

    private val _recentTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val recentTransactions: StateFlow<List<Transaction>> = _recentTransactions

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun setError(message: String?) {
        _error.value = message
    }

    fun clearError() {
        _error.value = null
    }

    suspend fun fetchTransactions(accessToken: String) {
        try {
            val authHeader = createAuthHeader(accessToken)
            val accounts = tellerApiService.getAccounts(authHeader)

            if (accounts.isNotEmpty()) {
                val accountId = accounts[0].id
                val fetchedTransactions: List<Transaction> = tellerApiService.getTransactions(
                    authorization = authHeader,
                    accountId = accountId
                )

                // Save API transactions to Room
                transactionDao.deleteAllApiTransactions()
                transactionDao.insertTransactions(fetchedTransactions.map {
                    it.toEntity(isManual = false)
                })

                // Get manual transactions
                val manualTransactions: List<Transaction> = transactionDao.getManualTransactions()
                    .first() // Collect the flow to get the actual list
                    .map { entity -> entity.toTransaction() }

                // Merge and sort the transactions by date
                val mergedTransactions: List<Transaction> = (fetchedTransactions + manualTransactions).sortedByDescending { transaction ->
                    transaction.date
                }

                // Update UI
                updateTransactionLists(mergedTransactions)

                // Save fetched transactions to Firestore
                try {
                    firestoreService.saveTransactions(fetchedTransactions)
                } catch (e: Exception) {
                    Timber.e(e, "Firestore save failed.")
                }
            } else {
                setError("No accounts found.")
            }
        } catch (e: Exception) {
            setError("Error fetching transactions: ${e.message}")
        }
    }



    fun addTransaction(description: String, amount: String, category: String) {
        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    id = "manual_${System.currentTimeMillis()}",
                    description = description,
                    amount = if (amount.startsWith("-")) amount else "-$amount",
                    date = java.time.LocalDate.now().toString(),
                    details = TransactionDetails(counterparty = Counterparty(name = category)),
                    category = TransactionCategory(primary = category, detailed = category)
                )

                // Save to Room
                transactionDao.insertTransaction(transaction.toEntity(isManual = true))

                // Update UI
                val updatedList = _allTransactions.value.toMutableList()
                updatedList.add(0, transaction)
                updateTransactionLists(updatedList)

                // Save to Firestore if user is logged in
                FirebaseAuth.getInstance().currentUser?.let {
                    try {
                        firestoreService.saveTransaction(transaction)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to save manual transaction to Firestore")
                    }
                }
            } catch (e: Exception) {
                setError("Error adding transaction: ${e.message}")
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                // Delete from Room
                transactionDao.deleteTransactionById(transactionId)

                // Update UI
                val updatedList = _allTransactions.value.filter { it.id != transactionId }
                updateTransactionLists(updatedList)

                // Delete from Firestore
                firestoreService.deleteTransaction(transactionId)
            } catch (e: Exception) {
                setError("Error deleting transaction: ${e.message}")
            }
        }
    }

    private fun updateTransactionLists(transactions: List<Transaction>) {
        _allTransactions.value = transactions.toMutableList()
        _recentTransactions.value = transactions.take(3)
    }

    private fun createAuthHeader(accessToken: String): String {
        val credentials = "$accessToken:"
        return "Basic ${Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)}"
    }

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private fun Transaction.toEntity(isManual: Boolean) = TransactionEntity(
        id = id,
        description = description,
        amount = amount,
        date = dateFormatter.format(LocalDate.parse(date)), // Ensure proper formatting
        details = details,
        category = category,
        isManual = isManual
    )

    private fun TransactionEntity.toTransaction() = Transaction(
        id = id,
        description = description,
        amount = amount,
        date = LocalDate.parse(date, dateFormatter).toString(), // Ensure proper conversion
        details = details,
        category = category
    )
}
