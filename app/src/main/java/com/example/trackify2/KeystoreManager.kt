package com.example.trackify2

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import android.util.Base64
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.security.KeyFactory
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

class KeystoreManager(private val context: Context) {
    companion object {
        private const val KEY_ALIAS = "teller_client_cert"
    }

    private fun readPemFile(resourceId: Int): String {
        return context.resources.openRawResource(resourceId).use { inputStream ->
            inputStream.bufferedReader().readText()
        }.trim()
    }

    fun createSSLContext(): SSLContext {
        try {
            // Read PEM files
            val privateKeyPem = readPemFile(R.raw.private_key)
            val certificatePem = readPemFile(R.raw.certificate)

            // Parse private key
            val privateKeyContent = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s+".toRegex(), "")
                .trim()

            val keyBytes = Base64.decode(privateKeyContent, Base64.NO_WRAP)
            val keySpec = PKCS8EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            val privateKey = keyFactory.generatePrivate(keySpec)

            // Parse certificate
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certificateStream = ByteArrayInputStream(certificatePem.toByteArray())
            val certificate = certificateFactory.generateCertificate(certificateStream)

            // Create KeyStore and initialize it
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            keyStore.setKeyEntry(
                KEY_ALIAS,
                privateKey,
                null,
                arrayOf(certificate)
            )

            // Initialize the SSL context
            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(keyStore, null)

            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)

            return SSLContext.getInstance("TLS").apply {
                init(
                    keyManagerFactory.keyManagers,
                    trustManagerFactory.trustManagers,
                    null
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to create SSL context: ${e.message}")
            throw SecurityException("Failed to create SSL context", e)
        }
    }
}
