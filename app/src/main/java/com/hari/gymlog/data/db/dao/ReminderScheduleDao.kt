package com.hari.gymlog.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hari.gymlog.data.db.entity.ReminderScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderScheduleDao {

    @Query("SELECT * FROM reminder_schedule ORDER BY id DESC")
    fun getAllSchedules(): Flow<List<ReminderScheduleEntity>>

    @Query("SELECT * FROM reminder_schedule WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): ReminderScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: ReminderScheduleEntity): Long

    @Update
    suspend fun update(schedule: ReminderScheduleEntity)

    @Delete
    suspend fun delete(schedule: ReminderScheduleEntity)

    @Query("DELETE FROM reminder_schedule WHERE id = :id")
    suspend fun deleteById(id: Long)
}
