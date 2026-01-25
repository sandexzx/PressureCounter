package com.example.pressurecounter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pressurecounter.ui.screens.*
import com.example.pressurecounter.ui.viewmodel.MeasurementViewModel

@Composable
fun PressureNavHost(
    navController: NavHostController,
    viewModel: MeasurementViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {
                    navController.navigate(Screen.AddMeasurement.route)
                }
            )
        }
        
        composable(Screen.Statistics.route) {
            StatisticsScreen(viewModel = viewModel)
        }
        
        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                onEditMeasurement = { measurementId ->
                    navController.navigate(Screen.EditMeasurement.createRoute(measurementId))
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = viewModel)
        }
        
        composable(Screen.AddMeasurement.route) {
            AddEditMeasurementScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.EditMeasurement.route,
            arguments = listOf(
                navArgument("measurementId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val measurementId = backStackEntry.arguments?.getLong("measurementId")
            AddEditMeasurementScreen(
                viewModel = viewModel,
                measurementId = measurementId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
