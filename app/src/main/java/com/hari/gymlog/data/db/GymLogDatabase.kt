package com.hari.gymlog.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hari.gymlog.data.db.dao.ActivityDao
import com.hari.gymlog.data.db.dao.FoodDao
import com.hari.gymlog.data.db.dao.WeightDao
import com.hari.gymlog.data.db.dao.WorkoutDao
import com.hari.gymlog.data.db.dao.WorkoutTemplateDao
import com.hari.gymlog.data.db.entity.ActivityEntity
import com.hari.gymlog.data.db.entity.FoodEntity
import com.hari.gymlog.data.db.entity.WeightEntity
import com.hari.gymlog.data.db.entity.WorkoutEntity
import com.hari.gymlog.data.db.entity.WorkoutTemplateEntity
import com.hari.gymlog.data.db.entity.ReminderScheduleEntity
import com.hari.gymlog.data.db.dao.ReminderScheduleDao

@Database(
    entities = [
        WorkoutEntity::class,
        FoodEntity::class,
        ActivityEntity::class,
        WeightEntity::class,
        WorkoutTemplateEntity::class,
        ReminderScheduleEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class GymLogDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun foodDao(): FoodDao
    abstract fun activityDao(): ActivityDao
    abstract fun weightDao(): WeightDao
    abstract fun workoutTemplateDao(): WorkoutTemplateDao
    abstract fun reminderScheduleDao(): ReminderScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: GymLogDatabase? = null

        fun getDatabase(context: Context): GymLogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymLogDatabase::class.java,
                    "gymlog_database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
