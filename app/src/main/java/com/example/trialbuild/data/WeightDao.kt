package com.example.trialbuild.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WeightEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(entry: WeightEntry)

    @Update
    suspend fun updateLog(entry: WeightEntry)

    @Delete
    suspend fun deleteLog(entry: WeightEntry)
}