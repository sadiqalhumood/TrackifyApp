package com.example.trackify2

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

class ScoringService {
    private val db = FirebaseFirestore.getInstance()
    private val scoresCollection = db.collection("userScores")
    private val auth = FirebaseAuth.getInstance()

    suspend fun calculateAndUpdateAllMonthlyScores(transactions: List<Transaction>) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Timber.d("No user logged in, skipping score calculation")
            return
        }

        try {
            Timber.d("Starting score calculation for user ${currentUser.uid}")
            Timber.d("Processing ${transactions.size} transactions")

            val monthlyTransactions = transactions.groupBy {
                LocalDate.parse(it.date).run {
                    YearMonth.of(year, month)
                }
            }.toSortedMap(reverseOrder())

            var totalScore = 0

            monthlyTransactions.forEach { (yearMonth, monthTransactions) ->
                var monthlyIncome = 0.0
                var monthlySavings = 0.0

                monthTransactions.forEach { transaction ->
                    val amount = transaction.amount.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        monthlyIncome += amount
                    } else {
                        monthlySavings -= amount
                    }
                }

                val savingsPercentage = if (monthlyIncome > 0) {
                    ((monthlySavings / monthlyIncome) * 100).toInt()
                } else 0

                Timber.d("Month: $yearMonth, Income: $monthlyIncome, Savings: $monthlySavings, Score: $savingsPercentage")
                totalScore += savingsPercentage
            }

            Timber.d("Total score calculated: $totalScore")

            // Create test score if no transactions exist
            if (transactions.isEmpty()) {
                totalScore = 100 // Example score
                Timber.d("No transactions found, creating test score: $totalScore")
            }

            // Update user's score
            val userScore = UserScore(
                userId = currentUser.uid,
                displayName = currentUser.displayName ?: "Anonymous",
                totalScore = totalScore,
                lastUpdated = LocalDate.now().toString(),
                monthlyScores = monthlyTransactions.map { (yearMonth, transactions) ->
                    MonthlyScore(
                        yearMonth = yearMonth.toString(),
                        score = calculateMonthScore(transactions)
                    )
                }
            )

            Timber.d("Updating score in Firestore: $userScore")
            scoresCollection.document(currentUser.uid).set(userScore).await()
            Timber.d("Score update completed successfully")

        } catch (e: Exception) {
            Timber.e(e, "Error calculating scores")
            throw e
        }
    }

    private fun calculateMonthScore(transactions: List<Transaction>): Int {
        var monthlyIncome = 0.0
        var monthlySavings = 0.0

        transactions.forEach { transaction ->
            val amount = transaction.amount.toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                monthlyIncome += amount
            } else {
                monthlySavings -= amount
            }
        }

        return if (monthlyIncome > 0) {
            ((monthlySavings / monthlyIncome) * 100).toInt()
        } else 0
    }

    private suspend fun updateUserScore(
        userId: String,
        totalScore: Int,
        displayName: String,
        monthlyScores: List<MonthlyScore>
    ) {
        try {
            val userScoreRef = scoresCollection.document(userId)
            db.runTransaction { transaction ->
                val updatedScore = UserScore(
                    userId = userId,
                    displayName = displayName,
                    totalScore = totalScore,
                    lastUpdated = LocalDate.now().toString(),
                    monthlyScores = monthlyScores
                )
                transaction.set(userScoreRef, updatedScore)
            }.await()
        } catch (e: Exception) {
            Timber.e(e, "Error updating score")
        }
    }

    suspend fun getTopScores(limit: Int = 10): List<UserScore> {
        return try {
            scoresCollection
                .orderBy("totalScore", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
                .toObjects(UserScore::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching leaderboard")
            emptyList()
        }
    }

    suspend fun getUserRank(userId: String): Int? {
        return try {
            val userScore = scoresCollection.document(userId).get().await()
                .toObject(UserScore::class.java) ?: return null

            val higherScores = scoresCollection
                .whereGreaterThan("totalScore", userScore.totalScore)
                .get()
                .await()
                .size()

            higherScores + 1
        } catch (e: Exception) {
            Timber.e(e, "Error fetching user rank")
            null
        }
    }
}

// Update UserScore data class
data class UserScore(
    val userId: String = "",
    val displayName: String = "",
    val totalScore: Int = 0,
    val lastUpdated: String = "",
    val monthlyScores: List<MonthlyScore> = emptyList()
)

data class MonthlyScore(
    val yearMonth: String = "",
    val score: Int = 0
)


class LeaderboardViewModel : ViewModel() {
    private val scoringService = ScoringService()
    private val scope = viewModelScope

    private val _leaderboardState = MutableStateFlow<LeaderboardState>(LeaderboardState.Loading)
    val leaderboardState: StateFlow<LeaderboardState> = _leaderboardState

    init {
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        scope.launch {
            try {
                _leaderboardState.value = LeaderboardState.Loading

                val scores = withContext(Dispatchers.IO) {
                    scoringService.getTopScores()
                }

                val currentUser = FirebaseAuth.getInstance().currentUser
                val userRank = if (currentUser != null) {
                    withContext(Dispatchers.IO) {
                        scoringService.getUserRank(currentUser.uid)
                    }
                } else null

                _leaderboardState.value = LeaderboardState.Success(
                    scores = scores,
                    userRank = userRank
                )
            } catch (e: Exception) {
                Timber.e(e, "Error loading leaderboard")
                _leaderboardState.value = LeaderboardState.Error(
                    message = "Failed to load leaderboard. Please try again."
                )
            }
        }
    }

    fun retry() {
        loadLeaderboard()
    }
}

sealed class LeaderboardState {
    object Loading : LeaderboardState()
    data class Success(
        val scores: List<UserScore>,
        val userRank: Int?
    ) : LeaderboardState()
    data class Error(val message: String) : LeaderboardState()
}

@Composable
private fun LeaderboardItem(
    rank: Int,
    score: UserScore,
    isCurrentUser: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank circle
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = MaterialTheme.shapes.small,
                    color = if (rank <= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "#$rank",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (rank <= 3)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column {
                    Text(
                        text = score.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (score.monthlyScores.isNotEmpty()) {
                        Text(
                            text = "Last updated: ${score.lastUpdated}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = "${score.totalScore} pts",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel()
) {
    val state by viewModel.leaderboardState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Savings Leaderboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = {
                    transactionViewModel.calculateScores(
                        transactionViewModel.allTransactions.value
                    )
                    viewModel.retry()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Leaderboard"
                )
            }
        }

        when (val currentState = state) {
            is LeaderboardState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is LeaderboardState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = currentState.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.retry() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }

            is LeaderboardState.Success -> {
                // User's rank card
                currentState.userRank?.let { rank ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Your Ranking",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "#$rank",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (currentState.scores.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No scores yet!",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(currentState.scores) { index, score ->
                            LeaderboardItem(
                                rank = index + 1,
                                score = score,
                                isCurrentUser = score.userId == FirebaseAuth.getInstance().currentUser?.uid
                            )
                        }
                    }
                }
            }
        }
    }
}