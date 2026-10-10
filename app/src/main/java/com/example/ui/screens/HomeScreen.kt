package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppSettings
import com.example.data.model.WeatherSnapshot
import com.example.ui.atmosphere.WeatherAtmosphere
import com.example.ui.components.AirQualityCard
import com.example.ui.components.DailyForecastList
import com.example.ui.components.HourlyForecastRow
import com.example.ui.components.HumidityCard
import com.example.ui.components.LargeTemperatureText
import com.example.ui.components.PressureCard
import com.example.ui.components.SunCard
import com.example.ui.components.UvIndexCard
import com.example.ui.components.VisibilityCard
import com.example.ui.components.WeatherInsightCard
import com.example.ui.components.WindCard
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    snapshot: WeatherSnapshot,
    settings: AppSettings,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLocationTitleClick: () -> Unit,
    onNavigateToDetails: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val current = snapshot.current
    val todayDaily = snapshot.daily.firstOrNull()

    val pullRefreshState = rememberPullToRefreshState()

    WeatherAtmosphere(
        condition = current.condition,
        isDay = current.isDay,
        reduceEffects = settings.reduceWeatherEffects
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullRefreshState,
            modifier = modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 96.dp)
            ) {
                // Top App Bar Header
                HomeTopBar(
                    cityName = snapshot.location.name,
                    countryName = snapshot.location.country,
                    isCached = snapshot.isCached,
                    lastUpdatedMillis = snapshot.lastUpdatedMillis,
                    onLocationClick = onLocationTitleClick,
                    onRefreshClick = onRefresh,
                    onSettingsClick = onNavigateToSettings,
                    isRefreshing = isRefreshing
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Hero Temperature & Condition
                LargeTemperatureText(
                    current = current,
                    todayDaily = todayDaily,
                    tempUnit = settings.tempUnit
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Hourly Forecast Ribbon
                HourlyForecastRow(
                    hourly = snapshot.hourly,
                    tempUnit = settings.tempUnit
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Weather Insight Card
                WeatherInsightCard(insight = snapshot.insight)

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Modular Information 2-Column Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    UvIndexCard(
                        uvIndex = current.uvIndex,
                        modifier = Modifier.weight(1f)
                    )
                    HumidityCard(
                        humidityPercent = current.humidityPercent,
                        dewPointCelsius = current.dewPointCelsius,
                        tempUnit = settings.tempUnit,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WindCard(
                        windSpeedKmh = current.windSpeedKmh,
                        windDirectionDegrees = current.windDirectionDegrees,
                        windGustsKmh = current.windGustsKmh,
                        windUnit = settings.windUnit,
                        modifier = Modifier.weight(1f)
                    )
                    PressureCard(
                        pressureHpa = current.pressureHpa,
                        pressureUnit = settings.pressureUnit,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Air Quality Highlight Card
                AirQualityCard(
                    airQuality = snapshot.airQuality,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 7-Day Daily Forecast
                DailyForecastList(
                    daily = snapshot.daily,
                    tempUnit = settings.tempUnit
                )
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    cityName: String,
    countryName: String,
    isCached: Boolean,
    lastUpdatedMillis: Long,
    onLocationClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isRefreshing: Boolean
) {
    val minutesAgo = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastUpdatedMillis)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 14.dp, start = 20.dp, end = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Location selector pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onLocationClick)
                .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f))
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("location_header_pill")
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cityName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Switch location",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (isCached) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CloudOff,
                            contentDescription = "Cached",
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Updated ${minutesAgo.coerceAtLeast(1)} min ago",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // Actions
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onRefreshClick,
                modifier = Modifier.testTag("refresh_weather_button")
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh weather",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
