package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.local.AppThemeMode

private val ArcDarkColorScheme = darkColorScheme(
    primary = ArcBlue80,
    onPrimary = Color(0xFF003062),
    primaryContainer = Color(0xFF004787),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary = ArcBlueGrey80,
    onSecondary = Color(0xFF283041),
    secondaryContainer = Color(0xFF3E4759),
    onSecondaryContainer = Color(0xFFDCE2F9),
    tertiary = ArcCyan80,
    onTertiary = Color(0xFF003544),
    tertiaryContainer = Color(0xFF004D63),
    onTertiaryContainer = Color(0xFFBCE9FF),
    background = ArcDarkBackground,
    onBackground = ArcDarkOnSurface,
    surface = ArcDarkSurface,
    onSurface = ArcDarkOnSurface,
    surfaceVariant = ArcDarkSurfaceContainer,
    onSurfaceVariant = ArcDarkOnSurfaceVariant,
    surfaceContainer = ArcDarkSurfaceContainer,
    surfaceContainerHigh = ArcDarkSurfaceContainerHigh,
    outline = Color(0xFF8E909A),
    outlineVariant = Color(0xFF44474F)
)

private val ArcLightColorScheme = lightColorScheme(
    primary = ArcBlue40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E4FF),
    onPrimaryContainer = Color(0xFF001C3B),
    secondary = ArcBlueGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE2F9),
    onSecondaryContainer = Color(0xFF131B2C),
    tertiary = ArcCyan40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBCE9FF),
    onTertiaryContainer = Color(0xFF001F2A),
    background = ArcLightBackground,
    onBackground = ArcLightOnSurface,
    surface = ArcLightSurface,
    onSurface = ArcLightOnSurface,
    surfaceVariant = ArcLightSurfaceContainer,
    onSurfaceVariant = ArcLightOnSurfaceVariant,
    surfaceContainer = ArcLightSurfaceContainer,
    surfaceContainerHigh = ArcLightSurfaceContainerHigh,
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0)
)

@Composable
fun ArcWeatherTheme(
    themeMode: AppThemeMode = AppThemeMode.DYNAMIC,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.DYNAMIC -> systemInDark
    }

    val context = LocalContext.current
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        themeMode == AppThemeMode.DYNAMIC && supportsDynamic -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> ArcDarkColorScheme
        else -> ArcLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
