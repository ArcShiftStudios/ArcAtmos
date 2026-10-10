package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TempUnit
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecastItem
import com.example.ui.theme.FredokaFamily
import kotlin.math.roundToInt

@Composable
fun LargeTemperatureText(
    current: CurrentWeather,
    todayDaily: DailyForecastItem?,
    tempUnit: TempUnit,
    modifier: Modifier = Modifier
) {
    val displayTemp = formatTemp(current.temperatureCelsius, tempUnit)
    val displayFeelsLike = formatTemp(current.feelsLikeCelsius, tempUnit)
    val maxTemp = todayDaily?.let { formatTemp(it.maxTempCelsius, tempUnit) } ?: displayTemp
    val minTemp = todayDaily?.let { formatTemp(it.minTempCelsius, tempUnit) } ?: displayTemp

    // Warm cream / off-white text color matching screenshot
    val creamyTextColor = Color(0xFFF5EBE0)
    // Mango / amber yellow-orange temperature color matching screenshot
    val mangoTempColor = Color(0xFFFFBA54)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Weather Condition Row: (Icon + Condition Name)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.testTag("condition_header_row")
        ) {
            WeatherIconView(
                condition = current.condition,
                isDay = current.isDay,
                size = 30.dp,
                showBackgroundBadge = false,
                modifier = Modifier.padding(end = 8.dp)
            )

            Text(
                text = current.condition.description,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FredokaFamily,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                ),
                color = creamyTextColor,
                modifier = Modifier.testTag("condition_text")
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Huge Hero Temperature: "27°" in chunky Fredoka
        AnimatedContent(
            targetState = displayTemp,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "HeroTempAnim"
        ) { temp ->
            Text(
                text = "$temp°",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FredokaFamily,
                    fontSize = 118.sp,
                    lineHeight = 118.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-2.5).sp
                ),
                color = mangoTempColor,
                modifier = Modifier.testTag("hero_temperature_text")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Feels like 28°
        Text(
            text = "Feels like $displayFeelsLike°",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FredokaFamily,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
            ),
            color = creamyTextColor,
            modifier = Modifier.testTag("feels_like_text")
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Max: 30° Min: 21°
        Text(
            text = "Max: $maxTemp°  Min: $minTemp°",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FredokaFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
            ),
            color = creamyTextColor,
            modifier = Modifier.testTag("high_low_text")
        )
    }
}

fun formatTemp(celsius: Double, unit: TempUnit): String {
    val temp = if (unit == TempUnit.FAHRENHEIT) {
        celsius * 9.0 / 5.0 + 32.0
    } else {
        celsius
    }
    return temp.roundToInt().toString()
}
