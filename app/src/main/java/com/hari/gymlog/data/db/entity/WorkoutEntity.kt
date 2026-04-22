package com.hari.gymlog.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,           // "yyyy-MM-dd"
    val exerciseName: String,
    val muscleGroup: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,         // kg
    val duration: Int           // minutes
)
