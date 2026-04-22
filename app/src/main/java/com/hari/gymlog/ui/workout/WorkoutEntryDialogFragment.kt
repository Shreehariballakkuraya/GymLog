package com.hari.gymlog.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.hari.gymlog.R
import com.hari.gymlog.data.db.entity.WorkoutEntity
import com.hari.gymlog.databinding.DialogWorkoutEntryBinding
import com.hari.gymlog.util.DateUtils

class WorkoutEntryDialogFragment(
    private val workoutToEdit: WorkoutEntity? = null
) : DialogFragment() {

    private var _binding: DialogWorkoutEntryBinding? = null
    private val binding get() = _binding!!

    // Share the ViewModel with WorkoutFragment
    private val viewModel: WorkoutViewModel by activityViewModels()

    private val muscleGroups = listOf(
        "Chest", "Back", "Shoulders", "Biceps", "Triceps", "Legs", "Core", "Cardio", "Full Body", "Other"
    )

    private val commonExercises = listOf(
        "Bench Press", "Squat", "Deadlift", "Overhead Press", 
        "Pull Up", "Rowing", "Push Up", "Lunges", "Bicep Curl",
        "Tricep Extension", "Leg Press", "Lat Pulldown", "Crunches",
        "Plank", "Cardio - Running", "Cardio - Cycling", "Cardio - Swimming",
        "Cardio - Rowing", "Cardio - Elliptical"
    )

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogWorkoutEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdown()
        populateIfEditing()

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            saveWorkout()
        }
    }

    private fun setupDropdown() {
        val muscleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, muscleGroups)
        binding.acMuscleGroup.setAdapter(muscleAdapter)

        val exerciseAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, commonExercises)
        binding.etExerciseName.setAdapter(exerciseAdapter)
    }

    private fun populateIfEditing() {
        if (workoutToEdit != null) {
            binding.tvDialogTitle.text = "Edit Workout"
            binding.etExerciseName.setText(workoutToEdit.exerciseName, false)
            binding.acMuscleGroup.setText(workoutToEdit.muscleGroup, false)
            binding.etSets.setText(workoutToEdit.sets.toString())
            binding.etReps.setText(workoutToEdit.reps.toString())
            binding.etWeight.setText(workoutToEdit.weight.toString())
            binding.etDuration.setText(workoutToEdit.duration.toString())
        }
    }

    private fun saveWorkout() {
        val exerciseName = binding.etExerciseName.text.toString().trim()
        val muscleGroup = binding.acMuscleGroup.text.toString().trim()
        val setsStr = binding.etSets.text.toString().trim()
        val repsStr = binding.etReps.text.toString().trim()
        val weightStr = binding.etWeight.text.toString().trim()
        val durationStr = binding.etDuration.text.toString().trim()

        if (exerciseName.isEmpty() || muscleGroup.isEmpty() || setsStr.isEmpty() || repsStr.isEmpty() || weightStr.isEmpty() || durationStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val sets = setsStr.toIntOrNull() ?: 0
        val reps = repsStr.toIntOrNull() ?: 0
        val weight = weightStr.toDoubleOrNull() ?: 0.0
        val duration = durationStr.toIntOrNull() ?: 0

        if (workoutToEdit != null) {
            val updatedWorkout = workoutToEdit.copy(
                exerciseName = exerciseName,
                muscleGroup = muscleGroup,
                sets = sets,
                reps = reps,
                weight = weight,
                duration = duration
            )
            viewModel.updateWorkout(updatedWorkout)
        } else {
            val newWorkout = WorkoutEntity(
                date = DateUtils.getCurrentDateForDb(),
                exerciseName = exerciseName,
                muscleGroup = muscleGroup,
                sets = sets,
                reps = reps,
                weight = weight,
                duration = duration
            )
            viewModel.addWorkout(newWorkout)
        }
        
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "WorkoutEntryDialog"
    }
}
