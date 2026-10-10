package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ==========================================
// Open-Meteo Weather API DTOs
// ==========================================

@JsonClass(generateAdapter = true)
data class OpenMeteoWeatherResponse(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "timezone") val timezone: String? = null,
    @Json(name = "current") val current: OpenMeteoCurrentDto? = null,
    @Json(name = "hourly") val hourly: OpenMeteoHourlyDto? = null,
    @Json(name = "daily") val daily: OpenMeteoDailyDto? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrentDto(
    @Json(name = "time") val time: String,
    @Json(name = "temperature_2m") val temperature2m: Double = 0.0,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Double = 0.0,
    @Json(name = "apparent_temperature") val apparentTemperature: Double = 0.0,
    @Json(name = "is_day") val isDay: Int = 1,
    @Json(name = "precipitation") val precipitation: Double = 0.0,
    @Json(name = "rain") val rain: Double = 0.0,
    @Json(name = "showers") val showers: Double = 0.0,
    @Json(name = "snowfall") val snowfall: Double = 0.0,
    @Json(name = "weather_code") val weatherCode: Int = 0,
    @Json(name = "cloud_cover") val cloudCover: Double = 0.0,
    @Json(name = "pressure_msl") val pressureMsl: Double = 1013.25,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double = 0.0,
    @Json(name = "wind_direction_10m") val windDirection10m: Double = 0.0,
    @Json(name = "wind_gusts_10m") val windGusts10m: Double = 0.0,
    @Json(name = "dew_point_2m") val dewPoint2m: Double = 0.0,
    @Json(name = "uv_index") val uvIndex: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class OpenMeteoHourlyDto(
    @Json(name = "time") val time: List<String> = emptyList(),
    @Json(name = "temperature_2m") val temperature2m: List<Double> = emptyList(),
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: List<Double> = emptyList(),
    @Json(name = "apparent_temperature") val apparentTemperature: List<Double> = emptyList(),
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>? = emptyList(),
    @Json(name = "precipitation") val precipitation: List<Double>? = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int> = emptyList(),
    @Json(name = "cloud_cover") val cloudCover: List<Double>? = emptyList(),
    @Json(name = "wind_speed_10m") val windSpeed10m: List<Double>? = emptyList(),
    @Json(name = "uv_index") val uvIndex: List<Double>? = emptyList(),
    @Json(name = "is_day") val isDay: List<Int>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class OpenMeteoDailyDto(
    @Json(name = "time") val time: List<String> = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int> = emptyList(),
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double> = emptyList(),
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double> = emptyList(),
    @Json(name = "apparent_temperature_max") val apparentTemperatureMax: List<Double>? = emptyList(),
    @Json(name = "apparent_temperature_min") val apparentTemperatureMin: List<Double>? = emptyList(),
    @Json(name = "sunrise") val sunrise: List<String>? = emptyList(),
    @Json(name = "sunset") val sunset: List<String>? = emptyList(),
    @Json(name = "uv_index_max") val uvIndexMax: List<Double>? = emptyList(),
    @Json(name = "precipitation_sum") val precipitationSum: List<Double>? = emptyList(),
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>? = emptyList(),
    @Json(name = "wind_speed_10m_max") val windSpeed10mMax: List<Double>? = emptyList(),
    @Json(name = "wind_direction_10m_dominant") val windDirection10mDominant: List<Double>? = emptyList()
)

// ==========================================
// Open-Meteo Air Quality API DTOs
// ==========================================

@JsonClass(generateAdapter = true)
data class OpenMeteoAirQualityResponse(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "current") val current: OpenMeteoAirQualityCurrentDto? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoAirQualityCurrentDto(
    @Json(name = "european_aqi") val europeanAqi: Double? = null,
    @Json(name = "us_aqi") val usAqi: Double? = null,
    @Json(name = "pm10") val pm10: Double? = null,
    @Json(name = "pm2_5") val pm25: Double? = null,
    @Json(name = "carbon_monoxide") val carbonMonoxide: Double? = null,
    @Json(name = "nitrogen_dioxide") val nitrogenDioxide: Double? = null,
    @Json(name = "sulphur_dioxide") val sulphurDioxide: Double? = null,
    @Json(name = "ozone") val ozone: Double? = null,
    @Json(name = "dust") val dust: Double? = null,
    @Json(name = "uv_index") val uvIndex: Double? = null
)

// ==========================================
// Open-Meteo Geocoding API DTOs
// ==========================================

@JsonClass(generateAdapter = true)
data class OpenMeteoGeocodingResponse(
    @Json(name = "results") val results: List<OpenMeteoGeocodingItem>? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoGeocodingItem(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "country") val country: String? = null,
    @Json(name = "country_code") val countryCode: String? = null,
    @Json(name = "admin1") val admin1: String? = null,
    @Json(name = "timezone") val timezone: String? = null
)

// ==========================================
// Domain Models
// ==========================================

enum class WeatherCategory {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SHOWERS,
    SNOW,
    THUNDERSTORM
}

enum class WeatherCondition(
    val code: Int,
    val description: String,
    val category: WeatherCategory,
    val isThunderstorm: Boolean = false,
    val isRain: Boolean = false,
    val isSnow: Boolean = false
) {
    CLEAR_SKY(0, "Clear Sky", WeatherCategory.CLEAR),
    MAINLY_CLEAR(1, "Mainly Clear", WeatherCategory.CLEAR),
    PARTLY_CLOUDY(2, "Partly Cloudy", WeatherCategory.PARTLY_CLOUDY),
    OVERCAST(3, "Overcast", WeatherCategory.CLOUDY),
    FOG(45, "Foggy", WeatherCategory.FOG),
    DEPOSITING_RIME_FOG(48, "Rime Fog", WeatherCategory.FOG),
    LIGHT_DRIZZLE(51, "Light Drizzle", WeatherCategory.DRIZZLE, isRain = true),
    MODERATE_DRIZZLE(53, "Drizzle", WeatherCategory.DRIZZLE, isRain = true),
    DENSE_DRIZZLE(55, "Dense Drizzle", WeatherCategory.DRIZZLE, isRain = true),
    FREEZING_LIGHT_DRIZZLE(56, "Freezing Drizzle", WeatherCategory.DRIZZLE, isRain = true),
    FREEZING_DENSE_DRIZZLE(57, "Dense Freezing Drizzle", WeatherCategory.DRIZZLE, isRain = true),
    SLIGHT_RAIN(61, "Slight Rain", WeatherCategory.RAIN, isRain = true),
    MODERATE_RAIN(63, "Moderate Rain", WeatherCategory.RAIN, isRain = true),
    HEAVY_RAIN(65, "Heavy Rain", WeatherCategory.RAIN, isRain = true),
    FREEZING_LIGHT_RAIN(66, "Light Freezing Rain", WeatherCategory.RAIN, isRain = true),
    FREEZING_HEAVY_RAIN(67, "Heavy Freezing Rain", WeatherCategory.RAIN, isRain = true),
    SLIGHT_SNOW(71, "Slight Snow", WeatherCategory.SNOW, isSnow = true),
    MODERATE_SNOW(73, "Moderate Snow", WeatherCategory.SNOW, isSnow = true),
    HEAVY_SNOW(75, "Heavy Snow", WeatherCategory.SNOW, isSnow = true),
    SNOW_GRAINS(77, "Snow Grains", WeatherCategory.SNOW, isSnow = true),
    SLIGHT_RAIN_SHOWERS(80, "Light Showers", WeatherCategory.SHOWERS, isRain = true),
    MODERATE_RAIN_SHOWERS(81, "Rain Showers", WeatherCategory.SHOWERS, isRain = true),
    VIOLENT_RAIN_SHOWERS(82, "Violent Showers", WeatherCategory.SHOWERS, isRain = true),
    SLIGHT_SNOW_SHOWERS(85, "Snow Showers", WeatherCategory.SNOW, isSnow = true),
    HEAVY_SNOW_SHOWERS(86, "Heavy Snow Showers", WeatherCategory.SNOW, isSnow = true),
    THUNDERSTORM(95, "Thunderstorm", WeatherCategory.THUNDERSTORM, isThunderstorm = true, isRain = true),
    THUNDERSTORM_SLIGHT_HAIL(96, "Thunderstorm with Hail", WeatherCategory.THUNDERSTORM, isThunderstorm = true, isRain = true),
    THUNDERSTORM_HEAVY_HAIL(99, "Severe Thunderstorm", WeatherCategory.THUNDERSTORM, isThunderstorm = true, isRain = true);

    companion object {
        fun fromWmoCode(code: Int): WeatherCondition {
            return entries.firstOrNull { it.code == code } ?: when {
                code in 1..2 -> PARTLY_CLOUDY
                code == 3 -> OVERCAST
                code in 40..49 -> FOG
                code in 50..59 -> MODERATE_DRIZZLE
                code in 60..69 -> MODERATE_RAIN
                code in 70..79 -> MODERATE_SNOW
                code in 80..84 -> MODERATE_RAIN_SHOWERS
                code in 85..89 -> SLIGHT_SNOW_SHOWERS
                code >= 90 -> THUNDERSTORM
                else -> CLEAR_SKY
            }
        }
    }
}

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    val temperatureCelsius: Double,
    val feelsLikeCelsius: Double,
    val condition: WeatherCondition,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val windDirectionDegrees: Double,
    val windGustsKmh: Double,
    val pressureHpa: Double,
    val uvIndex: Double,
    val cloudCoverPercent: Int,
    val dewPointCelsius: Double,
    val precipitationMm: Double,
    val isDay: Boolean,
    val timestamp: String
)

@JsonClass(generateAdapter = true)
data class HourlyForecastItem(
    val timeIso: String,
    val displayHour: String,
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
    val precipitationProbability: Int,
    val precipitationMm: Double,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val isDay: Boolean,
    val isCurrentHour: Boolean = false
)

@JsonClass(generateAdapter = true)
data class DailyForecastItem(
    val dateIso: String,
    val dayName: String,
    val dateFormatted: String,
    val maxTempCelsius: Double,
    val minTempCelsius: Double,
    val condition: WeatherCondition,
    val precipitationProbabilityMax: Int,
    val precipitationSumMm: Double,
    val sunrise: String,
    val sunset: String,
    val uvIndexMax: Double,
    val windSpeedMaxKmh: Double
)

@JsonClass(generateAdapter = true)
data class AirQualityData(
    val aqi: Int,
    val aqiSeverity: String,
    val mainPollutant: String,
    val pm25: Double,
    val pm10: Double,
    val o3: Double,
    val no2: Double,
    val so2: Double,
    val co: Double,
    val uvIndex: Double
)

@JsonClass(generateAdapter = true)
data class SunMoonData(
    val sunrise: String,
    val sunset: String,
    val daylightProgress: Float, // 0.0 to 1.0
    val isDaytime: Boolean,
    val moonPhasePercent: Int, // 0 to 100
    val moonPhaseName: String,
    val moonrise: String,
    val moonset: String
)

@JsonClass(generateAdapter = true)
data class UserLocation(
    val id: String,
    val name: String,
    val country: String,
    val admin1: String? = null,
    val latitude: Double,
    val longitude: Double,
    val isCurrentLocation: Boolean = false,
    val isDefault: Boolean = false
) {
    val displayName: String
        get() = when {
            admin1.isNullOrBlank() && country.isBlank() -> name
            admin1.isNullOrBlank() -> "$name, $country"
            country.isBlank() -> "$name, $admin1"
            else -> "$name, $admin1, $country"
        }
}

@JsonClass(generateAdapter = true)
data class WeatherSnapshot(
    val location: UserLocation,
    val current: CurrentWeather,
    val hourly: List<HourlyForecastItem>,
    val daily: List<DailyForecastItem>,
    val airQuality: AirQualityData,
    val sunMoon: SunMoonData,
    val insight: String,
    val lastUpdatedMillis: Long = System.currentTimeMillis(),
    val isCached: Boolean = false
)
