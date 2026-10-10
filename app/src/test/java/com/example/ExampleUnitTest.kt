package com.example

import com.example.data.model.AirQualityData
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyForecastItem
import com.example.data.model.SunMoonData
import com.example.data.model.UserLocation
import com.example.data.model.WeatherCondition
import com.example.data.model.WeatherSnapshot
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testMoshiUserLocationSerialization() {
    val moshi = Moshi.Builder()
      .addLast(KotlinJsonAdapterFactory())
      .build()
    val listType = Types.newParameterizedType(List::class.java, UserLocation::class.java)
    val adapter = moshi.adapter<List<UserLocation>>(listType)

    val locations = listOf(
      UserLocation(
        id = "bengaluru",
        name = "Bengaluru",
        country = "India",
        latitude = 12.9716,
        longitude = 77.5946,
        isDefault = true
      )
    )

    val json = adapter.toJson(locations)
    assertNotNull(json)

    val restored = adapter.fromJson(json)
    assertNotNull(restored)
    assertEquals(1, restored?.size)
    assertEquals("Bengaluru", restored?.first()?.name)
  }

  @Test
  fun testMoshiWeatherSnapshotSerialization() {
    val moshi = Moshi.Builder()
      .addLast(KotlinJsonAdapterFactory())
      .build()
    val adapter = moshi.adapter(WeatherSnapshot::class.java)

    val snapshot = WeatherSnapshot(
      location = UserLocation(
        id = "test",
        name = "Test City",
        country = "Test Country",
        latitude = 10.0,
        longitude = 20.0
      ),
      current = CurrentWeather(
        temperatureCelsius = 22.0,
        feelsLikeCelsius = 23.0,
        condition = WeatherCondition.CLEAR_SKY,
        humidityPercent = 55,
        windSpeedKmh = 12.0,
        windDirectionDegrees = 180.0,
        windGustsKmh = 18.0,
        pressureHpa = 1013.25,
        uvIndex = 4.0,
        cloudCoverPercent = 10,
        dewPointCelsius = 12.0,
        precipitationMm = 0.0,
        isDay = true,
        timestamp = "2026-10-04T12:00"
      ),
      hourly = emptyList(),
      daily = emptyList(),
      airQuality = AirQualityData(
        aqi = 40,
        aqiSeverity = "Good",
        mainPollutant = "O3",
        pm25 = 10.0,
        pm10 = 20.0,
        o3 = 30.0,
        no2 = 15.0,
        so2 = 2.0,
        co = 200.0,
        uvIndex = 4.0
      ),
      sunMoon = SunMoonData(
        sunrise = "06:00",
        sunset = "18:00",
        daylightProgress = 0.5f,
        isDaytime = true,
        moonPhasePercent = 50,
        moonPhaseName = "Full Moon",
        moonrise = "18:30",
        moonset = "06:30"
      ),
      insight = "Great weather today."
    )

    val json = adapter.toJson(snapshot)
    assertNotNull(json)

    val restored = adapter.fromJson(json)
    assertNotNull(restored)
    assertEquals("Test City", restored?.location?.name)
    assertEquals(22.0, restored?.current?.temperatureCelsius ?: 0.0, 0.01)
  }
}
