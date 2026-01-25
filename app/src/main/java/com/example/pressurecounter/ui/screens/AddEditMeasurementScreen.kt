package com.example.pressurecounter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pressurecounter.data.model.Feeling
import com.example.pressurecounter.data.model.Measurement
import com.example.pressurecounter.ui.viewmodel.MeasurementViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMeasurementScreen(
    viewModel: MeasurementViewModel,
    measurementId: Long? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var systolic by remember { mutableStateOf("") }
    var diastolic by remember { mutableStateOf("") }
    var pulse by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedFeeling by remember { mutableStateOf(Feeling.NORMAL) }
    var selectedDateTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    var systolicError by remember { mutableStateOf<String?>(null) }
    var diastolicError by remember { mutableStateOf<String?>(null) }
    var pulseError by remember { mutableStateOf<String?>(null) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val isEditing = measurementId != null
    var isLoading by remember { mutableStateOf(isEditing) }
    
    // Load existing measurement if editing
    LaunchedEffect(measurementId) {
        if (measurementId != null) {
            viewModel.getMeasurementById(measurementId)?.let { measurement ->
                systolic = measurement.systolic.toString()
                diastolic = measurement.diastolic.toString()
                pulse = measurement.pulse.toString()
                notes = measurement.notes
                selectedFeeling = measurement.feeling
                selectedDateTime = measurement.timestamp
            }
            isLoading = false
        }
    }
    
    fun validateAndSave(): Boolean {
        var isValid = true
        
        val systolicValue = systolic.toIntOrNull()
        if (systolicValue == null || systolicValue < 50 || systolicValue > 300) {
            systolicError = "Введите значение от 50 до 300"
            isValid = false
        } else {
            systolicError = null
        }
        
        val diastolicValue = diastolic.toIntOrNull()
        if (diastolicValue == null || diastolicValue < 30 || diastolicValue > 200) {
            diastolicError = "Введите значение от 30 до 200"
            isValid = false
        } else {
            diastolicError = null
        }
        
        val pulseValue = pulse.toIntOrNull()
        if (pulseValue == null || pulseValue < 30 || pulseValue > 250) {
            pulseError = "Введите значение от 30 до 250"
            isValid = false
        } else {
            pulseError = null
        }
        
        if (isValid) {
            if (isEditing) {
                viewModel.updateMeasurement(
                    Measurement(
                        id = measurementId!!,
                        systolic = systolicValue!!,
                        diastolic = diastolicValue!!,
                        pulse = pulseValue!!,
                        timestamp = selectedDateTime,
                        notes = notes,
                        feeling = selectedFeeling
                    )
                )
            } else {
                viewModel.insertMeasurement(
                    systolic = systolicValue!!,
                    diastolic = diastolicValue!!,
                    pulse = pulseValue!!,
                    timestamp = selectedDateTime,
                    notes = notes,
                    feeling = selectedFeeling
                )
            }
        }
        
        return isValid
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Редактировать" else "Новое измерение") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (validateAndSave()) {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, "Сохранить")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Pressure inputs
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Показатели",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = systolic,
                                onValueChange = { 
                                    systolic = it.filter { c -> c.isDigit() }.take(3)
                                    systolicError = null
                                },
                                label = { Text("Верхнее") },
                                suffix = { Text("SYS") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = systolicError != null,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            
                            OutlinedTextField(
                                value = diastolic,
                                onValueChange = { 
                                    diastolic = it.filter { c -> c.isDigit() }.take(3)
                                    diastolicError = null
                                },
                                label = { Text("Нижнее") },
                                suffix = { Text("DIA") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = diastolicError != null,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        
                        OutlinedTextField(
                            value = pulse,
                            onValueChange = { 
                                pulse = it.filter { c -> c.isDigit() }.take(3)
                                pulseError = null
                            },
                            label = { Text("Пульс") },
                            suffix = { Text("уд/мин") },
                            leadingIcon = { Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.error) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = pulseError != null,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
                
                // Date and time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(dateFormat.format(Date(selectedDateTime)))
                    }
                    
                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(timeFormat.format(Date(selectedDateTime)))
                    }
                }
                
                // Feeling selection
                Card(
                     shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Самочувствие",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectableGroup(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Feeling.entries.forEach { feeling ->
                                Column(
                                    modifier = Modifier
                                        .selectable(
                                            selected = selectedFeeling == feeling,
                                            onClick = { selectedFeeling = feeling },
                                            role = Role.RadioButton
                                        )
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = feeling.emoji,
                                        fontSize = if (selectedFeeling == feeling) 32.sp else 24.sp
                                    )
                                    if (selectedFeeling == feeling) {
                                        Text(
                                            text = feeling.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Заметки") },
                    placeholder = { Text("Опционально: лекарства, активность...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 4
                )
                
                // Save button
                Button(
                    onClick = {
                        if (validateAndSave()) {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isEditing) "Сохранить изменения" else "Сохранить",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateTime
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { newDate ->
                            val calendar = Calendar.getInstance()
                            val oldCalendar = Calendar.getInstance().apply { 
                                timeInMillis = selectedDateTime 
                            }
                            calendar.timeInMillis = newDate
                            calendar.set(Calendar.HOUR_OF_DAY, oldCalendar.get(Calendar.HOUR_OF_DAY))
                            calendar.set(Calendar.MINUTE, oldCalendar.get(Calendar.MINUTE))
                            selectedDateTime = calendar.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateTime }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Выберите время") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newCalendar = Calendar.getInstance().apply {
                            timeInMillis = selectedDateTime
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                        }
                        selectedDateTime = newCalendar.timeInMillis
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
