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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hari.gymlog.R
import com.hari.gymlog.data.db.GymLogDatabase
import com.hari.gymlog.data.db.entity.ReminderScheduleEntity
import com.hari.gymlog.databinding.FragmentReminderSettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class ReminderSettingsFragment : Fragment() {

    private var _binding: FragmentReminderSettingsBinding? = null
    private val binding get() = _binding!!

    private var scheduleId: Long = -1L

    companion object {
        private const val PREFS_NAME           = "reminder_prefs"
        private const val KEY_CUSTOM_EXERCISES = "reminder_custom_exercises"
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
        scheduleId = arguments?.getLong("scheduleId", -1L) ?: -1L

        setupExerciseDropdown()
        setupModeToggle()
        setupActiveHoursSlider()
        setupSaveButton()

        if (scheduleId != -1L) {
            loadScheduleFromDb(scheduleId)
        } else {
            // Default setup for new schedule
            binding.sliderActiveHours.values = listOf(10f, 18f)
            binding.tvActiveHoursLabel.text = formatTimeRange(10, 18)
            binding.switchEnable.isChecked = true
            binding.btnSaveReminder.text = "Create Schedule"
        }
    }

    private fun loadScheduleFromDb(id: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val schedule = GymLogDatabase.getDatabase(requireContext()).reminderScheduleDao().getScheduleById(id)
            withContext(Dispatchers.Main) {
                schedule?.let { populateUi(it) }
            }
        }
    }

    private fun populateUi(schedule: ReminderScheduleEntity) {
        binding.switchEnable.isChecked = schedule.isEnabled
        binding.acExercise.setText(schedule.exerciseName, false)
        binding.etCustomName.setText(schedule.customName)
        binding.etTargetReps.setText(schedule.targetReps.toString())
        binding.etCountdownSecs.setText(schedule.countdownSecs.toString())
        binding.etPaceSecs.setText(schedule.paceSecs.toString())
        binding.etHoldSecs.setText(schedule.holdSecs.toString())
        setIntervalChip(schedule.intervalMinutes)

        binding.sliderActiveHours.values = listOf(schedule.startHour.toFloat(), schedule.endHour.toFloat())
        binding.tvActiveHoursLabel.text = formatTimeRange(schedule.startHour, schedule.endHour)

        if (schedule.mode == MODE_TIME) {
            binding.toggleMode.check(R.id.btnModeTime)
        } else {
            binding.toggleMode.check(R.id.btnModeReps)
        }

        binding.btnSaveReminder.text = "Update Schedule"
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
        binding.toggleMode.check(R.id.btnModeReps)
        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val isTime = checkedId == R.id.btnModeTime
            binding.layoutRepsSection.visibility = if (isTime) View.GONE  else View.VISIBLE
            binding.layoutTimeSection.visibility = if (isTime) View.VISIBLE else View.GONE
        }
    }

    private fun setupActiveHoursSlider() {
        binding.sliderActiveHours.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            if (values.size >= 2) {
                val start = values[0].toInt()
                val end = values[1].toInt()
                binding.tvActiveHoursLabel.text = formatTimeRange(start, end)
            }
        }
    }

    private fun formatTimeRange(startHour: Int, endHour: Int): String {
        return "${formatHour(startHour)} - ${formatHour(endHour)}"
    }

    private fun formatHour(hour: Int): String {
        val amPm = if (hour < 12 || hour == 24) "AM" else "PM"
        val displayHour = when {
            hour == 0 || hour == 24 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return "$displayHour:00 $amPm"
    }

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
        val exercise       = binding.acExercise.text.toString().trim()
        val customName     = binding.etCustomName.text.toString().trim()
        val mode           = if (isTimeMode()) MODE_TIME else MODE_REPS
        val targetReps     = binding.etTargetReps.text.toString().toIntOrNull() ?: 10
        val countdown      = binding.etCountdownSecs.text.toString().toIntOrNull() ?: 10
        val paceSecs       = binding.etPaceSecs.text.toString().toIntOrNull() ?: 3
        val holdSecs       = binding.etHoldSecs.text.toString().toIntOrNull() ?: 60
        val intervalMins   = getIntervalMinutes()
        val enabled        = binding.switchEnable.isChecked

        val sliderValues   = binding.sliderActiveHours.values
        val startHour      = if (sliderValues.size >= 2) sliderValues[0].toInt() else 10
        val endHour        = if (sliderValues.size >= 2) sliderValues[1].toInt() else 18

        if (customName.isNotEmpty()) {
            saveCustomExercise(customName)
            refreshDropdown()
        }

        val entity = ReminderScheduleEntity(
            id = if (scheduleId != -1L) scheduleId else 0,
            exerciseName = exercise,
            customName = customName,
            mode = mode,
            targetReps = targetReps,
            countdownSecs = countdown,
            paceSecs = paceSecs,
            holdSecs = holdSecs,
            intervalMinutes = intervalMins,
            startHour = startHour,
            endHour = endHour,
            isEnabled = enabled
        )

        lifecycleScope.launch(Dispatchers.IO) {
            val dao = GymLogDatabase.getDatabase(requireContext()).reminderScheduleDao()
            val newId = if (scheduleId != -1L) {
                dao.update(entity)
                scheduleId
            } else {
                dao.insert(entity)
            }

            val finalEntity = entity.copy(id = newId)

            withContext(Dispatchers.Main) {
                if (enabled) {
                    scheduleWorker(finalEntity)
                    Toast.makeText(requireContext(), "Schedule Saved & Activated 🔔", Toast.LENGTH_SHORT).show()
                } else {
                    cancelWorker(newId)
                    Toast.makeText(requireContext(), "Schedule Saved (Disabled)", Toast.LENGTH_SHORT).show()
                }
                findNavController().popBackStack()
            }
        }
    }

    private fun scheduleWorker(schedule: ReminderScheduleEntity) {
        val workManager = WorkManager.getInstance(requireContext())
        val inputData = Data.Builder()
            .putLong(ReminderWorker.KEY_SCHEDULE_ID, schedule.id)
            .putString(ReminderWorker.KEY_EXERCISE, schedule.customName.ifEmpty { schedule.exerciseName }.ifEmpty { "Push-ups" })
            .putString(ReminderWorker.KEY_MODE, schedule.mode)
            .putInt(ReminderWorker.KEY_TARGET_REPS, schedule.targetReps)
            .putInt(ReminderWorker.KEY_COUNTDOWN_SECS, schedule.countdownSecs)
            .putInt(ReminderWorker.KEY_PACE_SECS, schedule.paceSecs)
            .putInt(ReminderWorker.KEY_HOLD_SECS, schedule.holdSecs)
            .putInt(ReminderWorker.KEY_START_HOUR, schedule.startHour)
            .putInt(ReminderWorker.KEY_END_HOUR, schedule.endHour)
            .build()

        val tag = "${ReminderWorker.WORK_TAG_PREFIX}${schedule.id}"
        val actualInterval = maxOf(schedule.intervalMinutes, 15L)
        val periodicRequest = PeriodicWorkRequestBuilder<ReminderWorker>(actualInterval, TimeUnit.MINUTES)
            .setInputData(inputData)
            .addTag(tag)
            .build()

        workManager.enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.UPDATE, periodicRequest)

        // Fire immediately for testing/feedback
        val immediateRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(inputData).build()
        workManager.enqueue(immediateRequest)
    }

    private fun cancelWorker(scheduleId: Long) {
        val tag = "${ReminderWorker.WORK_TAG_PREFIX}$scheduleId"
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(tag)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
