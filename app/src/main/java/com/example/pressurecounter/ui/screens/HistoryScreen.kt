package com.example.pressurecounter.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pressurecounter.data.model.Measurement
import com.example.pressurecounter.data.model.PressureCategory
import com.example.pressurecounter.ui.theme.*
import com.example.pressurecounter.ui.viewmodel.MeasurementViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MeasurementViewModel,
    onEditMeasurement: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val measurements by viewModel.allMeasurements.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Measurement?>(null) }
    
    // Group measurements by date
    val groupedMeasurements = remember(measurements) {
        measurements.groupBy { measurement ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = measurement.timestamp
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }.toSortedMap(compareByDescending { it })
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История измерений") }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (measurements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📋",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = "История пуста",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Добавьте первое измерение",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedMeasurements.forEach { (dateMillis, dayMeasurements) ->
                    // Date header
                    item(key = "header_$dateMillis") {
                        DateHeader(timestamp = dateMillis)
                    }
                    
                    // Measurements for this date
                    items(
                        items = dayMeasurements,
                        key = { it.id }
                    ) { measurement ->
                        SwipeableMeasurementItem(
                            measurement = measurement,
                            onEdit = { onEditMeasurement(measurement.id) },
                            onDelete = { showDeleteDialog = measurement }
                        )
                    }
                    
                    item(key = "spacer_$dateMillis") {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Удалить измерение?") },
            text = { 
                Text("Это действие нельзя отменить. Измерение будет удалено навсегда.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog?.let { viewModel.deleteMeasurement(it) }
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun DateHeader(timestamp: Long) {
    val today = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    
    val yesterday = remember { today - 24 * 60 * 60 * 1000 }
    
    val dateText = when {
        timestamp == today -> "Сегодня"
        timestamp == yesterday -> "Вчера"
        else -> {
            val sdf = SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("ru"))
            sdf.format(Date(timestamp))
        }
    }
    
    Text(
        text = dateText,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableMeasurementItem(
    measurement: Measurement,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false // Don't dismiss, show dialog first
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false
                }
                else -> false
            }
        }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    else -> Color.Transparent
                },
                label = "swipe_color"
            )
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                else -> null
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    ) {
        MeasurementListItem(
            measurement = measurement,
            onClick = onEdit
        )
    }
}

@Composable
fun MeasurementListItem(
    measurement: Measurement,
    onClick: () -> Unit
) {
    val categoryColor = when (measurement.pressureCategory) {
        PressureCategory.NORMAL -> PressureNormal
        PressureCategory.ELEVATED -> PressureElevated
        PressureCategory.HYPERTENSION_1 -> PressureHigh1
        PressureCategory.HYPERTENSION_2 -> PressureHigh2
        PressureCategory.HYPERTENSION_CRISIS -> PressureCrisis
        PressureCategory.HYPOTENSION -> PressureLow
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time
            Column {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.forLanguageTag("ru"))
                Text(
                    text = timeFormat.format(Date(measurement.timestamp)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = measurement.pressureCategory.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = categoryColor
                )
            }
            
            // Pressure
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${measurement.systolic}/${measurement.diastolic}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                    Text(
                        text = "мм рт.ст.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${measurement.pulse}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ChartPulse
                    )
                    Text(
                        text = "пульс",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = measurement.feeling.emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}
