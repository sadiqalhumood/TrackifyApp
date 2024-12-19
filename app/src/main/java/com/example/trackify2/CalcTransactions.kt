package com.example.trackify2

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.app.Application
import android.util.Base64
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import com.google.type.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import java.time.temporal.TemporalAdjusters

data class TransactionTotals(
    val totalIncome: Double = 0.0,
    val totalSpending: Double = 0.0,
    val totalSavings: Double = 0.0
)

class TransactionCalculator {
    fun calculateTotals(transactions: List<Transaction>): TransactionTotals {
        var income = 0.0
        var spending = 0.0

        transactions.forEach { transaction ->
            val amount = transaction.amount.toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                income += amount
            } else {
                spending += -amount
            }
        }

        val savings = income - spending

        return TransactionTotals(
            totalIncome = income,
            totalSpending = spending,
            totalSavings = savings
        )
    }
}



enum class ReportPeriod {
    WEEKLY,
    MONTHLY
}

data class PeriodTotals(
    val period: String,
    val income: Double,
    val spending: Double,
    val savings: Double
)

@Composable
fun ReportScreen(
    viewModel: TransactionViewModel = viewModel()
) {
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.MONTHLY) }
    val totals by viewModel.transactionTotals.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Financial Report",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Period Selection Tabs
        TabRow(
            selectedTabIndex = if (selectedPeriod == ReportPeriod.MONTHLY) 0 else 1,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Tab(
                selected = selectedPeriod == ReportPeriod.MONTHLY,
                onClick = { selectedPeriod = ReportPeriod.MONTHLY },
                text = { Text("Monthly") }
            )
            Tab(
                selected = selectedPeriod == ReportPeriod.WEEKLY,
                onClick = { selectedPeriod = ReportPeriod.WEEKLY },
                text = { Text("Weekly") }
            )
        }

        // Overall Summary Card
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
                    text = "Overall Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TotalItem(
                    title = "Total Income",
                    amount = totals.totalIncome,
                    color = Color(0xFF4CAF50)
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                TotalItem(
                    title = "Total Spending",
                    amount = totals.totalSpending,
                    color = Color(0xFFE57373)
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                TotalItem(
                    title = "Total Savings",
                    amount = totals.totalSavings,
                    color = if (totals.totalSavings >= 0) Color(0xFF4CAF50) else Color(0xFFE57373)
                )
            }
        }

        // Period Details
        val periodTotals = calculatePeriodTotals(transactions, selectedPeriod)

        Text(
            text = "${if (selectedPeriod == ReportPeriod.MONTHLY) "Monthly" else "Weekly"} Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(periodTotals) { periodTotal ->
                PeriodCard(periodTotal)
            }
        }
    }
}

@Composable
private fun PeriodCard(periodTotal: PeriodTotals) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = periodTotal.period,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TotalItem(
                title = "Income",
                amount = periodTotal.income,
                color = Color(0xFF4CAF50)
            )
            TotalItem(
                title = "Spending",
                amount = periodTotal.spending,
                color = Color(0xFFE57373)
            )
            TotalItem(
                title = "Savings",
                amount = periodTotal.savings,
                color = if (periodTotal.savings >= 0) Color(0xFF4CAF50) else Color(0xFFE57373)
            )
        }
    }
}

private fun calculatePeriodTotals(
    transactions: List<Transaction>,
    period: ReportPeriod
): List<PeriodTotals> {
    val periodMap = mutableMapOf<String, MutableList<Transaction>>()

    transactions.forEach { transaction ->
        val date = LocalDate.parse(transaction.date)
        val periodKey = when (period) {
            ReportPeriod.MONTHLY -> "${date.month} ${date.year}"
            ReportPeriod.WEEKLY -> {
                val start = date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                val end = date.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))

                val formatter = DateTimeFormatter.ofPattern("MMM d")
                val yearFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
                if (start.year != end.year) {
                    "${start.format(yearFormatter)} - ${end.format(yearFormatter)}"
                } else if (start.month != end.month) {
                    "${start.format(formatter)} - ${end.format(yearFormatter)}"
                } else {
                    "${start.format(formatter)} - ${end.format(DateTimeFormatter.ofPattern("d, yyyy"))}"
                }
            }
        }

        periodMap.getOrPut(periodKey) { mutableListOf() }.add(transaction)
    }

    return periodMap.map { (periodKey, periodTransactions) ->
        var income = 0.0
        var spending = 0.0

        periodTransactions.forEach { transaction ->
            val amount = transaction.amount.toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                income += amount
            } else {
                spending += -amount
            }
        }

        PeriodTotals(
            period = periodKey,
            income = income,
            spending = spending,
            savings = income - spending
        )
    }.sortedByDescending {
        when (period) {
            ReportPeriod.MONTHLY -> {
                val parts = it.period.split(" ")
                val month = java.time.Month.valueOf(parts[0].uppercase())
                val year = parts[1].toInt()
                LocalDate.of(year, month, 1)
            }
            ReportPeriod.WEEKLY -> {
                val startDate = it.period.split(" - ")[0]
                val formatter = DateTimeFormatter.ofPattern("MMM d[, yyyy]", Locale.ENGLISH)
                try {
                    LocalDate.parse(startDate.trim(), formatter)
                } catch (e: Exception) {
                    LocalDate.now()
                }
            }
        }
    }
}

@Composable
private fun TotalItem(
    title: String,
    amount: Double,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = String.format("$%.2f", amount),
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}