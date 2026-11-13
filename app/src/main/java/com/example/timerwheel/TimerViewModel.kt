package com.example.timerwheel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)

    var hours by mutableStateOf(prefs.getInt("saved_hours", 0))
        private set
    var minutes by mutableStateOf(prefs.getInt("saved_minutes", 1))
        private set
    var seconds by mutableStateOf(prefs.getInt("saved_seconds", 0))
        private set

    var totalRemainingSeconds by mutableStateOf(60)
        private set

    var isRunning by mutableStateOf(false)
        private set

    var isFinished by mutableStateOf(false)
        private set

    private var timerJob: Job? = null

    init {
        updateTotalSeconds()
    }

    private fun updateTotalSeconds() {
        totalRemainingSeconds = hours * 3600 + minutes * 60 + seconds
    }

    private fun saveTimePreferences() {
        prefs.edit().apply {
            putInt("saved_hours", hours)
            putInt("saved_minutes", minutes)
            putInt("saved_seconds", seconds)
            apply()
        }
    }

    fun adjustHours(delta: Int) {
        if (isRunning || isFinished) return
        hours = (hours + delta).coerceIn(0, 99)
        updateTotalSeconds()
        saveTimePreferences()
    }

    fun adjustMinutes(delta: Int) {
        if (isRunning || isFinished) return
        minutes = (minutes + delta).coerceIn(0, 59)
        updateTotalSeconds()
        saveTimePreferences()
    }

    fun adjustSeconds(delta: Int) {
        if (isRunning || isFinished) return
        seconds = (seconds + delta).coerceIn(0, 59)
        updateTotalSeconds()
        saveTimePreferences()
    }

    fun setTime(h: Int, m: Int, s: Int) {
        if (isRunning || isFinished) return
        hours = h.coerceIn(0, 99)
        minutes = m.coerceIn(0, 59)
        seconds = s.coerceIn(0, 59)
        updateTotalSeconds()
        saveTimePreferences()
    }

    fun toggleStartStop() {
        if (isFinished) {
            // Turn off alarm and reset
            resetTimer()
            return
        }
        if (isRunning) {
            pauseTimer()
        } else {
            if (totalRemainingSeconds <= 0) return
            startTimer()
        }
    }

    private fun startTimer() {
        isRunning = true
        isFinished = false
        startForegroundService(false)
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && totalRemainingSeconds > 0 && isRunning) {
                delay(1000L)
                if (totalRemainingSeconds > 0) {
                    totalRemainingSeconds--
                    hours = totalRemainingSeconds / 3600
                    minutes = (totalRemainingSeconds % 3600) / 60
                    seconds = totalRemainingSeconds % 60
                    if (totalRemainingSeconds > 0) {
                        startForegroundService(false)
                    } else {
                        isRunning = false
                        isFinished = true
                        startForegroundService(true)
                        break
                    }
                }
            }
        }
    }

    fun pauseTimer() {
        isRunning = false
        timerJob?.cancel()
        stopForegroundService()
    }

    fun resetTimer() {
        pauseTimer()
        hours = prefs.getInt("saved_hours", 0)
        minutes = prefs.getInt("saved_minutes", 1)
        seconds = prefs.getInt("saved_seconds", 0)
        updateTotalSeconds()
        isFinished = false
        stopForegroundService()
    }

    private fun startForegroundService(isFinished: Boolean) {
        val context = getApplication<Application>()
        val intent = Intent(context, TimerService::class.java).apply {
            putExtra(TimerService.EXTRA_SECONDS, totalRemainingSeconds)
            putExtra(TimerService.EXTRA_FINISHED, isFinished)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopForegroundService() {
        val context = getApplication<Application>()
        val intent = Intent(context, TimerService::class.java)
        try {
            context.stopService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
