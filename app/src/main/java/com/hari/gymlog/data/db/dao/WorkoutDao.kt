package com.hari.gymlog.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hari.gymlog.data.db.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE date = :date")
    fun getByDate(date: String): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: WorkoutEntity)

    @Update
    suspend fun update(workout: WorkoutEntity)

    @Delete
    suspend fun delete(workout: WorkoutEntity)

    @Query("SELECT DISTINCT date FROM workouts ORDER BY date DESC")
    fun getAllDates(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM workouts WHERE date BETWEEN :startDate AND :endDate")
    fun getWeeklyCount(startDate: String, endDate: String): Flow<Int>

    @Query("SELECT DISTINCT date FROM workouts WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getDistinctDatesInRange(startDate: String, endDate: String): Flow<List<String>>

    @Query("SELECT MAX(weight) FROM workouts WHERE exerciseName = :exerciseName")
    suspend fun getMaxWeightForExercise(exerciseName: String): Double?

    @Query("SELECT * FROM workouts ORDER BY date DESC")
    suspend fun getAllWorkouts(): List<WorkoutEntity>
}
