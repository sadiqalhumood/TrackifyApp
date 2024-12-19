package com.example.trackify2

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class TokenManager(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val sharedPrefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)

    suspend fun getAccessToken(): String? {
        // First try to get from SharedPreferences
        var accessToken = sharedPrefs.getString("ACCESS_TOKEN", null)

        // If not in SharedPreferences, try to get from Firestore
        if (accessToken == null) {
            auth.currentUser?.uid?.let { userId ->
                try {
                    val document = db.collection("users")
                        .document(userId)
                        .get()
                        .await()

                    accessToken = document.getString("accessToken")

                    // Save to SharedPreferences for future use
                    accessToken?.let {
                        sharedPrefs.edit().putString("ACCESS_TOKEN", it).apply()
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error fetching access token from Firestore")
                }
            }
        }

        return accessToken
    }

    fun clearAccessToken() {
        sharedPrefs.edit().remove("ACCESS_TOKEN").apply()
    }
}