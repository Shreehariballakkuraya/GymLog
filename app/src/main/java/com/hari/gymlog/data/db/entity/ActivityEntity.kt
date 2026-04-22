package com.hari.gymlog.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val steps: Int,
    val activeMinutes: Int
)
