package com.example.timerwheel

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: TimerViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212)),
                color = Color(0xFF121212)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    // Top Section: Digital Timer Display & Title (respects status bar / front camera cutout)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(
                            text = "TIMER WHEEL",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Timer Digits Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 24.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    TimeDisplayUnit(value = viewModel.hours, label = "JAM")
                                    Text(text = ":", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                                    TimeDisplayUnit(value = viewModel.minutes, label = "MENIT")
                                    Text(text = ":", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                                    TimeDisplayUnit(value = viewModel.seconds, label = "DETIK")
                                }

                                if (viewModel.isFinished) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "WAKTU HABIS! (TIME'S UP)",
                                        color = Color(0xFFFF453A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                } else if (viewModel.isRunning) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Timer Berjalan...",
                                        color = Color(0xFF30D158),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        if (!viewModel.isRunning && !viewModel.isFinished) {
                            TextButton(onClick = { viewModel.resetTimer() }) {
                                Text(text = "Reset Timer", color = Color(0xFF0A84FF))
                            }
                        }
                    }

                    // Bottom Section: iPod Click Wheel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IpodClickWheel(
                            isRunning = viewModel.isRunning,
                            isFinished = viewModel.isFinished,
                            onAdjustHours = { delta -> viewModel.adjustHours(delta) },
                            onAdjustMinutes = { delta -> viewModel.adjustMinutes(delta) },
                            onAdjustSeconds = { delta -> viewModel.adjustSeconds(delta) },
                            onStartStopClick = { viewModel.toggleStartStop() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeDisplayUnit(value: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = String.format(Locale.getDefault(), "%02d", value),
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
