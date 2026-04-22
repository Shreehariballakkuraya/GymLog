package com.hari.gymlog.reminder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hari.gymlog.R
import com.hari.gymlog.databinding.FragmentReminderSettingsBinding
import java.util.concurrent.TimeUnit

class ReminderSettingsFragment : Fragment() {

    private var _binding: FragmentReminderSettingsBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val PREFS_NAME           = "reminder_prefs"
        private const val KEY_ENABLED          = "reminder_enabled"
        private const val KEY_EXERCISE         = "reminder_exercise"
        private const val KEY_CUSTOM_NAME      = "reminder_custom_name"
        private const val KEY_CUSTOM_EXERCISES = "reminder_custom_exercises"  // persisted custom list
        private const val KEY_MODE             = "reminder_mode"
        private const val KEY_TARGET_REPS      = "reminder_target_reps"
        private const val KEY_COUNTDOWN        = "reminder_countdown"
        private const val KEY_PACE_SECS        = "reminder_pace_secs"
        private const val KEY_HOLD_SECS        = "reminder_hold_secs"
        private const val KEY_INTERVAL_MINUTES = "reminder_interval_minutes"
        private const val WORK_TAG             = "exercise_reminder_work"
        const val MODE_REPS = "reps"
        const val MODE_TIME = "time"
    }

    private val presetExercises = listOf(
        "Push-ups", "Pull-ups", "Squats", "Sit-ups",
        "Jumping Jacks", "Burpees", "Lunges", "Dips",
        "Plank", "Wall Sit", "Mountain Climbers"
    )

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) saveAndSchedule()
        else Toast.makeText(requireContext(), "Notification permission denied.", Toast.LENGTH_LONG).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReminderSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupExerciseDropdown()
        setupModeToggle()
        loadSavedSettings()
        setupSaveButton()
    }

    private fun buildExerciseList(): List<String> {
        val saved = loadCustomExercises()
        return (presetExercises + saved).distinct()
    }

    private fun loadCustomExercises(): List<String> {
        val raw = requireContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CUSTOM_EXERCISES, "") ?: ""
        return if (raw.isBlank()) emptyList()
        else raw.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun saveCustomExercise(name: String) {
        val existing = loadCustomExercises().toMutableList()
        if (!existing.contains(name)) {
            existing.add(name)
            requireContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_CUSTOM_EXERCISES, existing.joinToString("|"))
                .apply()
        }
    }

    private fun setupExerciseDropdown() {
        refreshDropdown()
        // When a preset is picked, clear the custom name field
        binding.acExercise.setOnItemClickListener { _, _, _, _ ->
            binding.etCustomName.text?.clear()
        }
    }

    private fun refreshDropdown() {
        val all = buildExerciseList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, all)
        binding.acExercise.setAdapter(adapter)
    }

    private fun setupModeToggle() {
        // Select Reps by default
        binding.toggleMode.check(R.id.btnModeReps)

        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val isTime = checkedId == R.id.btnModeTime
            binding.layoutRepsSection.visibility = if (isTime) View.GONE  else View.VISIBLE
            binding.layoutTimeSection.visibility = if (isTime) View.VISIBLE else View.GONE
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun getEffectiveExerciseName(): String {
        val custom = binding.etCustomName.text.toString().trim()
        return if (custom.isNotEmpty()) custom
        else binding.acExercise.text.toString().trim().ifEmpty { "Push-ups" }
    }

    private fun isTimeMode() = binding.toggleMode.checkedButtonId == R.id.btnModeTime

    private fun getIntervalMinutes(): Long = when {
        binding.chip30m.isChecked -> 30L
        binding.chip1h.isChecked  -> 60L
        binding.chip2h.isChecked  -> 120L
        binding.chip3h.isChecked  -> 180L
        binding.chip4h.isChecked  -> 240L
        else                      -> 60L
    }

    private fun setIntervalChip(minutes: Long) {
        when (minutes) {
            30L  -> binding.chip30m.isChecked = true
            60L  -> binding.chip1h.isChecked  = true
            120L -> binding.chip2h.isChecked  = true
            180L -> binding.chip3h.isChecked  = true
            240L -> binding.chip4h.isChecked  = true
            else -> binding.chip1h.isChecked  = true
        }
    }

    // ── Load / Save ───────────────────────────────────────────────────────────

    private fun loadSavedSettings() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        binding.switchEnable.isChecked = prefs.getBoolean(KEY_ENABLED, false)
        binding.acExercise.setText(prefs.getString(KEY_EXERCISE, "Push-ups"), false)
        binding.etCustomName.setText(prefs.getString(KEY_CUSTOM_NAME, ""))
        binding.etTargetReps.setText(prefs.getInt(KEY_TARGET_REPS, 10).toString())
        binding.etCountdownSecs.setText(prefs.getInt(KEY_COUNTDOWN, 10).toString())
        binding.etPaceSecs.setText(prefs.getInt(KEY_PACE_SECS, 3).toString())
        binding.etHoldSecs.setText(prefs.getInt(KEY_HOLD_SECS, 60).toString())
        setIntervalChip(prefs.getLong(KEY_INTERVAL_MINUTES, 60L))

        // Restore mode toggle
        val mode = prefs.getString(KEY_MODE, MODE_REPS)
        if (mode == MODE_TIME) {
            binding.toggleMode.check(R.id.btnModeTime)
        } else {
            binding.toggleMode.check(R.id.btnModeReps)
        }

        updateStatusText()
    }

    private fun setupSaveButton() {
        binding.btnSaveReminder.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    return@setOnClickListener
                }
            }
            saveAndSchedule()
        }
    }

    private fun saveAndSchedule() {
        val exercise       = getEffectiveExerciseName()
        val customName     = binding.etCustomName.text.toString().trim()
        val mode           = if (isTimeMode()) MODE_TIME else MODE_REPS
        val targetReps     = binding.etTargetReps.text.toString().toIntOrNull() ?: 10
        val countdown      = binding.etCountdownSecs.text.toString().toIntOrNull() ?: 10
        val paceSecs       = binding.etPaceSecs.text.toString().toIntOrNull() ?: 3
        val holdSecs       = binding.etHoldSecs.text.toString().toIntOrNull() ?: 60
        val intervalMins   = getIntervalMinutes()
        val enabled        = binding.switchEnable.isChecked

        // If user typed a new custom exercise name, persist it so it appears in the dropdown next time
        if (customName.isNotEmpty()) {
            saveCustomExercise(customName)
            refreshDropdown()
        }

        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putBoolean(KEY_ENABLED, enabled)
            putString(KEY_EXERCISE, binding.acExercise.text.toString().trim())
            putString(KEY_CUSTOM_NAME, customName)
            putString(KEY_MODE, mode)
            putInt(KEY_TARGET_REPS, targetReps)
            putInt(KEY_COUNTDOWN, countdown)
            putInt(KEY_PACE_SECS, paceSecs)
            putInt(KEY_HOLD_SECS, holdSecs)
            putLong(KEY_INTERVAL_MINUTES, intervalMins)
            apply()
        }

        val workManager = WorkManager.getInstance(requireContext())

        if (enabled) {
            val inputData = Data.Builder()
                .putString(ReminderWorker.KEY_EXERCISE, exercise)
                .putString(ReminderWorker.KEY_MODE, mode)
                .putInt(ReminderWorker.KEY_TARGET_REPS, targetReps)
                .putInt(ReminderWorker.KEY_COUNTDOWN_SECS, countdown)
                .putInt(ReminderWorker.KEY_PACE_SECS, paceSecs)
                .putInt(ReminderWorker.KEY_HOLD_SECS, holdSecs)
                .build()

            val actualInterval = maxOf(intervalMins, 15L)
            val periodicRequest = PeriodicWorkRequestBuilder<ReminderWorker>(actualInterval, TimeUnit.MINUTES)
                .setInputData(inputData)
                .addTag(WORK_TAG)
                .build()

            workManager.enqueueUniquePeriodicWork(WORK_TAG, ExistingPeriodicWorkPolicy.UPDATE, periodicRequest)

            // Fire immediately
            val immediateRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(inputData).build()
            workManager.enqueue(immediateRequest)

            val modeLabel = if (mode == MODE_TIME) "${holdSecs}s hold" else "$targetReps reps"
            binding.tvStatus.text = "✅ $exercise · $modeLabel every ${intervalMins}min"
            Toast.makeText(requireContext(), "Saved! First reminder coming right up 🔔", Toast.LENGTH_SHORT).show()
        } else {
            workManager.cancelAllWorkByTag(WORK_TAG)
            binding.tvStatus.text = "⏸ Reminders are off"
            Toast.makeText(requireContext(), "Reminders disabled", Toast.LENGTH_SHORT).show()
        }

        updateStatusText()
    }

    private fun updateStatusText() {
        val prefs    = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val enabled  = prefs.getBoolean(KEY_ENABLED, false)
        val exercise = prefs.getString(KEY_EXERCISE, "Push-ups")
        val mode     = prefs.getString(KEY_MODE, MODE_REPS)
        val interval = prefs.getLong(KEY_INTERVAL_MINUTES, 60L)
        val reps     = prefs.getInt(KEY_TARGET_REPS, 10)
        val hold     = prefs.getInt(KEY_HOLD_SECS, 60)

        binding.tvStatus.text = if (enabled) {
            val modeLabel = if (mode == MODE_TIME) "${hold}s hold" else "$reps reps"
            "✅ $exercise · $modeLabel · every ${interval}min"
        } else "⏸ Reminders are off"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
