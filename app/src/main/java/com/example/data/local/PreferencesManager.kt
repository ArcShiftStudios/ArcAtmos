package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.UserLocation
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "arcweather_prefs")

enum class AppThemeMode {
    DYNAMIC,
    SYSTEM,
    LIGHT,
    DARK
}

enum class TempUnit {
    CELSIUS,
    FAHRENHEIT
}

enum class WindUnit {
    KMH,
    MPH
}

enum class PressureUnit {
    HPA,
    INHG
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.DYNAMIC,
    val tempUnit: TempUnit = TempUnit.CELSIUS,
    val windUnit: WindUnit = WindUnit.KMH,
    val pressureUnit: PressureUnit = PressureUnit.HPA,
    val reduceWeatherEffects: Boolean = false,
    val notifyDailyForecast: Boolean = true,
    val notifyRainAlerts: Boolean = true,
    val notifySevereWeather: Boolean = true,
    val notifyTempChanges: Boolean = false
)

class PreferencesManager(private val context: Context) {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val locationListType = Types.newParameterizedType(List::class.java, UserLocation::class.java)
    private val locationListAdapter = moshi.adapter<List<UserLocation>>(locationListType)

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_SELECTED_LOCATION_ID = stringPreferencesKey("selected_location_id")
        val KEY_SAVED_LOCATIONS = stringPreferencesKey("saved_locations_json")
        val KEY_CACHED_WEATHER_JSON = stringPreferencesKey("cached_weather_snapshot")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_TEMP_UNIT = stringPreferencesKey("temp_unit")
        val KEY_WIND_UNIT = stringPreferencesKey("wind_unit")
        val KEY_PRESSURE_UNIT = stringPreferencesKey("pressure_unit")
        val KEY_REDUCE_EFFECTS = booleanPreferencesKey("reduce_weather_effects")
        val KEY_NOTIFY_DAILY = booleanPreferencesKey("notify_daily")
        val KEY_NOTIFY_RAIN = booleanPreferencesKey("notify_rain")
        val KEY_NOTIFY_SEVERE = booleanPreferencesKey("notify_severe")
        val KEY_NOTIFY_TEMP_CHANGES = booleanPreferencesKey("notify_temp_changes")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    val selectedLocationId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_LOCATION_ID]
    }

    suspend fun setSelectedLocationId(locationId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_LOCATION_ID] = locationId
        }
    }

    val savedLocations: Flow<List<UserLocation>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_SAVED_LOCATIONS]
        if (!json.isNullOrBlank()) {
            try {
                locationListAdapter.fromJson(json) ?: defaultLocations()
            } catch (e: Exception) {
                defaultLocations()
            }
        } else {
            defaultLocations()
        }
    }

    suspend fun saveLocations(locations: List<UserLocation>) {
        val json = locationListAdapter.toJson(locations)
        context.dataStore.edit { prefs ->
            prefs[KEY_SAVED_LOCATIONS] = json
        }
    }

    val cachedWeatherJson: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_CACHED_WEATHER_JSON]
    }

    suspend fun setCachedWeatherJson(json: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CACHED_WEATHER_JSON] = json
        }
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = when (prefs[KEY_THEME_MODE]) {
                "SYSTEM" -> AppThemeMode.SYSTEM
                "LIGHT" -> AppThemeMode.LIGHT
                "DARK" -> AppThemeMode.DARK
                else -> AppThemeMode.DYNAMIC
            },
            tempUnit = when (prefs[KEY_TEMP_UNIT]) {
                "FAHRENHEIT" -> TempUnit.FAHRENHEIT
                else -> TempUnit.CELSIUS
            },
            windUnit = when (prefs[KEY_WIND_UNIT]) {
                "MPH" -> WindUnit.MPH
                else -> WindUnit.KMH
            },
            pressureUnit = when (prefs[KEY_PRESSURE_UNIT]) {
                "INHG" -> PressureUnit.INHG
                else -> PressureUnit.HPA
            },
            reduceWeatherEffects = prefs[KEY_REDUCE_EFFECTS] ?: false,
            notifyDailyForecast = prefs[KEY_NOTIFY_DAILY] ?: true,
            notifyRainAlerts = prefs[KEY_NOTIFY_RAIN] ?: true,
            notifySevereWeather = prefs[KEY_NOTIFY_SEVERE] ?: true,
            notifyTempChanges = prefs[KEY_NOTIFY_TEMP_CHANGES] ?: false
        )
    }

    suspend fun updateThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs -> prefs[KEY_THEME_MODE] = mode.name }
    }

    suspend fun updateTempUnit(unit: TempUnit) {
        context.dataStore.edit { prefs -> prefs[KEY_TEMP_UNIT] = unit.name }
    }

    suspend fun updateWindUnit(unit: WindUnit) {
        context.dataStore.edit { prefs -> prefs[KEY_WIND_UNIT] = unit.name }
    }

    suspend fun updatePressureUnit(unit: PressureUnit) {
        context.dataStore.edit { prefs -> prefs[KEY_PRESSURE_UNIT] = unit.name }
    }

    suspend fun updateReduceEffects(reduce: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_REDUCE_EFFECTS] = reduce }
    }

    suspend fun updateNotification(key: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            when (key) {
                "daily" -> prefs[KEY_NOTIFY_DAILY] = enabled
                "rain" -> prefs[KEY_NOTIFY_RAIN] = enabled
                "severe" -> prefs[KEY_NOTIFY_SEVERE] = enabled
                "temp" -> prefs[KEY_NOTIFY_TEMP_CHANGES] = enabled
            }
        }
    }

    suspend fun clearCache() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_CACHED_WEATHER_JSON)
        }
    }

    private fun defaultLocations(): List<UserLocation> = listOf(
        UserLocation(
            id = "default_bengaluru",
            name = "Bengaluru",
            country = "India",
            admin1 = "Karnataka",
            latitude = 12.9716,
            longitude = 77.5946,
            isCurrentLocation = false,
            isDefault = true
        )
    )
}
