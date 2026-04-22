package com.hari.gymlog.data.repository

import com.hari.gymlog.data.db.dao.FoodDao
import com.hari.gymlog.data.db.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

class FoodRepository(private val foodDao: FoodDao) {
    fun getFoodsByDate(date: String): Flow<List<FoodEntity>> = foodDao.getByDate(date)

    fun getFoodsByDateAndMeal(date: String, mealType: String): Flow<List<FoodEntity>> =
        foodDao.getByDateAndMeal(date, mealType)

    suspend fun insert(food: FoodEntity) = foodDao.insert(food)

    suspend fun update(food: FoodEntity) = foodDao.update(food)

    suspend fun delete(food: FoodEntity) = foodDao.delete(food)

    suspend fun getAllFoods(): List<FoodEntity> = foodDao.getAllFoods()
}
