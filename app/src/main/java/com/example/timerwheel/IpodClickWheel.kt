package com.example.timerwheel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.hypot

@Composable
fun IpodClickWheel(
    isRunning: Boolean,
    isFinished: Boolean,
    onAdjustHours: (Int) -> Unit,
    onAdjustMinutes: (Int) -> Unit,
    onAdjustSeconds: (Int) -> Unit,
    onStartStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val feedback = remember { WheelFeedback(context) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(320.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(320.dp)
                    .pointerInput(isRunning, isFinished) {
                        if (isRunning || isFinished) return@pointerInput
                        var activeRing: Int? = null // 1: Inner (sec), 2: Middle (hour), 3: Outer (min)
                        var lastAngle = 0f
                        var accumulatedAngle = 0f

                        detectDragGestures(
                            onDragStart = { offset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val distance = hypot(offset.x - center.x, offset.y - center.y)
                                val density = density
                                val distDp = distance / density

                                activeRing = when {
                                    distDp <= 55f -> null // center button area
                                    distDp <= 85f -> 1 // Inner (Seconds)
                                    distDp <= 115f -> 2 // Middle (Hours)
                                    distDp <= 150f -> 3 // Outer (Minutes)
                                    else -> null
                                }

                                if (activeRing != null) {
                                    lastAngle = atan2(offset.y - center.y, offset.x - center.x) * (180f / Math.PI).toFloat()
                                    accumulatedAngle = 0f
                                }
                            },
                            onDrag = { change, _ ->
                                val ring = activeRing
                                if (ring != null) {
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val currentAngle = atan2(change.position.y - center.y, change.position.x - center.x) * (180f / Math.PI).toFloat()
                                    var delta = currentAngle - lastAngle

                                    if (delta > 180f) delta -= 360f
                                    if (delta < -180f) delta += 360f

                                    accumulatedAngle += delta
                                    lastAngle = currentAngle

                                    val threshold = 18f
                                    if (accumulatedAngle >= threshold) {
                                        val steps = (accumulatedAngle / threshold).toInt()
                                        accumulatedAngle -= steps * threshold
                                        feedback.triggerFeedback()
                                        when (ring) {
                                            1 -> onAdjustSeconds(steps)
                                            2 -> onAdjustHours(steps)
                                            3 -> onAdjustMinutes(steps)
                                        }
                                    } else if (accumulatedAngle <= -threshold) {
                                        val steps = (accumulatedAngle / -threshold).toInt()
                                        accumulatedAngle += steps * threshold
                                        feedback.triggerFeedback()
                                        when (ring) {
                                            1 -> onAdjustSeconds(-steps)
                                            2 -> onAdjustHours(-steps)
                                            3 -> onAdjustMinutes(-steps)
                                        }
                                    }
                                }
                            },
                            onDragEnd = {
                                activeRing = null
                            },
                            onDragCancel = {
                                activeRing = null
                            }
                        )
                    }
                    .pointerInput(isRunning, isFinished) {
                        detectTapGestures(
                            onTap = { offset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val distance = hypot(offset.x - center.x, offset.y - center.y)
                                val density = density
                                val distDp = distance / density
                                if (distDp <= 55f) {
                                    feedback.triggerFeedback()
                                    onStartStopClick()
                                }
                            }
                        )
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = size.width / 2f

                val outerRadius = maxRadius * 0.95f
                val middleRadius = maxRadius * 0.72f
                val innerRadius = maxRadius * 0.48f
                val centerButtonRadius = maxRadius * 0.35f

                // Outer wheel (Minutes)
                drawCircle(
                    color = Color(0xFF2C2C2E),
                    radius = outerRadius,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF48484A),
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = 3f)
                )

                // Middle wheel (Hours)
                drawCircle(
                    color = Color(0xFF1C1C1E),
                    radius = middleRadius,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF48484A),
                    radius = middleRadius,
                    center = center,
                    style = Stroke(width = 3f)
                )

                // Inner wheel (Seconds)
                drawCircle(
                    color = Color(0xFF2C2C2E),
                    radius = innerRadius,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF48484A),
                    radius = innerRadius,
                    center = center,
                    style = Stroke(width = 3f)
                )

                // Center Button (Start/Stop/Alarm Stop)
                drawCircle(
                    color = if (isRunning || isFinished) Color(0xFFFF453A) else Color(0xFF30D158),
                    radius = centerButtonRadius,
                    center = center
                )
            }

            // Center Button Label
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when {
                        isFinished -> "STOP"
                        isRunning -> "STOP"
                        else -> "START"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Outer: Menit  |  Middle: Jam  |  Inner: Detik",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
