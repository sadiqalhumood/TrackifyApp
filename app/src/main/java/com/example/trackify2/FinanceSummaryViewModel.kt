package com.example.trackify2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackify2.data.local.AppDatabase
import com.example.trackify2.data.local.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FinanceSummaryViewModel(application: Application) : AndroidViewModel(application) {
    private val transactionDao = AppDatabase.getDatabase(application).transactionDao()

    private val _netIncome = MutableStateFlow(0.0)
    val netIncome: StateFlow<Double> = _netIncome

    private val _netSpending = MutableStateFlow(0.0)
    val netSpending: StateFlow<Double> = _netSpending

    init {
        viewModelScope.launch {
            try {
                val transactions = transactionDao.getAllTransactions()
                    .first() // Collect the flow to get the actual data
                    .map { entity -> entity.toTransaction() } // Map each entity to Transaction
                calculateSummary(transactions)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun calculateSummary(transactions: List<Transaction>) {
        val income = transactions.filter { it.category?.primary == "Income" }
            .sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        val spending = transactions.filter { it.category?.primary != "Income" }
            .sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        _netIncome.value = income
        _netSpending.value = spending
    }

    private fun TransactionEntity.toTransaction(): Transaction {
        return Transaction(
            id = this.id,
            description = this.description,
            amount = this.amount,
            date = this.date,
            details = this.details,
            category = this.category
        )
    }
}
