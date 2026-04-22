package com.hari.gymlog.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hari.gymlog.data.db.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE date = :date")
    fun getByDate(date: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE date = :date AND mealType = :mealType")
    fun getByDateAndMeal(date: String, mealType: String): Flow<List<FoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(food: FoodEntity)

    @Update
    suspend fun update(food: FoodEntity)

    @Delete
    suspend fun delete(food: FoodEntity)

    @Query("SELECT * FROM foods ORDER BY date DESC")
    suspend fun getAllFoods(): List<FoodEntity>
}
