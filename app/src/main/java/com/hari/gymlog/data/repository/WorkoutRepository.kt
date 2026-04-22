package com.hari.gymlog.data.repository

import com.hari.gymlog.data.db.dao.WorkoutDao
import com.hari.gymlog.data.db.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutDao: WorkoutDao) {
    fun getWorkoutsByDate(date: String): Flow<List<WorkoutEntity>> = workoutDao.getByDate(date)

    suspend fun insert(workout: WorkoutEntity) = workoutDao.insert(workout)

    suspend fun update(workout: WorkoutEntity) = workoutDao.update(workout)

    suspend fun delete(workout: WorkoutEntity) = workoutDao.delete(workout)

    fun getAllDates(): Flow<List<String>> = workoutDao.getAllDates()

    fun getWeeklyCount(startDate: String, endDate: String): Flow<Int> = 
        workoutDao.getWeeklyCount(startDate, endDate)

    fun getDistinctDatesInRange(startDate: String, endDate: String): Flow<List<String>> =
        workoutDao.getDistinctDatesInRange(startDate, endDate)

    suspend fun getMaxWeightForExercise(exerciseName: String): Double? =
        workoutDao.getMaxWeightForExercise(exerciseName)

    suspend fun getAllWorkouts(): List<WorkoutEntity> = workoutDao.getAllWorkouts()
}
