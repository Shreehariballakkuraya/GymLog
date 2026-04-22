package com.hari.gymlog.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hari.gymlog.data.db.entity.WeightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weights WHERE date = :date")
    fun getByDate(date: String): Flow<List<WeightEntity>>

    @Query("SELECT * FROM weights WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getRange(startDate: String, endDate: String): Flow<List<WeightEntity>>

    @Query("SELECT * FROM weights ORDER BY date DESC LIMIT 1")
    fun getLatest(): Flow<WeightEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weight: WeightEntity)

    @Update
    suspend fun update(weight: WeightEntity)

    @Delete
    suspend fun delete(weight: WeightEntity)
}
