package com.hari.gymlog.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weights")
data class WeightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val bodyWeight: Double,
    val bodyFatPercentage: Double? = null
)
