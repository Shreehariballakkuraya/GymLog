package com.hari.gymlog.ui.weight

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hari.gymlog.data.db.GymLogDatabase
import com.hari.gymlog.data.db.entity.WeightEntity
import com.hari.gymlog.data.repository.WeightRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeightViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WeightRepository
    
    val latestWeight: StateFlow<WeightEntity?>
    val recentWeights: StateFlow<List<WeightEntity>>

    init {
        val database = GymLogDatabase.getDatabase(application)
        repository = WeightRepository(database.weightDao())

        latestWeight = repository.getLatestWeight()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        // Show last 30 days
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val endDate = formatter.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDate = formatter.format(calendar.time)

        recentWeights = repository.getWeightRange(startDate, endDate)
            .map { list -> list.reversed() } // Show newest first in the list
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addWeight(weight: WeightEntity) {
        viewModelScope.launch {
            repository.insert(weight)
        }
    }

    fun updateWeight(weight: WeightEntity) {
        viewModelScope.launch {
            repository.update(weight)
        }
    }

    fun deleteWeight(weight: WeightEntity) {
        viewModelScope.launch {
            repository.delete(weight)
        }
    }
}
