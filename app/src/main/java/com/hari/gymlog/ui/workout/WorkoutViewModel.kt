package com.hari.gymlog.ui.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hari.gymlog.data.db.GymLogDatabase
import com.hari.gymlog.data.db.entity.WorkoutEntity
import com.hari.gymlog.data.db.entity.WorkoutTemplateEntity
import com.hari.gymlog.data.repository.WorkoutRepository
import com.hari.gymlog.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GymLogDatabase.getDatabase(application)
    private val repository: WorkoutRepository = WorkoutRepository(database.workoutDao())
    private val templateDao = database.workoutTemplateDao()
    
    private val currentDateFlow = MutableStateFlow(DateUtils.getCurrentDateForDb())

    val workouts: StateFlow<List<WorkoutEntity>>
    val templates: StateFlow<List<WorkoutTemplateEntity>>

    // For PR detection result communication
    val prDetected = MutableStateFlow<String?>(null)

    init {
        workouts = currentDateFlow
            .flatMapLatest { date -> repository.getWorkoutsByDate(date) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        templates = templateDao.getAllTemplates()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun setDate(date: String) {
        currentDateFlow.value = date
    }

    fun addWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            // PR Detection: check if this weight is higher than historical max
            val currentMax = repository.getMaxWeightForExercise(workout.exerciseName)
            repository.insert(workout)

            if (currentMax != null && workout.weight > currentMax) {
                prDetected.value = "🏆 NEW PR on ${workout.exerciseName}! ${workout.weight} kg!"
            } else if (currentMax == null && workout.weight > 0) {
                // First time logging this exercise
                prDetected.value = "🎯 First ${workout.exerciseName} logged at ${workout.weight} kg!"
            }
        }
    }

    fun clearPrMessage() {
        prDetected.value = null
    }

    fun updateWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            repository.update(workout)
        }
    }

    fun deleteWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            repository.delete(workout)
        }
    }

    // Template functions
    fun saveAsTemplate(templateName: String, workouts: List<WorkoutEntity>) {
        viewModelScope.launch {
            val jsonArray = JSONArray()
            for (w in workouts) {
                val obj = JSONObject()
                obj.put("exerciseName", w.exerciseName)
                obj.put("muscleGroup", w.muscleGroup)
                obj.put("sets", w.sets)
                obj.put("reps", w.reps)
                obj.put("weight", w.weight)
                obj.put("duration", w.duration)
                jsonArray.put(obj)
            }
            val template = WorkoutTemplateEntity(
                templateName = templateName,
                exercisesJson = jsonArray.toString()
            )
            templateDao.insert(template)
        }
    }

    fun loadTemplate(template: WorkoutTemplateEntity) {
        viewModelScope.launch {
            val today = DateUtils.getCurrentDateForDb()
            val jsonArray = JSONArray(template.exercisesJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val workout = WorkoutEntity(
                    date = today,
                    exerciseName = obj.getString("exerciseName"),
                    muscleGroup = obj.getString("muscleGroup"),
                    sets = obj.getInt("sets"),
                    reps = obj.getInt("reps"),
                    weight = obj.getDouble("weight"),
                    duration = obj.getInt("duration")
                )
                repository.insert(workout)
            }
        }
    }

    fun deleteTemplate(template: WorkoutTemplateEntity) {
        viewModelScope.launch {
            templateDao.delete(template)
        }
    }
}
