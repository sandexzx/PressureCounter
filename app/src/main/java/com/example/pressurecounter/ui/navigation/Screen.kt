package com.example.pressurecounter.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    object Home : Screen("home", "Главная", Icons.Default.Home)
    object Statistics : Screen("statistics", "Статистика", Icons.Default.BarChart)
    object History : Screen("history", "История", Icons.Default.History)
    object Settings : Screen("settings", "Настройки", Icons.Default.Settings)
    object AddMeasurement : Screen("add_measurement", "Добавить измерение")
    object EditMeasurement : Screen("edit_measurement/{measurementId}", "Редактировать") {
        fun createRoute(measurementId: Long) = "edit_measurement/$measurementId"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Statistics,
    Screen.History,
    Screen.Settings
)
