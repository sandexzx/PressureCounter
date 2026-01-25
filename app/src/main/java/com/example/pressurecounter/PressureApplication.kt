package com.example.pressurecounter

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.pressurecounter.data.local.PressureDatabase
import com.example.pressurecounter.data.repository.MeasurementRepository

class PressureApplication : Application() {
    
    val database by lazy { PressureDatabase.getDatabase(this) }
    val repository by lazy { MeasurementRepository(database.measurementDao()) }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Напоминания об измерениях",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Напоминания о необходимости измерить давление"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        const val REMINDER_CHANNEL_ID = "pressure_reminder_channel"
    }
}
