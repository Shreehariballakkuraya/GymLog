package com.hari.gymlog.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hari.gymlog.data.db.entity.WorkoutTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutTemplateDao {
    @Query("SELECT * FROM workout_templates ORDER BY templateName ASC")
    fun getAllTemplates(): Flow<List<WorkoutTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: WorkoutTemplateEntity)

    @Delete
    suspend fun delete(template: WorkoutTemplateEntity)
}
