package com.example.trackify2

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email and password cannot be empty."
                } else {
                    signInWithEmail(auth, email, password) { success, message ->
                        if (success) {
                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            errorMessage = message
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = { navController.navigate("signup") }
        ) {
            Text("Don't have an account? Sign up")
        }
    }
}

fun signInWithEmail(
    auth: FirebaseAuth,
    email: String,
    password: String,
    onResult: (Boolean, String?) -> Unit
) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, "Login successful!")
            } else {
                onResult(false, task.exception?.localizedMessage ?: "Login failed.")
            }
        }
}

@Composable
fun SignupScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                when {
                    email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                        errorMessage = "All fields are required."
                    }
                    password != confirmPassword -> {
                        errorMessage = "Passwords do not match."
                    }
                    password.length < 6 -> {
                        errorMessage = "Password must be at least 6 characters long."
                    }
                    else -> {
                        createAccountWithEmail(auth, email, password) { success, message ->
                            if (success) {
                                navController.navigate("main") {
                                    popUpTo("signup") { inclusive = true }
                                }
                            } else {
                                errorMessage = message
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate("login") }
        ) {
            Text("Already have an account? Log in")
        }
    }
}

fun createAccountWithEmail(
    auth: FirebaseAuth,
    email: String,
    password: String,
    onResult: (Boolean, String?) -> Unit
) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, "Account created successfully!")
            } else {
                onResult(false, task.exception?.localizedMessage ?: "Sign up failed.")
            }
        }
}

//@Composable
//fun OnboardingScreen(navController: NavController) {
//    var monthlyIncome by remember { mutableStateOf("") }
//    var savingsGoal by remember { mutableStateOf("") }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//    var isLoading by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = "Set Your Financial Goals",
//            style = MaterialTheme.typography.headlineMedium,
//            color = MaterialTheme.colorScheme.primary
//        )
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        Text(
//            text = "Please enter your monthly income and savings goal to help us personalize your experience.",
//            style = MaterialTheme.typography.bodyLarge,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        OutlinedTextField(
//            value = monthlyIncome,
//            onValueChange = {
//                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
//                    monthlyIncome = it
//                }
//            },
//            label = { Text("Monthly Income ($)") },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//            modifier = Modifier.fillMaxWidth(),
//            enabled = !isLoading
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        OutlinedTextField(
//            value = savingsGoal,
//            onValueChange = {
//                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
//                    savingsGoal = it
//                }
//            },
//            label = { Text("Monthly Savings Goal ($)") },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//            modifier = Modifier.fillMaxWidth(),
//            enabled = !isLoading
//        )
//
//        if (errorMessage != null) {
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = errorMessage!!,
//                color = MaterialTheme.colorScheme.error
//            )
//        }
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        Button(
//            onClick = {
//                when {
//                    monthlyIncome.isBlank() || savingsGoal.isBlank() -> {
//                        errorMessage = "Please fill in all fields"
//                    }
//                    monthlyIncome.toLongOrNull() == null || savingsGoal.toLongOrNull() == null -> {
//                        errorMessage = "Please enter valid numbers"
//                    }
//                    savingsGoal.toLong() > monthlyIncome.toLong() -> {
//                        errorMessage = "Savings goal cannot be higher than monthly income"
//                    }
//                    else -> {
//                        isLoading = true
//                        errorMessage = null
//                        val currentUser = FirebaseAuth.getInstance().currentUser
//
//                        if (currentUser == null) {
//                            errorMessage = "No user logged in"
//                            isLoading = false
//                            return@Button
//                        }
//
//                        val userInfo = hashMapOf(
//                            "monthlyIncome" to monthlyIncome.toLong(),
//                            "savingsGoal" to savingsGoal.toLong(),
//                            "userId" to currentUser.uid,
//                            "createdAt" to System.currentTimeMillis()
//                        )
//
//                        FirebaseFirestore.getInstance()
//                            .collection("users")
//                            .document(currentUser.uid)
//                            .set(userInfo)
//                            .addOnSuccessListener {
//                                isLoading = false
//                                navController.navigate("main") {
//                                    popUpTo("onboarding") { inclusive = true }
//                                }
//                            }
//                            .addOnFailureListener { e ->
//                                isLoading = false
//                                errorMessage = e.localizedMessage ?: "Failed to save information"
//                                Timber.e(e, "Error saving financial info")
//                            }
//                    }
//                }
//            },
//            modifier = Modifier.fillMaxWidth(),
//            enabled = !isLoading
//        ) {
//            if (isLoading) {
//                CircularProgressIndicator(
//                    modifier = Modifier.size(24.dp),
//                    color = MaterialTheme.colorScheme.onPrimary
//                )
//            } else {
//                Text("Continue")
//            }
//        }
//
//        if (isLoading) {
//            Spacer(modifier = Modifier.height(16.dp))
//            Text(
//                "Saving your information...",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//    }
//}
//
//private suspend fun saveUserFinancialInfoSuspend(
//    monthlyIncome: Long,
//    savingsGoal: Long
//): Boolean = withContext(Dispatchers.IO) {
//    try {
//        val currentUser = FirebaseAuth.getInstance().currentUser
//            ?: throw Exception("No user logged in")
//
//        Timber.d("Attempting to save financial info for user: ${currentUser.uid}")
//
//        val userInfo = hashMapOf(
//            "monthlyIncome" to monthlyIncome,
//            "savingsGoal" to savingsGoal,
//            "userId" to currentUser.uid,
//            "createdAt" to System.currentTimeMillis()
//        )
//
//        FirebaseFirestore.getInstance()
//            .collection("users")
//            .document(currentUser.uid)
//            .set(userInfo)
//            .await()
//
//        Timber.d("Successfully saved financial info")
//        true
//    } catch (e: Exception) {
//        Timber.e(e, "Error saving financial info")
//        throw e
//    }
//}