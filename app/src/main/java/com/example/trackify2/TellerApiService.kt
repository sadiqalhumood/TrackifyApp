package com.example.trackify2

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface TellerApiService {
    @GET("accounts")
    suspend fun getAccounts(
        @Header("Authorization") accessToken: String
    ): List<Account>


    @GET("accounts/{account_id}/transactions")
    suspend fun getTransactions(
        @Header("Authorization") accessToken: String,
        @Path("account_id") accountId: String
    ): TransactionResponse
}

