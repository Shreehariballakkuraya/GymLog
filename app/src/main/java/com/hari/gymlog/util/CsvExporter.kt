package com.hari.gymlog.util

import android.content.Context
import com.hari.gymlog.data.db.GymLogDatabase
import com.hari.gymlog.data.repository.FoodRepository
import com.hari.gymlog.data.repository.WorkoutRepository
import java.io.File
import java.io.FileWriter

object CsvExporter {

    suspend fun exportAllData(context: Context): File {
        val db = GymLogDatabase.getDatabase(context)
        val workoutRepo = WorkoutRepository(db.workoutDao())
        val foodRepo = FoodRepository(db.foodDao())

        val file = File(context.cacheDir, "gymlog_export_${System.currentTimeMillis()}.csv")
        val writer = FileWriter(file)

        // Workouts section
        writer.append("=== WORKOUTS ===\n")
        writer.append("Date,Exercise,Muscle Group,Sets,Reps,Weight(kg),Duration(min)\n")
        val workouts = workoutRepo.getAllWorkouts()
        for (w in workouts) {
            writer.append("${w.date},${w.exerciseName},${w.muscleGroup},${w.sets},${w.reps},${w.weight},${w.duration}\n")
        }

        writer.append("\n")

        // Food section
        writer.append("=== FOOD LOG ===\n")
        writer.append("Date,Meal Type,Food Name,Quantity,Calories,Protein(g),Carbs(g),Fat(g)\n")
        val foods = foodRepo.getAllFoods()
        for (f in foods) {
            writer.append("${f.date},${f.mealType},${f.foodName},${f.quantity},${f.calories ?: ""},${f.protein ?: ""},${f.carbs ?: ""},${f.fat ?: ""}\n")
        }

        writer.flush()
        writer.close()

        return file
    }
}
