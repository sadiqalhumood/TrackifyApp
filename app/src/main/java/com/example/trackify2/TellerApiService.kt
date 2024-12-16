package com.example.trackify2

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Url

interface TellerApiService {
    @GET("accounts")
    suspend fun getAccounts(
        @Header("Authorization") authorization: String,
        @Header("Teller-Version") tellerVersion: String = "2020-10-12"
    ): List<Account>

    @GET("accounts/{account_id}/transactions")
    suspend fun getTransactions(
        @Header("Authorization") authorization: String,
        @Header("Teller-Version") tellerVersion: String = "2020-10-12",
        @Path("account_id") accountId: String
    ): List<Transaction>
}


