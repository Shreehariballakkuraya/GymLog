package com.hari.gymlog.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hari.gymlog.data.db.GymLogDatabase
import com.hari.gymlog.data.db.entity.ActivityEntity
import com.hari.gymlog.data.db.entity.WeightEntity
import com.hari.gymlog.data.repository.ActivityRepository
import com.hari.gymlog.data.repository.WeightRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val activityRepo: ActivityRepository
    private val weightRepo: WeightRepository

    val activeData: StateFlow<List<ActivityEntity>>
    val weightData: StateFlow<List<WeightEntity>>

    init {
        val database = GymLogDatabase.getDatabase(application)
        activityRepo = ActivityRepository(database.activityDao())
        weightRepo = WeightRepository(database.weightDao())

        // Calculate a 30 day window string array
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val endDate = formatter.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDate = formatter.format(calendar.time)

        activeData = activityRepo.getActivityRange(startDate, endDate)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        weightData = weightRepo.getWeightRange(startDate, endDate)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
}
