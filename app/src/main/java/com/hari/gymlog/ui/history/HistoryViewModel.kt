package com.hari.gymlog.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hari.gymlog.data.db.GymLogDatabase
import com.hari.gymlog.data.repository.ActivityRepository
import com.hari.gymlog.data.repository.FoodRepository
import com.hari.gymlog.data.repository.WeightRepository
import com.hari.gymlog.data.repository.WorkoutRepository
import com.hari.gymlog.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(
    val selectedDate: String = "",
    val workoutsLogged: Int = 0,
    val mealsLogged: Int = 0,
    val steps: Int = 0,
    val activeMinutes: Int = 0,
    val weight: Double? = null
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val workoutRepo: WorkoutRepository
    private val foodRepo: FoodRepository
    private val activityRepo: ActivityRepository
    private val weightRepo: WeightRepository

    private val selectedDateFlow = MutableStateFlow(DateUtils.getCurrentDateForDb())

    val uiState: StateFlow<HistoryUiState>

    init {
        val database = GymLogDatabase.getDatabase(application)
        workoutRepo = WorkoutRepository(database.workoutDao())
        foodRepo = FoodRepository(database.foodDao())
        activityRepo = ActivityRepository(database.activityDao())
        weightRepo = WeightRepository(database.weightDao())

        uiState = selectedDateFlow.flatMapLatest { date ->
            combine(
                workoutRepo.getWorkoutsByDate(date),
                foodRepo.getFoodsByDate(date),
                activityRepo.getActivityByDate(date),
                weightRepo.getWeightByDate(date) // Note: getWeightByDate returns list, we want first
            ) { workouts, foods, activityList, weightList ->
                
                val activity = activityList.firstOrNull()
                val weight = weightList.firstOrNull()

                HistoryUiState(
                    selectedDate = date,
                    workoutsLogged = workouts.size,
                    mealsLogged = foods.size,
                    steps = activity?.steps ?: 0,
                    activeMinutes = activity?.activeMinutes ?: 0,
                    weight = weight?.bodyWeight
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState()
        )
    }

    fun setSelectedDate(year: Int, month: Int, dayOfMonth: Int) {
        // month is 0-indexed in Android CalendarView (0 = January)
        val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        selectedDateFlow.value = formattedDate
    }
}
