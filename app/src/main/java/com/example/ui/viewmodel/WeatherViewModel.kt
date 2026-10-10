package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppSettings
import com.example.data.local.AppThemeMode
import com.example.data.local.PreferencesManager
import com.example.data.local.PressureUnit
import com.example.data.local.TempUnit
import com.example.data.local.WindUnit
import com.example.data.location.LocationClient
import com.example.data.model.UserLocation
import com.example.data.model.WeatherSnapshot
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    data object Onboarding : WeatherUiState
    data class Loading(val isFirstLoad: Boolean = true) : WeatherUiState
    data class Success(val snapshot: WeatherSnapshot, val isRefreshing: Boolean = false) : WeatherUiState
    data class Error(val message: String, val cachedSnapshot: WeatherSnapshot? = null) : WeatherUiState
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val weatherRepository = WeatherRepository(preferencesManager = preferencesManager)
    private val locationClient = LocationClient(application)

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading(true))
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    val settings: StateFlow<AppSettings> = preferencesManager.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val savedLocations: StateFlow<List<UserLocation>> = preferencesManager.savedLocations
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedLocation = MutableStateFlow<UserLocation?>(null)
    val selectedLocation: StateFlow<UserLocation?> = _selectedLocation.asStateFlow()

    private val _searchResults = MutableStateFlow<List<UserLocation>>(emptyList())
    val searchResults: StateFlow<List<UserLocation>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            val onboardingDone = preferencesManager.isOnboardingCompleted.first()
            if (!onboardingDone) {
                _uiState.value = WeatherUiState.Onboarding
            } else {
                val locations = preferencesManager.savedLocations.first()
                val selectedId = preferencesManager.selectedLocationId.firstOrNull()
                val targetLocation = locations.firstOrNull { it.id == selectedId }
                    ?: locations.firstOrNull { it.isDefault }
                    ?: locations.firstOrNull()
                    ?: defaultLocation()

                _selectedLocation.value = targetLocation
                loadWeather(targetLocation)
            }
        }
    }

    fun loadWeather(location: UserLocation, isRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is WeatherUiState.Success && isRefresh) {
                _uiState.value = currentState.copy(isRefreshing = true)
            } else if (!isRefresh) {
                _uiState.value = WeatherUiState.Loading(isFirstLoad = currentState !is WeatherUiState.Success)
            }

            val result = weatherRepository.getWeatherData(location)
            result.fold(
                onSuccess = { snapshot ->
                    _uiState.value = WeatherUiState.Success(snapshot = snapshot, isRefreshing = false)
                },
                onFailure = { error ->
                    val cached = if (currentState is WeatherUiState.Success) currentState.snapshot else null
                    _uiState.value = WeatherUiState.Error(
                        message = error.message ?: "Failed to retrieve weather data",
                        cachedSnapshot = cached
                    )
                }
            )
        }
    }

    fun detectCurrentLocationAndLoad() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading(true)
            val location = locationClient.getCurrentLocation()
            if (location != null) {
                val userLoc = locationClient.reverseGeocode(location.latitude, location.longitude)
                // Add to saved locations and set as default/selected
                val existing = preferencesManager.savedLocations.first().toMutableList()
                val updated = existing.filterNot { it.id == "current_gps" }.toMutableList()
                updated.add(0, userLoc)
                preferencesManager.saveLocations(updated)
                preferencesManager.setSelectedLocationId(userLoc.id)
                preferencesManager.setOnboardingCompleted(true)
                _selectedLocation.value = userLoc
                loadWeather(userLoc)
            } else {
                // Fallback to default city if location failed
                val fallback = defaultLocation()
                selectLocation(fallback)
                preferencesManager.setOnboardingCompleted(true)
            }
        }
    }

    fun selectLocation(location: UserLocation) {
        _selectedLocation.value = location
        viewModelScope.launch {
            preferencesManager.setSelectedLocationId(location.id)
            preferencesManager.setOnboardingCompleted(true)
            loadWeather(location)
        }
    }

    fun addLocation(location: UserLocation) {
        viewModelScope.launch {
            val current = preferencesManager.savedLocations.first().toMutableList()
            if (current.none { it.name.equals(location.name, ignoreCase = true) }) {
                current.add(location)
                preferencesManager.saveLocations(current)
            }
            selectLocation(location)
        }
    }

    fun removeLocation(location: UserLocation) {
        viewModelScope.launch {
            val current = preferencesManager.savedLocations.first().toMutableList()
            if (current.size > 1) {
                current.removeAll { it.id == location.id }
                preferencesManager.saveLocations(current)
                if (_selectedLocation.value?.id == location.id) {
                    val next = current.first()
                    selectLocation(next)
                }
            }
        }
    }

    fun setDefaultLocation(location: UserLocation) {
        viewModelScope.launch {
            val current = preferencesManager.savedLocations.first().map {
                it.copy(isDefault = it.id == location.id)
            }
            preferencesManager.saveLocations(current)
        }
    }

    fun searchCities(query: String) {
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(300) // Debounce
            val results = weatherRepository.searchLocations(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun updateThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { preferencesManager.updateThemeMode(mode) }
    }

    fun updateTempUnit(unit: TempUnit) {
        viewModelScope.launch { preferencesManager.updateTempUnit(unit) }
    }

    fun updateWindUnit(unit: WindUnit) {
        viewModelScope.launch { preferencesManager.updateWindUnit(unit) }
    }

    fun updatePressureUnit(unit: PressureUnit) {
        viewModelScope.launch { preferencesManager.updatePressureUnit(unit) }
    }

    fun updateReduceEffects(reduce: Boolean) {
        viewModelScope.launch { preferencesManager.updateReduceEffects(reduce) }
    }

    fun updateNotification(key: String, enabled: Boolean) {
        viewModelScope.launch { preferencesManager.updateNotification(key, enabled) }
    }

    fun clearCache() {
        viewModelScope.launch {
            preferencesManager.clearCache()
            _selectedLocation.value?.let { loadWeather(it, isRefresh = true) }
        }
    }

    private fun defaultLocation() = UserLocation(
        id = "default_bengaluru",
        name = "Bengaluru",
        country = "India",
        admin1 = "Karnataka",
        latitude = 12.9716,
        longitude = 77.5946,
        isCurrentLocation = false,
        isDefault = true
    )
}
