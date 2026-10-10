package com.example.ui.atmosphere

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.model.WeatherCategory
import com.example.data.model.WeatherCondition
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class AtmosphereColors(
    val top: Color,
    val middle: Color,
    val bottom: Color,
    val cloudColor: Color,
    val sunGlowColor: Color,
    val rainColor: Color
)

@Composable
fun WeatherAtmosphere(
    condition: WeatherCondition,
    isDay: Boolean,
    reduceEffects: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val atmosphereColors = remember(condition, isDay) {
        getAtmosphereColors(condition, isDay)
    }

    val bgImageRes = remember(condition, isDay) {
        when {
            !isDay -> R.drawable.img_bg_night
            condition.isRain || condition.isThunderstorm -> R.drawable.img_bg_rainy
            condition.category == WeatherCategory.CLOUDY || condition.category == WeatherCategory.FOG || condition.category == WeatherCategory.SNOW -> R.drawable.img_bg_cloudy
            else -> R.drawable.img_bg_clear_day
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "AtmosphereAnim")

    // Slow metamorphic cloud morphing phase (40s cycle)
    val cloudPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (reduceEffects) 120000 else 40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CloudPhase"
    )

    // Gentle cloud drift (50s cycle)
    val cloudDrift by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (reduceEffects) 100000 else 45000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CloudDrift"
    )

    // Rain drop cycle (1.2s cycle)
    val rainAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RainAnim"
    )

    // Safe, subtle ambient lightning flash (low intensity, never strobe)
    var lightningOpacity by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(condition.isThunderstorm, reduceEffects) {
        if (condition.isThunderstorm && !reduceEffects) {
            while (true) {
                // Wait 7 to 18 seconds between gentle flashes
                val delayTime = Random.nextLong(7000, 18000)
                delay(delayTime)

                // Smooth fade in
                lightningOpacity = 0.12f
                delay(90)
                lightningOpacity = 0.04f
                delay(70)
                lightningOpacity = 0.08f
                delay(120)
                lightningOpacity = 0f
            }
        } else {
            lightningOpacity = 0f
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Weather Photo Backdrop with smooth crossfade
        Crossfade(
            targetState = bgImageRes,
            animationSpec = tween(600),
            label = "WeatherBgCrossfade",
            modifier = Modifier.fillMaxSize()
        ) { resId ->
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Translucent Atmosphere Tint & Particle Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Base atmospheric vertical gradient overlay to guarantee UI contrast
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        atmosphereColors.top.copy(alpha = if (isDay) 0.50f else 0.65f),
                        atmosphereColors.middle.copy(alpha = if (isDay) 0.40f else 0.55f),
                        atmosphereColors.bottom.copy(alpha = if (isDay) 0.65f else 0.80f)
                    ),
                    startY = 0f,
                    endY = height
                ),
                size = size
            )

            // Subtle sun/moon ambient radial glow
            if (isDay && condition.category == WeatherCategory.CLEAR) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            atmosphereColors.sunGlowColor.copy(alpha = 0.28f),
                            atmosphereColors.sunGlowColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.75f, height * 0.15f),
                        radius = width * 0.7f
                    ),
                    center = Offset(width * 0.75f, height * 0.15f),
                    radius = width * 0.7f
                )
            } else if (!isDay) {
                // Subtle moon night glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF81D4FA).copy(alpha = 0.18f),
                            Color(0xFF4FC3F7).copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.8f, height * 0.12f),
                        radius = width * 0.55f
                    ),
                    center = Offset(width * 0.8f, height * 0.12f),
                    radius = width * 0.55f
                )
            }

            // Metamorphic Soft Clouds
            drawMetamorphicClouds(
                category = condition.category,
                cloudPhase = cloudPhase,
                cloudDrift = cloudDrift,
                cloudColor = atmosphereColors.cloudColor,
                isDay = isDay,
                reduceEffects = reduceEffects
            )

            // 4. Draw gentle rain streaks if raining
            if (condition.isRain && !reduceEffects) {
                drawSubtleRain(
                    rainAnim = rainAnim,
                    rainColor = atmosphereColors.rainColor,
                    intensity = if (condition.category == WeatherCategory.DRIZZLE) 18 else 32
                )
            }

            // 5. Draw safe ambient lightning flash overlay
            if (lightningOpacity > 0f) {
                drawRect(
                    color = Color.White.copy(alpha = lightningOpacity),
                    size = size
                )
            }
        }

        // Screen content sits smoothly on top of atmospheric canvas
        content()
    }
}

private fun DrawScope.drawMetamorphicClouds(
    category: WeatherCategory,
    cloudPhase: Float,
    cloudDrift: Float,
    cloudColor: Color,
    isDay: Boolean,
    reduceEffects: Boolean
) {
    if (category == WeatherCategory.CLEAR && isDay) return

    val width = size.width
    val height = size.height

    val baseAlpha = when (category) {
        WeatherCategory.CLEAR -> 0.04f
        WeatherCategory.PARTLY_CLOUDY -> 0.12f
        WeatherCategory.CLOUDY -> 0.22f
        WeatherCategory.FOG -> 0.28f
        WeatherCategory.DRIZZLE, WeatherCategory.RAIN, WeatherCategory.SHOWERS -> 0.18f
        WeatherCategory.THUNDERSTORM -> 0.26f
        WeatherCategory.SNOW -> 0.20f
    }

    val morphOffset1 = sin(cloudPhase) * 20f
    val morphOffset2 = cos(cloudPhase * 1.3f) * 25f

    // Cloud layer 1 (Upper atmospheric mass)
    val path1 = Path().apply {
        moveTo(-50f + cloudDrift, height * 0.08f)
        cubicTo(
            width * 0.25f, height * 0.05f + morphOffset1,
            width * 0.55f, height * 0.16f - morphOffset2,
            width * 0.85f, height * 0.07f + morphOffset1
        )
        cubicTo(
            width * 1.05f, height * 0.02f,
            width * 1.15f + cloudDrift, height * 0.18f,
            width + 60f, height * 0.22f
        )
        lineTo(width + 60f, 0f)
        lineTo(-50f, 0f)
        close()
    }

    drawPath(
        path = path1,
        brush = Brush.verticalGradient(
            colors = listOf(
                cloudColor.copy(alpha = baseAlpha * 1.3f),
                cloudColor.copy(alpha = baseAlpha * 0.6f),
                Color.Transparent
            ),
            startY = 0f,
            endY = height * 0.24f
        )
    )

    // Cloud layer 2 (Mid-screen soft volumetric bank)
    if (category != WeatherCategory.PARTLY_CLOUDY) {
        val path2 = Path().apply {
            moveTo(-40f, height * 0.22f + morphOffset2)
            cubicTo(
                width * 0.2f, height * 0.28f - morphOffset1,
                width * 0.6f, height * 0.19f + morphOffset2,
                width * 0.9f, height * 0.29f
            )
            cubicTo(
                width * 1.05f, height * 0.32f,
                width + 80f, height * 0.24f,
                width + 80f, 0f
            )
            lineTo(-40f, 0f)
            close()
        }

        drawPath(
            path = path2,
            brush = Brush.verticalGradient(
                colors = listOf(
                    cloudColor.copy(alpha = baseAlpha * 0.9f),
                    cloudColor.copy(alpha = baseAlpha * 0.3f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = height * 0.34f
            )
        )
    }
}

private fun DrawScope.drawSubtleRain(
    rainAnim: Float,
    rainColor: Color,
    intensity: Int
) {
    val width = size.width
    val height = size.height

    // Generate fixed deterministic streaks modulated by rainAnim
    for (i in 0 until intensity) {
        val seedX = ((i * 137.5f) % width)
        val speedMultiplier = 0.8f + (i % 5) * 0.15f
        val dropProgress = (rainAnim * speedMultiplier + (i * 0.05f)) % 1f

        val startX = seedX - (dropProgress * 30f)
        val startY = dropProgress * (height + 100f) - 50f
        val streakLength = 16f + (i % 4) * 6f

        val endX = startX - 8f
        val endY = startY + streakLength

        drawLine(
            color = rainColor.copy(alpha = 0.22f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 1.4f
        )
    }
}

private fun getAtmosphereColors(condition: WeatherCondition, isDay: Boolean): AtmosphereColors {
    return if (isDay) {
        when (condition.category) {
            WeatherCategory.CLEAR -> AtmosphereColors(
                top = Color(0xFF1E88E5), // Vivid clear daylight blue
                middle = Color(0xFF64B5F6),
                bottom = Color(0xFFE3F2FD),
                cloudColor = Color(0xFFFFFFFF),
                sunGlowColor = Color(0xFFFFD54F),
                rainColor = Color(0xFF90CAF9)
            )
            WeatherCategory.PARTLY_CLOUDY -> AtmosphereColors(
                top = Color(0xFF1976D2),
                middle = Color(0xFF78909C),
                bottom = Color(0xFFECEFF1),
                cloudColor = Color(0xFFFFFFFF),
                sunGlowColor = Color(0xFFFFE082),
                rainColor = Color(0xFF90CAF9)
            )
            WeatherCategory.CLOUDY -> AtmosphereColors(
                top = Color(0xFF455A64),
                middle = Color(0xFF607D8B),
                bottom = Color(0xFFCFD8DC),
                cloudColor = Color(0xFFECEFF1),
                sunGlowColor = Color(0xFFB0BEC5),
                rainColor = Color(0xFFB0BEC5)
            )
            WeatherCategory.FOG -> AtmosphereColors(
                top = Color(0xFF546E7A),
                middle = Color(0xFF78909C),
                bottom = Color(0xFFD6DBDF),
                cloudColor = Color(0xFFFAFAFA),
                sunGlowColor = Color(0xFFB0BEC5),
                rainColor = Color(0xFFB0BEC5)
            )
            WeatherCategory.DRIZZLE, WeatherCategory.SHOWERS -> AtmosphereColors(
                top = Color(0xFF263238),
                middle = Color(0xFF37474F),
                bottom = Color(0xFF78909C),
                cloudColor = Color(0xFFB0BEC5),
                sunGlowColor = Color(0xFFFFE082),
                rainColor = Color(0xFF81D4FA)
            )
            WeatherCategory.RAIN -> AtmosphereColors(
                top = Color(0xFF1A232F),
                middle = Color(0xFF263238),
                bottom = Color(0xFF455A64),
                cloudColor = Color(0xFF90A4AE),
                sunGlowColor = Color(0xFF78909C),
                rainColor = Color(0xFF81D4FA)
            )
            WeatherCategory.THUNDERSTORM -> AtmosphereColors(
                top = Color(0xFF101726),
                middle = Color(0xFF1E2235),
                bottom = Color(0xFF2E3440),
                cloudColor = Color(0xFF5C6B73),
                sunGlowColor = Color(0xFF7986CB),
                rainColor = Color(0xFF9FA8DA)
            )
            WeatherCategory.SNOW -> AtmosphereColors(
                top = Color(0xFF37474F),
                middle = Color(0xFF546E7A),
                bottom = Color(0xFFECEFF1),
                cloudColor = Color(0xFFFFFFFF),
                sunGlowColor = Color(0xFFECEFF1),
                rainColor = Color(0xFFFFFFFF)
            )
        }
    } else {
        // Night atmosphere
        when (condition.category) {
            WeatherCategory.CLEAR -> AtmosphereColors(
                top = Color(0xFF070B14),
                middle = Color(0xFF0D1526),
                bottom = Color(0xFF131D33),
                cloudColor = Color(0xFF1E293B),
                sunGlowColor = Color(0xFF81D4FA),
                rainColor = Color(0xFF64B5F6)
            )
            WeatherCategory.THUNDERSTORM -> AtmosphereColors(
                top = Color(0xFF04060B),
                middle = Color(0xFF0B0F1C),
                bottom = Color(0xFF181B2B),
                cloudColor = Color(0xFF202336),
                sunGlowColor = Color(0xFF5C6BC0),
                rainColor = Color(0xFF7986CB)
            )
            WeatherCategory.RAIN, WeatherCategory.SHOWERS, WeatherCategory.DRIZZLE -> AtmosphereColors(
                top = Color(0xFF080C14),
                middle = Color(0xFF101722),
                bottom = Color(0xFF1A2332),
                cloudColor = Color(0xFF253346),
                sunGlowColor = Color(0xFF4FC3F7),
                rainColor = Color(0xFF81D4FA)
            )
            else -> AtmosphereColors(
                top = Color(0xFF090E17),
                middle = Color(0xFF111827),
                bottom = Color(0xFF1A2234),
                cloudColor = Color(0xFF222D3D),
                sunGlowColor = Color(0xFF90CAF9),
                rainColor = Color(0xFF90CAF9)
            )
        }
    }
}
