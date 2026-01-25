package com.example.pressurecounter.data.repository

import com.example.pressurecounter.data.local.MeasurementDao
import com.example.pressurecounter.data.model.Measurement
import kotlinx.coroutines.flow.Flow
import java.util.*

class MeasurementRepository(private val measurementDao: MeasurementDao) {
    
    val allMeasurements: Flow<List<Measurement>> = measurementDao.getAllMeasurements()
    
    val latestMeasurement: Flow<Measurement?> = measurementDao.getLatestMeasurement()
    
    val totalCount: Flow<Int> = measurementDao.getTotalCount()
    
    fun getMeasurementsBetween(startTime: Long, endTime: Long): Flow<List<Measurement>> {
        return measurementDao.getMeasurementsBetween(startTime, endTime)
    }
    
    fun getMeasurementsFrom(startTime: Long): Flow<List<Measurement>> {
        return measurementDao.getMeasurementsFrom(startTime)
    }
    
    fun getWeekMeasurements(): Flow<List<Measurement>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        return measurementDao.getMeasurementsFrom(calendar.timeInMillis)
    }
    
    fun getMonthMeasurements(): Flow<List<Measurement>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        return measurementDao.getMeasurementsFrom(calendar.timeInMillis)
    }
    
    fun getYearMeasurements(): Flow<List<Measurement>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -1)
        return measurementDao.getMeasurementsFrom(calendar.timeInMillis)
    }
    
    suspend fun getMeasurementById(id: Long): Measurement? {
        return measurementDao.getMeasurementById(id)
    }
    
    suspend fun insertMeasurement(measurement: Measurement): Long {
        return measurementDao.insertMeasurement(measurement)
    }
    
    suspend fun updateMeasurement(measurement: Measurement) {
        measurementDao.updateMeasurement(measurement)
    }
    
    suspend fun deleteMeasurement(measurement: Measurement) {
        measurementDao.deleteMeasurement(measurement)
    }
    
    suspend fun deleteMeasurementById(id: Long) {
        measurementDao.deleteMeasurementById(id)
    }
    
    suspend fun deleteAllMeasurements() {
        measurementDao.deleteAllMeasurements()
    }
    
    suspend fun getStatistics(startTime: Long, endTime: Long): Statistics {
        return Statistics(
            avgSystolic = measurementDao.getAverageSystolic(startTime, endTime),
            avgDiastolic = measurementDao.getAverageDiastolic(startTime, endTime),
            avgPulse = measurementDao.getAveragePulse(startTime, endTime),
            minSystolic = measurementDao.getMinSystolic(startTime, endTime),
            maxSystolic = measurementDao.getMaxSystolic(startTime, endTime),
            minDiastolic = measurementDao.getMinDiastolic(startTime, endTime),
            maxDiastolic = measurementDao.getMaxDiastolic(startTime, endTime),
            minPulse = measurementDao.getMinPulse(startTime, endTime),
            maxPulse = measurementDao.getMaxPulse(startTime, endTime)
        )
    }
}

data class Statistics(
    val avgSystolic: Double?,
    val avgDiastolic: Double?,
    val avgPulse: Double?,
    val minSystolic: Int?,
    val maxSystolic: Int?,
    val minDiastolic: Int?,
    val maxDiastolic: Int?,
    val minPulse: Int?,
    val maxPulse: Int?
)
