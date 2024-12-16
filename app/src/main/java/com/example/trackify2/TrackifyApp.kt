package com.example.trackify2

import android.app.Application
import com.google.firebase.BuildConfig
import timber.log.Timber

class TrackifyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for better logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        verifySslSetup()
    }

    private fun verifySslSetup() {
        try {
            // Verify SSL setup by creating the context
            val keystoreManager = KeystoreManager(this)
            keystoreManager.createSSLContext()
            Timber.i("Teller SSL context successfully created")
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to create Teller SSL context: ${e.message}")
            // You might want to show some UI feedback to the user
            // or implement a retry mechanism
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during SSL setup: ${e.message}")
            e.printStackTrace()
        }
    }
}