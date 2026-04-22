package com.hari.gymlog.data.repository

import com.hari.gymlog.data.db.dao.ActivityDao
import com.hari.gymlog.data.db.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

class ActivityRepository(private val activityDao: ActivityDao) {
    fun getActivityByDate(date: String): Flow<List<ActivityEntity>> = activityDao.getByDate(date)

    fun getActivityRange(startDate: String, endDate: String): Flow<List<ActivityEntity>> = activityDao.getRange(startDate, endDate)

    suspend fun insertOrUpdate(activity: ActivityEntity) = activityDao.insert(activity)

    suspend fun delete(activity: ActivityEntity) = activityDao.delete(activity)
}
