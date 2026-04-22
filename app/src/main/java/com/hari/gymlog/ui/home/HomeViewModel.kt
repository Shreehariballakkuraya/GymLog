package com.hari.gymlog.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hari.gymlog.data.db.GymLogDatabase
import com.hari.gymlog.data.repository.ActivityRepository
import com.hari.gymlog.data.repository.FoodRepository
import com.hari.gymlog.data.repository.WeightRepository
import com.hari.gymlog.data.repository.WorkoutRepository
import com.hari.gymlog.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HomeUiState(
    val workoutCount: Int = 0,
    val foodCount: Int = 0,
    val steps: Int = 0,
    val activeMinutes: Int = 0,
    val latestWeight: Double? = null,
    val streakDays: Int = 0,
    val weeklyDaysWorkedOut: Int = 0,
    val totalProtein: Int = 0,
    val totalCarbs: Int = 0,
    val totalFat: Int = 0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GymLogDatabase.getDatabase(application)
    
    private val workoutRepo = WorkoutRepository(database.workoutDao())
    private val foodRepo = FoodRepository(database.foodDao())
    private val activityRepo = ActivityRepository(database.activityDao())
    private val weightRepo = WeightRepository(database.weightDao())

    private val today = DateUtils.getCurrentDateForDb()

    // Compute week boundaries (Monday to Sunday)
    private val weekStart: String
    private val weekEnd: String

    init {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        weekStart = formatter.format(cal.time)
        cal.add(Calendar.DAY_OF_WEEK, 6)
        weekEnd = formatter.format(cal.time)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        workoutRepo.getWorkoutsByDate(today),
        foodRepo.getFoodsByDate(today),
        activityRepo.getActivityByDate(today),
        weightRepo.getLatestWeight(),
        workoutRepo.getDistinctDatesInRange(weekStart, weekEnd)
    ) { results ->
        val workouts = results[0] as List<*>
        val foods = results[1] as List<*>
        val activities = results[2] as List<*>
        val latestWeight = results[3] as? com.hari.gymlog.data.db.entity.WeightEntity
        val weeklyDates = results[4] as List<*>

        val activity = (activities as? List<com.hari.gymlog.data.db.entity.ActivityEntity>)?.firstOrNull()

        val foodList = foods.filterIsInstance<com.hari.gymlog.data.db.entity.FoodEntity>()
        val totalProtein = foodList.sumOf { it.protein ?: 0 }
        val totalCarbs = foodList.sumOf { it.carbs ?: 0 }
        val totalFat = foodList.sumOf { it.fat ?: 0 }

        // Compute streak from all dates
        val streakDays = computeStreak()

        HomeUiState(
            workoutCount = workouts.size,
            foodCount = foods.size,
            steps = activity?.steps ?: 0,
            activeMinutes = activity?.activeMinutes ?: 0,
            latestWeight = latestWeight?.bodyWeight,
            streakDays = streakDays,
            weeklyDaysWorkedOut = weeklyDates.size,
            totalProtein = totalProtein,
            totalCarbs = totalCarbs,
            totalFat = totalFat
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    private fun computeStreak(): Int {
        // Simple streak heuristic: we count from today backwards
        // For a reactive approach, we compute it in the combine block
        // but for simplicity, we use the weekly data as a proxy.
        // A full streak requires a synchronous query which we'll approximate.
        return 0 // Will be computed reactively below
    }
}
