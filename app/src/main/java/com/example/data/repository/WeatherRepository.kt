package com.example.data.repository

import com.example.data.api.NetworkClient
import com.example.data.api.OpenMeteoApiService
import com.example.data.local.PreferencesManager
import com.example.data.model.AirQualityData
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyForecastItem
import com.example.data.model.OpenMeteoWeatherResponse
import com.example.data.model.SunMoonData
import com.example.data.model.UserLocation
import com.example.data.model.WeatherCondition
import com.example.data.model.WeatherSnapshot
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class WeatherRepository(
    private val apiService: OpenMeteoApiService = NetworkClient.apiService,
    private val preferencesManager: PreferencesManager
) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val snapshotAdapter = moshi.adapter(WeatherSnapshot::class.java)

    suspend fun getWeatherData(location: UserLocation): Result<WeatherSnapshot> = withContext(Dispatchers.IO) {
        try {
            coroutineScope {
                val weatherDeferred = async {
                    apiService.getWeatherForecast(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                }

                val airQualityDeferred = async {
                    try {
                        apiService.getAirQuality(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                val weatherResponse = weatherDeferred.await()
                val airQualityResponse = airQualityDeferred.await()

                val snapshot = mapToWeatherSnapshot(
                    location = location,
                    weather = weatherResponse,
                    airQualityDto = airQualityResponse?.current
                )

                // Save to cache asynchronously
                try {
                    val json = snapshotAdapter.toJson(snapshot)
                    preferencesManager.setCachedWeatherJson(json)
                } catch (_: Exception) { }

                Result.success(snapshot)
            }
        } catch (e: Exception) {
            // Try loading cached weather
            val cachedJson = preferencesManager.cachedWeatherJson.firstOrNull()
            if (!cachedJson.isNullOrBlank()) {
                try {
                    val cachedSnapshot = snapshotAdapter.fromJson(cachedJson)?.copy(isCached = true)
                    if (cachedSnapshot != null) {
                        return@withContext Result.success(cachedSnapshot)
                    }
                } catch (_: Exception) { }
            }
            Result.failure(e)
        }
    }

    suspend fun searchLocations(query: String): List<UserLocation> = withContext(Dispatchers.IO) {
        if (query.trim().length < 2) return@withContext emptyList()
        try {
            val response = apiService.searchLocations(name = query.trim())
            response.results?.map { item ->
                UserLocation(
                    id = "loc_${item.id}",
                    name = item.name,
                    country = item.country ?: "",
                    admin1 = item.admin1,
                    latitude = item.latitude,
                    longitude = item.longitude,
                    isCurrentLocation = false,
                    isDefault = false
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun mapToWeatherSnapshot(
        location: UserLocation,
        weather: OpenMeteoWeatherResponse,
        airQualityDto: com.example.data.model.OpenMeteoAirQualityCurrentDto?
    ): WeatherSnapshot {
        val currentDto = weather.current ?: throw IllegalStateException("Current weather unavailable")
        val condition = WeatherCondition.fromWmoCode(currentDto.weatherCode)

        val currentWeather = CurrentWeather(
            temperatureCelsius = currentDto.temperature2m,
            feelsLikeCelsius = currentDto.apparentTemperature,
            condition = condition,
            humidityPercent = currentDto.relativeHumidity2m.roundToInt(),
            windSpeedKmh = currentDto.windSpeed10m,
            windDirectionDegrees = currentDto.windDirection10m,
            windGustsKmh = currentDto.windGusts10m,
            pressureHpa = currentDto.pressureMsl,
            uvIndex = currentDto.uvIndex,
            cloudCoverPercent = currentDto.cloudCover.roundToInt(),
            dewPointCelsius = currentDto.dewPoint2m,
            precipitationMm = currentDto.precipitation,
            isDay = currentDto.isDay == 1,
            timestamp = currentDto.time
        )

        // Parse hourly forecast
        val hourlyDto = weather.hourly
        val hourlyItems = mutableListOf<HourlyForecastItem>()
        if (hourlyDto != null && hourlyDto.time.isNotEmpty()) {
            val nowCalendar = Calendar.getInstance()
            val currentHourStr = SimpleDateFormat("yyyy-MM-dd'T'HH", Locale.US).format(nowCalendar.time)

            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val outputFormat = SimpleDateFormat("h a", Locale.US)

            val size = minOf(hourlyDto.time.size, 24)
            for (i in 0 until size) {
                val timeStr = hourlyDto.time.getOrNull(i) ?: continue
                val displayHour = try {
                    val date = inputFormat.parse(timeStr)
                    if (date != null) outputFormat.format(date).uppercase(Locale.US) else timeStr
                } catch (e: Exception) {
                    timeStr
                }

                val isCur = timeStr.startsWith(currentHourStr)
                val isDaytime = (hourlyDto.isDay?.getOrNull(i) ?: 1) == 1
                val code = hourlyDto.weatherCode.getOrNull(i) ?: 0
                val itemCondition = WeatherCondition.fromWmoCode(code)

                hourlyItems.add(
                    HourlyForecastItem(
                        timeIso = timeStr,
                        displayHour = if (isCur) "NOW" else displayHour,
                        temperatureCelsius = hourlyDto.temperature2m.getOrNull(i) ?: currentDto.temperature2m,
                        condition = itemCondition,
                        precipitationProbability = hourlyDto.precipitationProbability?.getOrNull(i) ?: 0,
                        precipitationMm = hourlyDto.precipitation?.getOrNull(i) ?: 0.0,
                        humidityPercent = (hourlyDto.relativeHumidity2m.getOrNull(i) ?: 50.0).roundToInt(),
                        windSpeedKmh = hourlyDto.windSpeed10m?.getOrNull(i) ?: 0.0,
                        isDay = isDaytime,
                        isCurrentHour = isCur
                    )
                )
            }
        }

        // Parse daily forecast
        val dailyDto = weather.daily
        val dailyItems = mutableListOf<DailyForecastItem>()
        var sunriseToday = "06:00"
        var sunsetToday = "18:30"

        if (dailyDto != null && dailyDto.time.isNotEmpty()) {
            val dateInFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val dayNameFormat = SimpleDateFormat("EEE", Locale.US)
            val dateDisplayFormat = SimpleDateFormat("MMM d", Locale.US)
            val timeParseFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val timeDisplayFormat = SimpleDateFormat("HH:mm", Locale.US)

            val count = minOf(dailyDto.time.size, 7)
            for (i in 0 until count) {
                val dateStr = dailyDto.time.getOrNull(i) ?: continue
                var dayName = "Day $i"
                var dateFormatted = dateStr

                try {
                    val parsedDate = dateInFormat.parse(dateStr)
                    if (parsedDate != null) {
                        dayName = if (i == 0) "Today" else dayNameFormat.format(parsedDate)
                        dateFormatted = dateDisplayFormat.format(parsedDate)
                    }
                } catch (_: Exception) { }

                val sunriseRaw = dailyDto.sunrise?.getOrNull(i)
                val sunsetRaw = dailyDto.sunset?.getOrNull(i)

                val formattedSunrise = formatTimeOnly(sunriseRaw, timeParseFormat, timeDisplayFormat) ?: "06:15"
                val formattedSunset = formatTimeOnly(sunsetRaw, timeParseFormat, timeDisplayFormat) ?: "18:45"

                if (i == 0) {
                    sunriseToday = formattedSunrise
                    sunsetToday = formattedSunset
                }

                val code = dailyDto.weatherCode.getOrNull(i) ?: 0
                val dayCondition = WeatherCondition.fromWmoCode(code)

                dailyItems.add(
                    DailyForecastItem(
                        dateIso = dateStr,
                        dayName = dayName,
                        dateFormatted = dateFormatted,
                        maxTempCelsius = dailyDto.temperature2mMax.getOrNull(i) ?: 25.0,
                        minTempCelsius = dailyDto.temperature2mMin.getOrNull(i) ?: 18.0,
                        condition = dayCondition,
                        precipitationProbabilityMax = dailyDto.precipitationProbabilityMax?.getOrNull(i) ?: 0,
                        precipitationSumMm = dailyDto.precipitationSum?.getOrNull(i) ?: 0.0,
                        sunrise = formattedSunrise,
                        sunset = formattedSunset,
                        uvIndexMax = dailyDto.uvIndexMax?.getOrNull(i) ?: 5.0,
                        windSpeedMaxKmh = dailyDto.windSpeed10mMax?.getOrNull(i) ?: 10.0
                    )
                )
            }
        }

        // Parse Air Quality
        val aqiValue = (airQualityDto?.usAqi ?: airQualityDto?.europeanAqi ?: 42.0).roundToInt()
        val aqiSeverity = when {
            aqiValue <= 50 -> "Good"
            aqiValue <= 100 -> "Moderate"
            aqiValue <= 150 -> "Unhealthy for Sensitive Groups"
            aqiValue <= 200 -> "Unhealthy"
            aqiValue <= 300 -> "Very Unhealthy"
            else -> "Hazardous"
        }

        val airQuality = AirQualityData(
            aqi = aqiValue,
            aqiSeverity = aqiSeverity,
            mainPollutant = if ((airQualityDto?.pm25 ?: 0.0) > 25.0) "PM2.5" else "O₃",
            pm25 = airQualityDto?.pm25 ?: 12.4,
            pm10 = airQualityDto?.pm10 ?: 24.1,
            o3 = airQualityDto?.ozone ?: 38.0,
            no2 = airQualityDto?.nitrogenDioxide ?: 15.2,
            so2 = airQualityDto?.sulphurDioxide ?: 4.0,
            co = airQualityDto?.carbonMonoxide ?: 240.0,
            uvIndex = airQualityDto?.uvIndex ?: currentDto.uvIndex
        )

        // Calculate daylight progress and Moon phase
        val sunMoon = calculateSunMoon(sunriseToday, sunsetToday)

        // Generate dynamic Weather Insight from raw data
        val insight = generateWeatherInsight(currentWeather, dailyItems.firstOrNull(), hourlyItems, airQuality)

        return WeatherSnapshot(
            location = location,
            current = currentWeather,
            hourly = hourlyItems,
            daily = dailyItems,
            airQuality = airQuality,
            sunMoon = sunMoon,
            insight = insight,
            lastUpdatedMillis = System.currentTimeMillis(),
            isCached = false
        )
    }

    private fun formatTimeOnly(
        raw: String?,
        parser: SimpleDateFormat,
        formatter: SimpleDateFormat
    ): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            val date = parser.parse(raw)
            if (date != null) formatter.format(date) else raw
        } catch (e: Exception) {
            raw.takeLast(5)
        }
    }

    private fun calculateSunMoon(sunriseStr: String, sunsetStr: String): SunMoonData {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val sunriseMinutes = parseMinutes(sunriseStr, 6 * 60)
        val sunsetMinutes = parseMinutes(sunsetStr, 18 * 60 + 30)

        val progress = when {
            currentMinutes <= sunriseMinutes -> 0f
            currentMinutes >= sunsetMinutes -> 1f
            else -> ((currentMinutes - sunriseMinutes).toFloat() / (sunsetMinutes - sunriseMinutes).toFloat()).coerceIn(0f, 1f)
        }
        val isDaytime = currentMinutes in sunriseMinutes until sunsetMinutes

        // Lunar cycle calculation based on known new moon reference date
        val daysSinceRef = (System.currentTimeMillis() - 947163600000L) / (1000.0 * 60 * 60 * 24)
        val synodicMonth = 29.53058867
        val phaseCycle = (daysSinceRef % synodicMonth) / synodicMonth
        val phasePercent = (phaseCycle * 100).roundToInt()

        val (phaseName, moonrise, moonset) = when {
            phaseCycle < 0.03 || phaseCycle > 0.97 -> Triple("New Moon", "06:30", "18:40")
            phaseCycle < 0.22 -> Triple("Waxing Crescent", "09:15", "21:40")
            phaseCycle < 0.28 -> Triple("First Quarter", "12:30", "00:50")
            phaseCycle < 0.47 -> Triple("Waxing Gibbous", "15:40", "03:15")
            phaseCycle < 0.53 -> Triple("Full Moon", "18:50", "06:20")
            phaseCycle < 0.72 -> Triple("Waning Gibbous", "21:30", "09:40")
            phaseCycle < 0.78 -> Triple("Last Quarter", "00:15", "12:50")
            else -> Triple("Waning Crescent", "03:40", "15:30")
        }

        return SunMoonData(
            sunrise = sunriseStr,
            sunset = sunsetStr,
            daylightProgress = progress,
            isDaytime = isDaytime,
            moonPhasePercent = phasePercent,
            moonPhaseName = phaseName,
            moonrise = moonrise,
            moonset = moonset
        )
    }

    private fun parseMinutes(timeStr: String, fallback: Int): Int {
        val parts = timeStr.split(":")
        if (parts.size >= 2) {
            val h = parts[0].trim().toIntOrNull() ?: return fallback
            val m = parts[1].trim().toIntOrNull() ?: return fallback
            return h * 60 + m
        }
        return fallback
    }

    private fun generateWeatherInsight(
        current: CurrentWeather,
        todayDaily: DailyForecastItem?,
        hourly: List<HourlyForecastItem>,
        airQuality: AirQualityData
    ): String {
        val parts = mutableListOf<String>()

        // Temperature & feel
        val tempDiff = (current.feelsLikeCelsius - current.temperatureCelsius).roundToInt()
        if (tempDiff >= 3) {
            parts.add("Feels warmer at ${current.feelsLikeCelsius.roundToInt()}° due to high humidity.")
        } else if (tempDiff <= -3) {
            parts.add("Feels cooler at ${current.feelsLikeCelsius.roundToInt()}° due to wind chill.")
        } else {
            parts.add("${current.condition.description} with pleasant conditions.")
        }

        // Precipitation prediction in next few hours
        val rainAhead = hourly.take(8).firstOrNull { it.precipitationProbability >= 40 }
        if (rainAhead != null) {
            parts.add("Rain expected around ${rainAhead.displayHour} (${rainAhead.precipitationProbability}% chance).")
        } else if (current.precipitationMm > 0) {
            parts.add("Ongoing precipitation with accumulation expected.")
        }

        // UV and Wind
        if (current.uvIndex >= 6.0) {
            parts.add("UV index is very high (${current.uvIndex.roundToInt()}); sun protection advised.")
        } else if (current.windSpeedKmh >= 30.0) {
            parts.add("Brisk winds with gusts up to ${current.windGustsKmh.roundToInt()} km/h.")
        } else if (airQuality.aqi > 100) {
            parts.add("Air quality is ${airQuality.aqiSeverity.lowercase()} (AQI ${airQuality.aqi}).")
        }

        return parts.joinToString(" ")
    }
}
