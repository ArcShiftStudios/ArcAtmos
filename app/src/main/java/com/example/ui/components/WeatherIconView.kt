package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.WeatherCategory
import com.example.data.model.WeatherCondition

@Composable
fun WeatherIconView(
    condition: WeatherCondition,
    isDay: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    showBackgroundBadge: Boolean = false
) {
    val (icon, tint, bgColors) = getWeatherIconSpec(condition, isDay)

    if (showBackgroundBadge) {
        Box(
            modifier = modifier
                .size(size * 1.4f)
                .clip(CircleShape)
                .background(Brush.radialGradient(bgColors)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = condition.description,
                tint = tint,
                modifier = Modifier.size(size)
            )
        }
    } else {
        Icon(
            imageVector = icon,
            contentDescription = condition.description,
            tint = tint,
            modifier = modifier.size(size)
        )
    }
}

fun getWeatherIconSpec(condition: WeatherCondition, isDay: Boolean): Triple<ImageVector, Color, List<Color>> {
    return if (!isDay && condition.category == WeatherCategory.CLEAR) {
        Triple(
            Icons.Rounded.NightsStay,
            Color(0xFF90CAF9),
            listOf(Color(0xFF1E3A8A).copy(alpha = 0.5f), Color.Transparent)
        )
    } else {
        when (condition.category) {
            WeatherCategory.CLEAR -> Triple(
                Icons.Rounded.WbSunny,
                Color(0xFFFFB300),
                listOf(Color(0xFFFFB300).copy(alpha = 0.35f), Color.Transparent)
            )
            WeatherCategory.PARTLY_CLOUDY -> Triple(
                Icons.Rounded.CloudQueue,
                if (isDay) Color(0xFFFFCA28) else Color(0xFF90CAF9),
                listOf(Color(0xFF64B5F6).copy(alpha = 0.3f), Color.Transparent)
            )
            WeatherCategory.CLOUDY -> Triple(
                Icons.Rounded.Cloud,
                Color(0xFFB0BEC5),
                listOf(Color(0xFF78909C).copy(alpha = 0.3f), Color.Transparent)
            )
            WeatherCategory.FOG -> Triple(
                Icons.Rounded.Air,
                Color(0xFFCFD8DC),
                listOf(Color(0xFF90A4AE).copy(alpha = 0.3f), Color.Transparent)
            )
            WeatherCategory.DRIZZLE -> Triple(
                Icons.Rounded.Grain,
                Color(0xFF4FC3F7),
                listOf(Color(0xFF0288D1).copy(alpha = 0.3f), Color.Transparent)
            )
            WeatherCategory.RAIN, WeatherCategory.SHOWERS -> Triple(
                Icons.Rounded.WaterDrop,
                Color(0xFF29B6F6),
                listOf(Color(0xFF0277BD).copy(alpha = 0.35f), Color.Transparent)
            )
            WeatherCategory.SNOW -> Triple(
                Icons.Rounded.AcUnit,
                Color(0xFFE0F7FA),
                listOf(Color(0xFF80DEEA).copy(alpha = 0.35f), Color.Transparent)
            )
            WeatherCategory.THUNDERSTORM -> Triple(
                Icons.Rounded.Thunderstorm,
                Color(0xFF7E57C2),
                listOf(Color(0xFF512DA8).copy(alpha = 0.4f), Color.Transparent)
            )
        }
    }
}
