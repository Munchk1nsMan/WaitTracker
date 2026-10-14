package com.example.trialbuild.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_logs")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weightKg: Float,
    val timestamp: Long = System.currentTimeMillis()
)