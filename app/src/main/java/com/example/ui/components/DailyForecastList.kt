package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TempUnit
import com.example.data.model.DailyForecastItem

@Composable
fun DailyForecastList(
    daily: List<DailyForecastItem>,
    tempUnit: TempUnit,
    modifier: Modifier = Modifier
) {
    if (daily.isEmpty()) return

    val globalMin = daily.minOfOrNull { it.minTempCelsius } ?: 10.0
    val globalMax = daily.maxOfOrNull { it.maxTempCelsius } ?: 35.0
    val rangeSpan = (globalMax - globalMin).coerceAtLeast(1.0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("daily_forecast_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "7-DAY FORECAST",
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            daily.forEachIndexed { index, item ->
                DailyForecastRowItem(
                    item = item,
                    tempUnit = tempUnit,
                    globalMin = globalMin,
                    globalMax = globalMax,
                    rangeSpan = rangeSpan
                )
                if (index < daily.size - 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRowItem(
    item: DailyForecastItem,
    tempUnit: TempUnit,
    globalMin: Double,
    globalMax: Double,
    rangeSpan: Double
) {
    val barPrimaryColor = MaterialTheme.colorScheme.primary
    val barSecondaryColor = MaterialTheme.colorScheme.tertiary
    val barTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day name
        Text(
            text = item.dayName,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (item.dayName == "Today") FontWeight.Bold else FontWeight.Medium
            ),
            color = if (item.dayName == "Today") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(68.dp)
        )

        // Weather icon & rain chance
        Row(
            modifier = Modifier.width(60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            WeatherIconView(
                condition = item.condition,
                isDay = true,
                size = 22.dp
            )
            if (item.precipitationProbabilityMax >= 20) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${item.precipitationProbabilityMax}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Min Temp
        Text(
            text = "${formatTemp(item.minTempCelsius, tempUnit)}°",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(36.dp)
        )

        // Temperature Range Bar
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .padding(horizontal = 8.dp)
        ) {
            val width = size.width
            val height = size.height

            // Background track
            drawRoundRect(
                color = barTrackColor,
                size = Size(width, height),
                cornerRadius = CornerRadius(height / 2, height / 2)
            )

            // Normalized range
            val startRatio = ((item.minTempCelsius - globalMin) / rangeSpan).coerceIn(0.0, 1.0).toFloat()
            val endRatio = ((item.maxTempCelsius - globalMin) / rangeSpan).coerceIn(0.0, 1.0).toFloat()

            val startX = startRatio * width
            val endX = (endRatio * width).coerceAtLeast(startX + height)

            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(barSecondaryColor, barPrimaryColor),
                    startX = startX,
                    endX = endX
                ),
                topLeft = Offset(startX, 0f),
                size = Size(endX - startX, height),
                cornerRadius = CornerRadius(height / 2, height / 2)
            )
        }

        // Max Temp
        Text(
            text = "${formatTemp(item.maxTempCelsius, tempUnit)}°",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(36.dp)
        )
    }
}
