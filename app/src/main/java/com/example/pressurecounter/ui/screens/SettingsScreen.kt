package com.example.pressurecounter.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.pressurecounter.notifications.ReminderWorker
import com.example.pressurecounter.ui.viewmodel.MeasurementViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MeasurementViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    val totalCount by viewModel.totalCount.collectAsState()
    
    // Reminder settings
    var remindersEnabled by remember { 
        mutableStateOf(
            context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getBoolean("reminders_enabled", false)
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            remindersEnabled = true
            context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("reminders_enabled", true)
                .apply()
            ReminderWorker.scheduleReminder(context)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Reminders section
            Text(
                text = "Напоминания",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                ListItem(
                    headlineContent = { Text("Напоминания об измерениях") },
                    supportingContent = { Text("Напоминать каждые 12 часов") },
                    leadingContent = {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                    },
                    trailingContent = {
                        Switch(
                            checked = remindersEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        remindersEnabled = true
                                        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                                            .edit()
                                            .putBoolean("reminders_enabled", true)
                                            .apply()
                                        ReminderWorker.scheduleReminder(context)
                                    }
                                } else {
                                    remindersEnabled = false
                                    context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                                        .edit()
                                        .putBoolean("reminders_enabled", false)
                                        .apply()
                                    ReminderWorker.cancelReminder(context)
                                }
                            }
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Data section
            Text(
                text = "Данные",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    // Export data
                    ListItem(
                        headlineContent = { Text("Экспорт данных") },
                        supportingContent = { Text("Сохранить измерения в CSV файл") },
                        leadingContent = {
                            Icon(Icons.Default.Download, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        },
                        modifier = Modifier.clickable { showExportDialog = true }
                    )
                    
                    HorizontalDivider()
                    
                    // Share data
                    ListItem(
                        headlineContent = { Text("Поделиться данными") },
                        supportingContent = { Text("Отправить данные через другие приложения") },
                        leadingContent = {
                            Icon(Icons.Default.Share, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        },
                        modifier = Modifier.clickable {
                            shareData(context, viewModel)
                        }
                    )
                    
                    HorizontalDivider()
                    
                    // Delete all data
                    ListItem(
                        headlineContent = { 
                            Text(
                                "Удалить все данные",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        supportingContent = { 
                            Text(
                                "Всего записей: $totalCount",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.DeleteForever, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        modifier = Modifier.clickable { 
                            if (totalCount > 0) {
                                showDeleteAllDialog = true 
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // About section
            Text(
                text = "О приложении",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Версия") },
                        supportingContent = { Text("1.0.0") },
                        leadingContent = {
                            Icon(Icons.Default.Info, contentDescription = null)
                        }
                    )
                    
                    HorizontalDivider()
                    
                    ListItem(
                        headlineContent = { Text("Мониторинг давления") },
                        supportingContent = { 
                            Text("Приложение для отслеживания артериального давления и пульса") 
                        },
                        leadingContent = {
                            Icon(Icons.Default.Favorite, contentDescription = null)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Column {
                        Text(
                            text = "Важная информация",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Данное приложение не является медицинским прибором. " +
                                   "При проблемах со здоровьем обратитесь к врачу.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Delete all confirmation dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Удалить все данные?") },
            text = { 
                Text(
                    "Все $totalCount измерений будут удалены безвозвратно. " +
                    "Рекомендуем сначала экспортировать данные."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.allMeasurements.value.forEach {
                            viewModel.deleteMeasurement(it)
                        }
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить всё")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
    
    // Export dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            icon = { Icon(Icons.Default.Download, contentDescription = null) },
            title = { Text("Экспорт данных") },
            text = { 
                Text("Данные будут сохранены в формате CSV и доступны для отправки.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        exportAndShare(context, viewModel)
                        showExportDialog = false
                    }
                ) {
                    Text("Экспортировать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

private fun shareData(context: Context, viewModel: MeasurementViewModel) {
    val csvContent = viewModel.exportToCsv()
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Данные измерений давления")
        putExtra(Intent.EXTRA_TEXT, csvContent)
    }
    
    context.startActivity(Intent.createChooser(intent, "Поделиться данными"))
}

private fun exportAndShare(context: Context, viewModel: MeasurementViewModel) {
    val csvContent = viewModel.exportToCsv()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.forLanguageTag("ru"))
    val fileName = "pressure_data_${dateFormat.format(Date())}.csv"
    
    try {
        val file = File(context.cacheDir, fileName)
        file.writeText(csvContent)
        
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Экспортировать данные"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
