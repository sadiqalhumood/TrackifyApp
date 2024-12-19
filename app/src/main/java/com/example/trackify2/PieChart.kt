package com.example.trackify2

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.absoluteValue
import kotlin.math.atan2
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput

private val categoryColors = mapOf(
    StandardCategory.FOOD_AND_DRINK to Color(0xFFFF9800),
    StandardCategory.SHOPPING to Color(0xFF2196F3),
    StandardCategory.TRANSPORTATION to Color(0xFF4CAF50),
    StandardCategory.BILLS_AND_UTILITIES to Color(0xFFE91E63),
    StandardCategory.ENTERTAINMENT to Color(0xFF9C27B0),
    StandardCategory.HEALTH to Color(0xFF00BCD4),
    StandardCategory.TRANSFER to Color(0xFF795548),
    StandardCategory.OTHER to Color(0xFF607D8B),
    StandardCategory.INCOME to Color(0xFF009688)
)

data class PieChartData(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val startAngle: Float,
    val sweepAngle: Float,
    val color: Color
)

@Composable
fun TransactionPieChart(
    transactions: List<Transaction>,
    radiusOuter: Dp = 120.dp,
    chartBarWidth: Dp = 30.dp,
    animDuration: Int = 1000
) {
    val currentYearMonth = YearMonth.now()

    val categoryData = remember(transactions) {
        try {
            val currentMonthTransactions = transactions.filter { transaction ->
                try {
                    val transactionDate = LocalDate.parse(transaction.date)
                    val transactionYearMonth = YearMonth.from(transactionDate)
                    transactionYearMonth == currentYearMonth
                } catch (e: Exception) {
                    false
                }
            }

            val amounts = mutableMapOf<StandardCategory, Double>()

            // Safely calculate amounts for each category
            currentMonthTransactions.forEach { transaction ->
                try {
                    val category = StandardCategory.fromDisplayName(
                        transaction.category?.primary ?: StandardCategory.OTHER.displayName
                    )
                    val amount = transaction.amount.replace(Regex("[^0-9.-]"), "")
                        .toDoubleOrNull()?.absoluteValue ?: 0.0

                    if (amount > 0 && category != StandardCategory.INCOME) {
                        amounts[category] = (amounts[category] ?: 0.0) + amount
                    }
                } catch (e: Exception) {
                    // Skip invalid transactions
                }
            }

            val total = amounts.values.sum()
            if (total <= 0) return@remember emptyList()

            var startAngle = 0f
            amounts.entries.sortedByDescending { it.value }
                .map { (category, amount) ->
                    val percentage = (amount / total * 100).toFloat()
                    val sweepAngle = 360f * percentage / 100f
                    PieChartData(
                        category = category.displayName,
                        amount = amount,
                        percentage = percentage,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        color = categoryColors[category] ?: Color.Gray
                    ).also {
                        startAngle += sweepAngle
                    }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    var selectedSegment by remember { mutableStateOf<PieChartData?>(null) }

    Box(
        modifier = Modifier.size(radiusOuter * 2f),
        contentAlignment = Alignment.Center
    ) {
        if (categoryData.isEmpty()) {
            Text(
                text = "No spending data\nfor this month",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        } else {
            Canvas(
                modifier = Modifier
                    .size(radiusOuter * 2f)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val position = awaitPointerEvent().changes.first().position
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val touchVector = position - center

                                try {
                                    var angle = Math.toDegrees(
                                        atan2(touchVector.y, touchVector.x).toDouble()
                                    ).toFloat()
                                    angle = (angle + 360) % 360

                                    selectedSegment = categoryData.firstOrNull { data ->
                                        val start = data.startAngle
                                        val end = (data.startAngle + data.sweepAngle) % 360
                                        if (start <= end) {
                                            angle >= start && angle <= end
                                        } else {
                                            angle >= start || angle <= end
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Ignore touch events that cause errors
                                }
                            }
                        }
                    }
            ) {
                categoryData.forEach { data ->
                    drawArc(
                        color = data.color,
                        startAngle = data.startAngle,
                        sweepAngle = data.sweepAngle,
                        useCenter = false,
                        style = Stroke(
                            width = chartBarWidth.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }
            }

            selectedSegment?.let { data ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = data.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "$${String.format("%,.2f", data.amount)}",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${String.format("%.1f", data.percentage)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } ?: Text(
                text = "Tap a segment\nto see details",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}