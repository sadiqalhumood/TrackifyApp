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
        val currentMonthTransactions = transactions.filter { transaction ->
            val transactionDate = LocalDate.parse(transaction.date)
            val transactionYearMonth = YearMonth.from(transactionDate)
            transactionYearMonth == currentYearMonth
        }

        val amounts = currentMonthTransactions
            .groupBy {
                StandardCategory.fromDisplayName(
                    it.category?.primary ?: StandardCategory.OTHER.displayName
                )
            }
            .mapValues { (_, transactions) ->
                transactions.sumOf {
                    it.amount.replace(Regex("[^0-9.-]"), "").toDouble().absoluteValue
                }
            }
            .filter { it.value > 0 && it.key != StandardCategory.INCOME }

        val total = amounts.values.sum()
        var startAngle = -90f // Start from top

        amounts.entries.sortedByDescending { it.value }.map { (category, amount) ->
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
    }

    var animationPlayed by remember { mutableStateOf(false) }
    var selectedSegment by remember { mutableStateOf<PieChartData?>(null) }

    // Animation for size
    val animateSize by animateFloatAsState(
        targetValue = if (animationPlayed) radiusOuter.value * 2f else 0f,
        animationSpec = tween(durationMillis = animDuration)
    )

    // Swirl animation
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(key1 = true) {
        animationPlayed = true
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(
                durationMillis = animDuration,
                easing = LinearOutSlowInEasing
            )
        )
    }

    Box(
        modifier = Modifier.size(animateSize.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(radiusOuter * 2f)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val position = awaitPointerEvent().changes.first().position

                            val center = Offset((size.width / 2).toFloat(),
                                (size.height / 2).toFloat()
                            )
                            val angleRad = atan2(
                                position.y - center.y,
                                position.x - center.x
                            )

                            // Convert to degrees and normalize
                            var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
                            angleDeg = (angleDeg + 90 + 360) % 360 // Adjust to start from top and normalize

                            selectedSegment = categoryData.find { data ->
                                val normalizedStart = (data.startAngle + 90 + 360) % 360
                                val normalizedEnd = (normalizedStart + data.sweepAngle) % 360

                                if (normalizedStart <= normalizedEnd) {
                                    angleDeg >= normalizedStart && angleDeg <= normalizedEnd
                                } else {
                                    angleDeg >= normalizedStart || angleDeg <= normalizedEnd
                                }
                            }
                        }
                    }
                }
        ) {
            // Apply rotation animation to the whole pie chart
            rotate(rotation.value) {
                categoryData.forEach { data ->
                    drawArc(
                        color = data.color,
                        startAngle = data.startAngle,
                        sweepAngle = data.sweepAngle,
                        useCenter = false,
                        style = Stroke(chartBarWidth.toPx(), cap = StrokeCap.Round),
                        size = size
                    )
                }
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