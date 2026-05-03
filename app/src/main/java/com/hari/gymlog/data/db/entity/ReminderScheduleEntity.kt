package com.hari.gymlog.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_schedule")
data class ReminderScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseName: String,
    val customName: String = "",
    val mode: String, // "reps" or "time"
    val targetReps: Int = 10,
    val countdownSecs: Int = 10,
    val paceSecs: Int = 3,
    val holdSecs: Int = 60,
    val intervalMinutes: Long = 60,
    val startHour: Int = 10,
    val endHour: Int = 18,
    val isEnabled: Boolean = true
)
