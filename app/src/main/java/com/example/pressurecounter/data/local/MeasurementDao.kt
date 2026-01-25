package com.example.pressurecounter.data.local

import androidx.room.*
import com.example.pressurecounter.data.model.Measurement
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    
    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<Measurement>>
    
    @Query("SELECT * FROM measurements WHERE id = :id")
    suspend fun getMeasurementById(id: Long): Measurement?
    
    @Query("SELECT * FROM measurements ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMeasurement(): Flow<Measurement?>
    
    @Query("SELECT * FROM measurements WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getMeasurementsBetween(startTime: Long, endTime: Long): Flow<List<Measurement>>
    
    @Query("SELECT * FROM measurements WHERE timestamp >= :startTime ORDER BY timestamp ASC")
    fun getMeasurementsFrom(startTime: Long): Flow<List<Measurement>>
    
    @Query("SELECT AVG(systolic) FROM measurements WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getAverageSystolic(startTime: Long, endTime: Long): Double?
    
    @Query("SELECT AVG(diastolic) FROM measurements WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getAverageDiastolic(startTime: Long, endTime: Long): Double?
    
    @Query("SELECT AVG(pulse) FROM measurements WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getAveragePulse(startTime: Long, endTime: Long): Double?
    
    @Query("SELECT MIN(systolic) FROM measurements WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getMinSystolic(startTime: Long, endTime: Long): Int?
    
    @Query("SELECT MAX(systolic) FROM measurements WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getMaxSystolic(startTime: Long, endTime: Long): Int?
    
    @Query("SELECT MIN(diastolic) FROM measurements WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getMinDiastolic(startTime: Long, endTime: Long): Int?
    
    @Query("SELECT MAX(diastolic) FROM measurements WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getMaxDiastolic(startTime: Long, endTime: Long): Int?
    
    @Query("SELECT MIN(pulse) FROM measurements WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getMinPulse(startTime: Long, endTime: Long): Int?
    
    @Query("SELECT MAX(pulse) FROM measurements WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getMaxPulse(startTime: Long, endTime: Long): Int?
    
    @Query("SELECT COUNT(*) FROM measurements")
    fun getTotalCount(): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: Measurement): Long
    
    @Update
    suspend fun updateMeasurement(measurement: Measurement)
    
    @Delete
    suspend fun deleteMeasurement(measurement: Measurement)
    
    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteMeasurementById(id: Long)
    
    @Query("DELETE FROM measurements")
    suspend fun deleteAllMeasurements()
}
