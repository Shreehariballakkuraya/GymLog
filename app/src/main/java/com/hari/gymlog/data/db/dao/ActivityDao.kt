package com.hari.gymlog.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hari.gymlog.data.db.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities WHERE date = :date")
    fun getByDate(date: String): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getRange(startDate: String, endDate: String): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity)

    @Update
    suspend fun update(activity: ActivityEntity)

    @Delete
    suspend fun delete(activity: ActivityEntity)
}
