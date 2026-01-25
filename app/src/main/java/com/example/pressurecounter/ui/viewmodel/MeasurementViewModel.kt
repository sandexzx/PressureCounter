package com.example.pressurecounter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pressurecounter.data.model.Feeling
import com.example.pressurecounter.data.model.Measurement
import com.example.pressurecounter.data.repository.MeasurementRepository
import com.example.pressurecounter.data.repository.Statistics
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class MeasurementViewModel(private val repository: MeasurementRepository) : ViewModel() {
    
    val allMeasurements: StateFlow<List<Measurement>> = repository.allMeasurements
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val latestMeasurement: StateFlow<Measurement?> = repository.latestMeasurement
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    
    val weekMeasurements: StateFlow<List<Measurement>> = repository.getWeekMeasurements()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val monthMeasurements: StateFlow<List<Measurement>> = repository.getMonthMeasurements()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val totalCount: StateFlow<Int> = repository.totalCount
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)
    
    private val _weekStatistics = MutableStateFlow<Statistics?>(null)
    val weekStatistics: StateFlow<Statistics?> = _weekStatistics
    
    private val _monthStatistics = MutableStateFlow<Statistics?>(null)
    val monthStatistics: StateFlow<Statistics?> = _monthStatistics
    
    private val _yearStatistics = MutableStateFlow<Statistics?>(null)
    val yearStatistics: StateFlow<Statistics?> = _yearStatistics
    
    private val _editingMeasurement = MutableStateFlow<Measurement?>(null)
    val editingMeasurement: StateFlow<Measurement?> = _editingMeasurement
    
    init {
        loadStatistics()
    }
    
    fun loadStatistics() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            
            // Week statistics
            calendar.timeInMillis = now
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            _weekStatistics.value = repository.getStatistics(calendar.timeInMillis, now)
            
            // Month statistics
            calendar.timeInMillis = now
            calendar.add(Calendar.MONTH, -1)
            _monthStatistics.value = repository.getStatistics(calendar.timeInMillis, now)
            
            // Year statistics
            calendar.timeInMillis = now
            calendar.add(Calendar.YEAR, -1)
            _yearStatistics.value = repository.getStatistics(calendar.timeInMillis, now)
        }
    }
    
    fun insertMeasurement(
        systolic: Int,
        diastolic: Int,
        pulse: Int,
        timestamp: Long = System.currentTimeMillis(),
        notes: String = "",
        feeling: Feeling = Feeling.NORMAL
    ) {
        viewModelScope.launch {
            val measurement = Measurement(
                systolic = systolic,
                diastolic = diastolic,
                pulse = pulse,
                timestamp = timestamp,
                notes = notes,
                feeling = feeling
            )
            repository.insertMeasurement(measurement)
            loadStatistics()
        }
    }
    
    fun updateMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            repository.updateMeasurement(measurement)
            loadStatistics()
        }
    }
    
    fun deleteMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            repository.deleteMeasurement(measurement)
            loadStatistics()
        }
    }
    
    fun deleteMeasurementById(id: Long) {
        viewModelScope.launch {
            repository.deleteMeasurementById(id)
            loadStatistics()
        }
    }
    
    fun setEditingMeasurement(measurement: Measurement?) {
        _editingMeasurement.value = measurement
    }
    
    suspend fun getMeasurementById(id: Long): Measurement? {
        return repository.getMeasurementById(id)
    }
    
    fun exportToCsv(): String {
        val measurements = allMeasurements.value
        val sb = StringBuilder()
        sb.appendLine("Дата,Время,Систолическое,Диастолическое,Пульс,Пульсовое давление,Самочувствие,Заметки")
        
        val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.forLanguageTag("ru"))
        val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.forLanguageTag("ru"))
        
        measurements.forEach { m ->
            val date = Date(m.timestamp)
            sb.appendLine(
                "${dateFormat.format(date)}," +
                "${timeFormat.format(date)}," +
                "${m.systolic}," +
                "${m.diastolic}," +
                "${m.pulse}," +
                "${m.pulsePressure}," +
                "${m.feeling.description}," +
                "\"${m.notes.replace("\"", "\"\"")}\""
            )
        }
        
        return sb.toString()
    }
}

class MeasurementViewModelFactory(private val repository: MeasurementRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeasurementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MeasurementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
