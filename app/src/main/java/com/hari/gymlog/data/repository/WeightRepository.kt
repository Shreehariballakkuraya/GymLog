package com.hari.gymlog.data.repository

import com.hari.gymlog.data.db.dao.WeightDao
import com.hari.gymlog.data.db.entity.WeightEntity
import kotlinx.coroutines.flow.Flow

class WeightRepository(private val weightDao: WeightDao) {
    fun getWeightByDate(date: String): Flow<List<WeightEntity>> = weightDao.getByDate(date)

    fun getWeightRange(startDate: String, endDate: String): Flow<List<WeightEntity>> =
        weightDao.getRange(startDate, endDate)

    fun getLatestWeight(): Flow<WeightEntity?> = weightDao.getLatest()

    suspend fun insert(weight: WeightEntity) = weightDao.insert(weight)

    suspend fun update(weight: WeightEntity) = weightDao.update(weight)

    suspend fun delete(weight: WeightEntity) = weightDao.delete(weight)
}
