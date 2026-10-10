package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewQuilt
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeatherCondition
import com.example.ui.atmosphere.WeatherAtmosphere
import com.example.ui.screens.DetailsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LocationsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.RadarScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ArcWeatherTheme
import com.example.ui.viewmodel.WeatherUiState
import com.example.ui.viewmodel.WeatherViewModel

/**
 * State-based navigation destinations for ArcWeather.
 * Supports smooth transitions without requiring navigation-compose.
 */
enum class AppScreen(val route: String, val title: String, val icon: ImageVector, val tag: String) {
    ONBOARDING("onboarding", "Welcome", Icons.Rounded.Home, "screen_onboarding"),
    HOME("home", "Home", Icons.Rounded.Home, "tab_home"),
    DETAILS("details", "Metrics", Icons.AutoMirrored.Rounded.ViewQuilt, "tab_details"),
    RADAR("radar", "Radar", Icons.Rounded.Layers, "tab_radar"),
    LOCATIONS("locations", "Cities", Icons.Rounded.LocationCity, "tab_locations"),
    SETTINGS("settings", "Settings", Icons.Rounded.Tune, "tab_settings")
}

/**
 * Lightweight state-based navigation controller with back stack management.
 */
class StateNavController(initialScreen: AppScreen) {
    var currentScreen by mutableStateOf(initialScreen)
        private set

    val backStack = mutableStateListOf<AppScreen>()

    val canNavigateBack: Boolean
        get() = backStack.isNotEmpty()

    fun navigateTo(screen: AppScreen, clearStack: Boolean = false) {
        if (clearStack) {
            backStack.clear()
        } else if (currentScreen != screen) {
            backStack.add(currentScreen)
        }
        currentScreen = screen
    }

    fun popBackStack(): Boolean {
        if (backStack.isNotEmpty()) {
            currentScreen = backStack.removeAt(backStack.lastIndex)
            return true
        }
        return false
    }

    fun popToHome() {
        backStack.clear()
        currentScreen = AppScreen.HOME
    }
}

class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()
            val uiState by viewModel.uiState.collectAsState()
            val savedLocations by viewModel.savedLocations.collectAsState()
            val selectedLocation by viewModel.selectedLocation.collectAsState()
            val searchResults by viewModel.searchResults.collectAsState()
            val isSearching by viewModel.isSearching.collectAsState()

            // Initialize state-based navigation controller
            val navController = remember {
                StateNavController(
                    if (uiState is WeatherUiState.Onboarding) AppScreen.ONBOARDING else AppScreen.HOME
                )
            }

            // Sync navigation when onboarding completes or starts
            LaunchedEffect(uiState) {
                if (uiState is WeatherUiState.Onboarding && navController.currentScreen != AppScreen.ONBOARDING && !navController.canNavigateBack) {
                    navController.navigateTo(AppScreen.ONBOARDING, clearStack = true)
                } else if (uiState is WeatherUiState.Success && navController.currentScreen == AppScreen.ONBOARDING && navController.backStack.isEmpty()) {
                    navController.navigateTo(AppScreen.HOME, clearStack = true)
                }
            }

            // Intercept system Back button to pop backstack
            BackHandler(enabled = navController.canNavigateBack || navController.currentScreen != AppScreen.HOME) {
                if (!navController.popBackStack()) {
                    navController.popToHome()
                }
            }

            ArcWeatherTheme(themeMode = settings.themeMode) {
                Scaffold(
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    containerColor = Color.Transparent,
                    modifier = Modifier.fillMaxSize()
                ) { _ ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Loading & Error States overlay cleanly if data isn't ready
                        when (val state = uiState) {
                            is WeatherUiState.Loading -> {
                                WeatherAtmosphereLoadingScreen(
                                    locationName = selectedLocation?.name ?: "Loading weather..."
                                )
                            }

                            is WeatherUiState.Error -> {
                                WeatherErrorScreen(
                                    errorMessage = state.message,
                                    onRetry = {
                                        selectedLocation?.let { viewModel.loadWeather(it) }
                                            ?: viewModel.detectCurrentLocationAndLoad()
                                    },
                                    onSearchManually = {
                                        navController.navigateTo(AppScreen.LOCATIONS)
                                    }
                                )
                            }

                            else -> {
                                val currentSnapshot = (state as? WeatherUiState.Success)?.snapshot

                                // Animated screen transitions between Onboarding, Home, Details, Radar, Locations, and Settings
                                AnimatedContent(
                                    targetState = navController.currentScreen,
                                    transitionSpec = {
                                        val isForward = targetState.ordinal > initialState.ordinal
                                        if (isForward) {
                                            (slideInHorizontally(
                                                initialOffsetX = { fullWidth -> fullWidth / 2 },
                                                animationSpec = tween(280, easing = FastOutSlowInEasing)
                                            ) + fadeIn(animationSpec = tween(240))) togetherWith
                                                    (slideOutHorizontally(
                                                        targetOffsetX = { fullWidth -> -fullWidth / 3 },
                                                        animationSpec = tween(240, easing = FastOutSlowInEasing)
                                                    ) + fadeOut(animationSpec = tween(180)))
                                        } else {
                                            (slideInHorizontally(
                                                initialOffsetX = { fullWidth -> -fullWidth / 2 },
                                                animationSpec = tween(280, easing = FastOutSlowInEasing)
                                            ) + fadeIn(animationSpec = tween(240))) togetherWith
                                                    (slideOutHorizontally(
                                                        targetOffsetX = { fullWidth -> fullWidth / 3 },
                                                        animationSpec = tween(240, easing = FastOutSlowInEasing)
                                                    ) + fadeOut(animationSpec = tween(180)))
                                        }
                                    },
                                    label = "ScreenTransitionGraph",
                                    modifier = Modifier.fillMaxSize()
                                ) { screen ->
                                    when (screen) {
                                        AppScreen.ONBOARDING -> {
                                            OnboardingScreen(
                                                onUseLocationClick = {
                                                    viewModel.detectCurrentLocationAndLoad()
                                                    navController.navigateTo(AppScreen.HOME, clearStack = true)
                                                },
                                                onSelectCity = { loc ->
                                                    viewModel.addLocation(loc)
                                                    navController.navigateTo(AppScreen.HOME, clearStack = true)
                                                },
                                                onSearchQuery = { q -> viewModel.searchCities(q) },
                                                searchResults = searchResults,
                                                isSearching = isSearching
                                            )
                                        }

                                        AppScreen.HOME -> {
                                            if (currentSnapshot != null) {
                                                HomeScreen(
                                                    snapshot = currentSnapshot,
                                                    settings = settings,
                                                    isRefreshing = (state as? WeatherUiState.Success)?.isRefreshing ?: false,
                                                    onRefresh = { viewModel.loadWeather(currentSnapshot.location, isRefresh = true) },
                                                    onLocationTitleClick = { navController.navigateTo(AppScreen.LOCATIONS) },
                                                    onNavigateToDetails = { navController.navigateTo(AppScreen.DETAILS) },
                                                    onNavigateToSettings = { navController.navigateTo(AppScreen.SETTINGS) }
                                                )
                                            } else {
                                                WeatherAtmosphereLoadingScreen(locationName = "Loading Home...")
                                            }
                                        }

                                        AppScreen.DETAILS -> {
                                            if (currentSnapshot != null) {
                                                DetailsScreen(
                                                    snapshot = currentSnapshot,
                                                    settings = settings
                                                )
                                            } else {
                                                WeatherAtmosphereLoadingScreen(locationName = "Loading Metrics...")
                                            }
                                        }

                                        AppScreen.RADAR -> {
                                            if (currentSnapshot != null) {
                                                RadarScreen(currentLocation = currentSnapshot.location)
                                            } else {
                                                WeatherAtmosphereLoadingScreen(locationName = "Loading Radar...")
                                            }
                                        }

                                        AppScreen.LOCATIONS -> {
                                            LocationsScreen(
                                                savedLocations = savedLocations,
                                                selectedLocationId = currentSnapshot?.location?.id ?: selectedLocation?.id ?: "",
                                                searchResults = searchResults,
                                                isSearching = isSearching,
                                                onSearchQuery = { q -> viewModel.searchCities(q) },
                                                onSelectLocation = { loc ->
                                                    viewModel.selectLocation(loc)
                                                    navController.navigateTo(AppScreen.HOME)
                                                },
                                                onAddLocation = { loc ->
                                                    viewModel.addLocation(loc)
                                                    navController.navigateTo(AppScreen.HOME)
                                                },
                                                onRemoveLocation = { loc -> viewModel.removeLocation(loc) },
                                                onSetDefaultLocation = { loc -> viewModel.setDefaultLocation(loc) },
                                                onDetectCurrentLocation = {
                                                    viewModel.detectCurrentLocationAndLoad()
                                                    navController.navigateTo(AppScreen.HOME)
                                                }
                                            )
                                        }

                                        AppScreen.SETTINGS -> {
                                            SettingsScreen(
                                                settings = settings,
                                                onUpdateThemeMode = { mode -> viewModel.updateThemeMode(mode) },
                                                onUpdateTempUnit = { unit -> viewModel.updateTempUnit(unit) },
                                                onUpdateWindUnit = { unit -> viewModel.updateWindUnit(unit) },
                                                onUpdatePressureUnit = { unit -> viewModel.updatePressureUnit(unit) },
                                                onUpdateReduceEffects = { reduce -> viewModel.updateReduceEffects(reduce) },
                                                onUpdateNotification = { k, v -> viewModel.updateNotification(k, v) },
                                                onClearCache = { viewModel.clearCache() },
                                                onNavigateBack = { navController.popBackStack() },
                                                onRevisitOnboarding = { navController.navigateTo(AppScreen.ONBOARDING) }
                                            )
                                        }
                                    }
                                }

                                // Floating Translucent Dock (hidden during Onboarding screen)
                                if (navController.currentScreen != AppScreen.ONBOARDING) {
                                    FloatingTranslucentDock(
                                        currentScreen = navController.currentScreen,
                                        onTabSelected = { selectedScreen ->
                                            if (selectedScreen == AppScreen.HOME && navController.currentScreen == AppScreen.HOME) {
                                                // Already at Home, optionally refresh
                                                currentSnapshot?.let { viewModel.loadWeather(it.location, isRefresh = true) }
                                            } else {
                                                navController.navigateTo(selectedScreen)
                                            }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .windowInsetsPadding(WindowInsets.navigationBars)
                                            .padding(horizontal = 20.dp, vertical = 12.dp)
                                            .testTag("floating_dock")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Floating Translucent Dock with Hardware Blur and Frosted Glass styling.
 * Floats seamlessly above background weather animations.
 */
@Composable
fun FloatingTranslucentDock(
    currentScreen: AppScreen,
    onTabSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val dockShape = RoundedCornerShape(32.dp)
    val isLight = MaterialTheme.colorScheme.background.red > 0.5f

    // Frosted glass background brush and border
    val glassBgColor = if (isLight) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f)
    }

    val glassBorderColor = if (isLight) {
        Color.White.copy(alpha = 0.65f)
    } else {
        Color.White.copy(alpha = 0.15f)
    }

    // Tabs displayed on dock
    val dockTabs = remember {
        listOf(
            AppScreen.HOME,
            AppScreen.DETAILS,
            AppScreen.RADAR,
            AppScreen.LOCATIONS,
            AppScreen.SETTINGS
        )
    }

    Box(
        modifier = modifier
            .widthIn(max = 440.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = dockShape,
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
    ) {
        // Hardware blur backdrop layer (Android 12+ API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(dockShape)
                    .blur(24.dp)
            )
        }

        // Translucent frosted glass container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(dockShape)
                .background(glassBgColor)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(glassBorderColor, glassBorderColor.copy(alpha = 0.05f))
                    ),
                    shape = dockShape
                )
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dockTabs.forEach { tab ->
                val isSelected = currentScreen == tab

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    },
                    animationSpec = tween(durationMillis = 200),
                    label = "DockIconColor"
                )

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                    label = "DockItemScale"
                )

                val pillBgColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    } else {
                        Color.Transparent
                    },
                    animationSpec = tween(durationMillis = 200),
                    label = "DockPillBg"
                )

                Column(
                    modifier = Modifier
                        .scale(scale)
                        .clip(RoundedCornerShape(20.dp))
                        .background(pillBgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, radius = 28.dp),
                            onClick = { onTabSelected(tab) }
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag(tab.tag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = iconColor
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherAtmosphereLoadingScreen(locationName: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "SkeletonPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    WeatherAtmosphere(
        condition = WeatherCondition.CLEAR_SKY,
        isDay = true,
        reduceEffects = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 28.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = locationName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Fetching live atmosphere data...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Skeleton cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = alpha))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = alpha))
                )
            }
        }
    }
}

@Composable
fun WeatherErrorScreen(
    errorMessage: String,
    onRetry: () -> Unit,
    onSearchManually: () -> Unit
) {
    WeatherAtmosphere(
        condition = WeatherCondition.OVERCAST,
        isDay = true,
        reduceEffects = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 28.dp, start = 32.dp, end = 32.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Weather data unavailable",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
                    .testTag("error_retry_button")
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onSearchManually,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
                    .testTag("error_search_button")
            ) {
                Text("Search Manually", fontWeight = FontWeight.Bold)
            }
        }
    }
}
