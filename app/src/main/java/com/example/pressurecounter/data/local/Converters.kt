package com.example.pressurecounter.data.local

import androidx.room.TypeConverter
import com.example.pressurecounter.data.model.Feeling

class Converters {
    
    @TypeConverter
    fun fromFeeling(feeling: Feeling): String {
        return feeling.name
    }
    
    @TypeConverter
    fun toFeeling(value: String): Feeling {
        return try {
            Feeling.valueOf(value)
        } catch (e: IllegalArgumentException) {
            Feeling.NORMAL
        }
    }
}
