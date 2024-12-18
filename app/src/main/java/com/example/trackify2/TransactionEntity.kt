package com.example.trackify2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.example.trackify2.TransactionCategory
import com.example.trackify2.TransactionDetails
import java.util.Date

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val description: String,
    val amount: String,
    val date: String,
    val details: TransactionDetails,
    val category: TransactionCategory?,
    val isManual: Boolean // To distinguish between manual and API transactions
)



class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTransactionDetails(details: TransactionDetails): String {
        return gson.toJson(details)
    }

    @TypeConverter
    fun toTransactionDetails(value: String): TransactionDetails {
        return gson.fromJson(value, TransactionDetails::class.java)
    }

    @TypeConverter
    fun fromTransactionCategory(category: TransactionCategory?): String? {
        return category?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toTransactionCategory(value: String?): TransactionCategory? {
        return value?.let { gson.fromJson(it, TransactionCategory::class.java) }
    }
}