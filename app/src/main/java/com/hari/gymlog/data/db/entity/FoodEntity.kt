package com.hari.gymlog.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val mealType: String,       // Breakfast | Lunch | Dinner | Snack
    val foodName: String,
    val quantity: String,
    val calories: Int? = null,   // optional
    @ColumnInfo(defaultValue = "NULL") val protein: Int? = null,
    @ColumnInfo(defaultValue = "NULL") val carbs: Int? = null,
    @ColumnInfo(defaultValue = "NULL") val fat: Int? = null
)
