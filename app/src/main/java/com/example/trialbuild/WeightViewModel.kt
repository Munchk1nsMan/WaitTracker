package com.example.trialbuild

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trialbuild.data.AppDatabase
import com.example.trialbuild.data.WeightEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

data class GraphDataPoint(
    val dateLabel: String,
    val rawWeight: Float,
    val rollingAvg: Float?
)

class WeightViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).weightDao()

    val logs: StateFlow<List<WeightEntry>> = dao.getAllLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val graphData: StateFlow<List<GraphDataPoint>> = logs.map { list ->
        val sortedList = list.sortedBy { it.timestamp }
        val sevenDaysMs = 7 * 24 * 60 * 60 * 1000L

        sortedList.mapIndexed { _, current ->
            val windowStart = current.timestamp - sevenDaysMs
            val windowEntries = sortedList.filter { it.timestamp in windowStart..current.timestamp }
            val rollingAvg = if (windowEntries.isNotEmpty()) {
                windowEntries.map { it.weightKg }.average().toFloat()
            } else null

            val dateStr = SimpleDateFormat("dd/MM", Locale.getDefault())
                .format(java.util.Date(current.timestamp))

            GraphDataPoint(dateStr, current.weightKg, rollingAvg)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addWeight(weight: Float) {
        viewModelScope.launch {
            dao.insertLog(WeightEntry(weightKg = weight))
        }
    }

    fun updateWeight(entry: WeightEntry, newWeight: Float) {
        viewModelScope.launch {
            dao.updateLog(entry.copy(weightKg = newWeight))
        }
    }

    fun deleteWeight(entry: WeightEntry) {
        viewModelScope.launch {
            dao.deleteLog(entry)
        }
    }
}