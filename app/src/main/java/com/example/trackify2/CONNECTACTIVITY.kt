package com.example.trackify2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.google.android.material.snackbar.Snackbar
import io.teller.connect.sdk.*
import timber.log.Timber
import java.io.Serializable

class ConnectActivity : FragmentActivity(), ConnectListener {

    companion object {
        val config = Config(
            appId = "app_p71icd52t0ie9bj0q6000",
            environment = Environment.SANDBOX,
            selectAccount = SelectAccount.SINGLE,
            products = listOf(Product.IDENTITY, Product.TRANSACTIONS, Product.BALANCE),
            debug = true
        )
        const val EXTRA_CONFIG = "extra_config"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Always use Companion.config
        val config = Companion.config
        val args = ConnectFragment.buildArgs(config)

        // Create the ConnectFragment instance and set its arguments
        val connectFragment = ConnectFragment().apply {
            arguments = args
        }

        // Add the fragment to your activity’s root view (android.R.id.content is a convenient container)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add(android.R.id.content, connectFragment, "TellerConnectFragment")
        }
    }


    override fun onInit() {
        Timber.d("Initialized Teller Connect")
    }

    override fun onExit() {
        Timber.d("User exited Teller Connect")
        finish()
    }

    override fun onSuccess(registration: Registration) {
        Timber.d("onSuccess triggered")
        val accessToken = registration.accessToken
        if (accessToken != null) {
            Timber.i("Access Token: $accessToken")
            val sharedPrefs = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
            sharedPrefs.edit().putString("ACCESS_TOKEN", accessToken).apply()
            finish()
        } else {
            Timber.e("Access Token is null")
            Toast.makeText(this, "Failed to get access token", Toast.LENGTH_LONG).show()
        }
    }



    override fun onSuccess(payment: Payment) {
        Timber.i("Payment Success! ID: ${payment.id}")
        finish()
    }

    override fun onSuccess(payee: Payee) {
        Timber.i("Payee Success! ID: ${payee.id}")
        finish()
    }

    override fun onFailure(error: Error) {
        Timber.e("Error: ${error.message}")
        Toast.makeText(this, "Error: ${error.message ?: "Unknown Error"}", Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onEvent(name: String, data: Map<String, Any>) {
        Timber.d("Event: $name, Data: $data")
    }
}
