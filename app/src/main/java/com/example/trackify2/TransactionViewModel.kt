package com.example.trackify2

import android.app.Application
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.net.ssl.SSLHandshakeException


class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val tellerApiService: TellerApiService = RetrofitInstance.getTellerApi(application)
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun setError(message: String) {
        _error.value = message
    }

    suspend fun fetchTransactions(accessToken: String) {
        try {
            val authHeader = createAuthHeader(accessToken)
            // First fetch the account
            val accounts = tellerApiService.getAccounts(authHeader)

            if (accounts.isNotEmpty()) {
                val accountId = accounts[0].id
                val transactions = tellerApiService.getTransactions(
                    authorization = authHeader,
                    accountId = accountId
                )
                _transactions.value = transactions
                _error.value = null
                Timber.d("Fetched ${transactions.size} transactions")
            } else {
                _error.value = "No accounts found"
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching transactions: ${e.message}")
            _error.value = "Error fetching transactions: ${e.message}"
        }
    }

    private fun createAuthHeader(accessToken: String): String {
        val credentials = "$accessToken:"
        return "Basic ${Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)}"
    }
}

