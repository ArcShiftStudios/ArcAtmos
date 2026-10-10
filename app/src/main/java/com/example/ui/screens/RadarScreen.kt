package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserLocation
import kotlin.math.cos
import kotlin.math.sin

enum class RadarLayer(val label: String) {
    PRECIPITATION("Precipitation"),
    TEMPERATURE("Temperature"),
    WIND("Wind Flow"),
    CLOUDS("Cloud Cover")
}

@Composable
fun RadarScreen(
    currentLocation: UserLocation,
    modifier: Modifier = Modifier
) {
    var selectedLayer by remember { mutableStateOf(RadarLayer.PRECIPITATION) }
    var isPlaying by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweepAnim")
    val radarPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarSweepPhase"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 96.dp)
    ) {
        // Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "WEATHER RADAR",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Regional atmospheric scan for ${currentLocation.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Layers,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Layer selection chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadarLayer.entries.forEach { layer ->
                val isSelected = layer == selectedLayer
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainer
                        )
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedLayer = layer }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = layer.label.split(" ").first(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive Radar Canvas Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("radar_viewport_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F172A)
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2
                    val cy = h / 2

                    // Grid lines & Range rings
                    val ringColor = Color(0xFF1E293B)
                    drawCircle(color = ringColor, radius = w * 0.2f, center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
                    drawCircle(color = ringColor, radius = w * 0.35f, center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
                    drawCircle(color = ringColor, radius = w * 0.48f, center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))

                    drawLine(color = ringColor, start = Offset(cx, 0f), end = Offset(cx, h), strokeWidth = 1f)
                    drawLine(color = ringColor, start = Offset(0f, cy), end = Offset(w, cy), strokeWidth = 1f)

                    // Layer visualization
                    when (selectedLayer) {
                        RadarLayer.PRECIPITATION -> {
                            // Precipitation echo clusters
                            for (i in 0..4) {
                                val angle = (i * 1.25f) + (if (isPlaying) radarPhase * 0.2f else 0f)
                                val dist = w * (0.18f + (i * 0.06f))
                                val px = cx + cos(angle) * dist
                                val py = cy + sin(angle) * dist
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF00E676).copy(alpha = 0.65f),
                                            Color(0xFF29B6F6).copy(alpha = 0.45f),
                                            Color.Transparent
                                        ),
                                        center = Offset(px, py),
                                        radius = 55.dp.toPx()
                                    ),
                                    center = Offset(px, py),
                                    radius = 55.dp.toPx()
                                )
                            }
                        }
                        RadarLayer.TEMPERATURE -> {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF9800).copy(alpha = 0.45f),
                                        Color(0xFF00E5FF).copy(alpha = 0.25f),
                                        Color.Transparent
                                    ),
                                    center = Offset(cx, cy),
                                    radius = w * 0.6f
                                ),
                                center = Offset(cx, cy),
                                radius = w * 0.6f
                            )
                        }
                        RadarLayer.WIND -> {
                            // Wind vector streamlines
                            for (row in -2..2) {
                                for (col in -2..2) {
                                    val ox = cx + col * 70.dp.toPx()
                                    val oy = cy + row * 70.dp.toPx()
                                    val streamAngle = 0.6f + sin(radarPhase + row) * 0.2f
                                    val ex = ox + cos(streamAngle) * 35.dp.toPx()
                                    val ey = oy + sin(streamAngle) * 35.dp.toPx()
                                    drawLine(
                                        color = Color(0xFF64B5F6).copy(alpha = 0.5f),
                                        start = Offset(ox, oy),
                                        end = Offset(ex, ey),
                                        strokeWidth = 2f
                                    )
                                }
                            }
                        }
                        RadarLayer.CLOUDS -> {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.45f),
                                        Color(0xFF90A4AE).copy(alpha = 0.25f),
                                        Color.Transparent
                                    ),
                                    center = Offset(cx + sin(radarPhase) * 30f, cy),
                                    radius = w * 0.5f
                                ),
                                center = Offset(cx + sin(radarPhase) * 30f, cy),
                                radius = w * 0.5f
                            )
                        }
                    }

                    // Location pin at center
                    drawCircle(
                        color = Color(0xFF38BDF8),
                        radius = 8f,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = 0.35f),
                        radius = 18f,
                        center = Offset(cx, cy)
                    )
                }

                // Controls overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isPlaying = !isPlaying }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (isPlaying) "Live Radar Loop (Past 60 min)" else "Paused",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
        }
    }
}
