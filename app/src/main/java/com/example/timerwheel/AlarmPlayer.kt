package com.example.timerwheel

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager

class AlarmPlayer(private val context: Context) {
    private var ringtone: Ringtone? = null

    fun playAlarm() {
        try {
            stopAlarm()
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(context, alarmUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAlarm() {
        try {
            ringtone?.stop()
            ringtone = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
