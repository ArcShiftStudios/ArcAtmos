package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.TempUnit
import com.example.data.model.WeatherCategory
import com.example.data.model.WeatherCondition
import com.example.ui.components.formatTemp
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ArcAtmos", appName)
  }

  @Test
  fun `weather condition WMO code mapping`() {
    val clear = WeatherCondition.fromWmoCode(0)
    assertEquals(WeatherCategory.CLEAR, clear.category)

    val rain = WeatherCondition.fromWmoCode(63)
    assertEquals(WeatherCategory.RAIN, rain.category)
    assertEquals(true, rain.isRain)

    val storm = WeatherCondition.fromWmoCode(95)
    assertEquals(WeatherCategory.THUNDERSTORM, storm.category)
    assertEquals(true, storm.isThunderstorm)
  }

  @Test
  fun `temperature conversion format`() {
    val celsius = 25.0
    val formattedC = formatTemp(celsius, TempUnit.CELSIUS)
    assertEquals("25", formattedC)

    val formattedF = formatTemp(celsius, TempUnit.FAHRENHEIT)
    assertEquals("77", formattedF)
  }
}
