package com.example.pressurecounter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Перечисление для выбора самочувствия
 */
enum class Feeling(val emoji: String, val description: String) {
    GREAT("😊", "Отлично"),
    GOOD("🙂", "Хорошо"),
    NORMAL("😐", "Нормально"),
    BAD("😞", "Плохо"),
    TERRIBLE("😫", "Ужасно")
}

/**
 * Категория давления на основе значений
 */
enum class PressureCategory(val color: Long, val description: String) {
    HYPOTENSION(0xFF2196F3, "Пониженное"),       // Синий
    NORMAL(0xFF4CAF50, "Норма"),                  // Зеленый
    ELEVATED(0xFFFFEB3B, "Повышенное"),           // Желтый
    HYPERTENSION_1(0xFFFF9800, "Гипертония 1 ст."), // Оранжевый
    HYPERTENSION_2(0xFFF44336, "Гипертония 2 ст."), // Красный
    HYPERTENSION_CRISIS(0xFF9C27B0, "Криз")       // Фиолетовый
}

/**
 * Сущность для хранения измерений давления и пульса
 */
@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Систолическое давление (верхнее) */
    val systolic: Int,
    
    /** Диастолическое давление (нижнее) */
    val diastolic: Int,
    
    /** Пульс */
    val pulse: Int,
    
    /** Время измерения в миллисекундах */
    val timestamp: Long = System.currentTimeMillis(),
    
    /** Заметки (опционально) */
    val notes: String = "",
    
    /** Самочувствие */
    val feeling: Feeling = Feeling.NORMAL
) {
    /**
     * Пульсовое давление (разница между систолическим и диастолическим)
     */
    val pulsePressure: Int
        get() = systolic - diastolic
    
    /**
     * Определяет категорию давления на основе значений
     */
    val pressureCategory: PressureCategory
        get() = when {
            systolic < 90 || diastolic < 60 -> PressureCategory.HYPOTENSION
            systolic < 120 && diastolic < 80 -> PressureCategory.NORMAL
            systolic in 120..129 && diastolic < 80 -> PressureCategory.ELEVATED
            systolic in 130..139 || diastolic in 80..89 -> PressureCategory.HYPERTENSION_1
            systolic in 140..179 || diastolic in 90..119 -> PressureCategory.HYPERTENSION_2
            else -> PressureCategory.HYPERTENSION_CRISIS
        }
}
