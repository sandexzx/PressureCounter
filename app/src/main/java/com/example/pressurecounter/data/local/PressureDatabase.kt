package com.example.pressurecounter.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pressurecounter.data.model.Measurement

@Database(
    entities = [Measurement::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PressureDatabase : RoomDatabase() {
    
    abstract fun measurementDao(): MeasurementDao
    
    companion object {
        @Volatile
        private var INSTANCE: PressureDatabase? = null
        
        fun getDatabase(context: Context): PressureDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PressureDatabase::class.java,
                    "pressure_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
