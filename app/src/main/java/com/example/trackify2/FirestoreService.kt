package com.example.trackify2.data.remote

import com.example.trackify2.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun saveTransaction(transaction: Transaction) {
        auth.currentUser?.uid?.let { userId ->
            try {
                db.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .document(transaction.id)
                    .set(transaction)
                    .await()
            } catch (e: Exception) {
                Timber.e(e, "Error saving transaction to Firestore")
                // Don't throw, just log
            }
        }
    }

    suspend fun saveTransactions(transactions: List<Transaction>) {
        auth.currentUser?.uid?.let { userId ->
            try {
                val batch = db.batch()

                transactions.forEach { transaction ->
                    val docRef = db.collection("users")
                        .document(userId)
                        .collection("transactions")
                        .document(transaction.id)
                    batch.set(docRef, transaction)
                }

                batch.commit().await()
            } catch (e: Exception) {
                Timber.e(e, "Error saving transactions to Firestore")
                // Don't throw, just log
            }
        }
    }


    suspend fun deleteTransaction(transactionId: String) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            db.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transactionId)
                .delete()
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error deleting transaction from Firestore")
            throw e
        }
    }

    suspend fun getAllTransactions(): List<Transaction> {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            return db.collection("users")
                .document(userId)
                .collection("transactions")
                .get()
                .await()
                .toObjects(Transaction::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching transactions from Firestore")
            throw e
        }
    }
}