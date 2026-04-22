package com.hari.gymlog.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_templates")
data class WorkoutTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateName: String,
    val exercisesJson: String    // JSON array of exercise objects
)
