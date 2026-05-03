package com.hari.gymlog.ui.reminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hari.gymlog.R
import com.hari.gymlog.data.db.GymLogDatabase
import com.hari.gymlog.databinding.FragmentReminderListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.hari.gymlog.reminder.ReminderWorker
import androidx.work.WorkManager
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers

class ReminderListFragment : Fragment() {

    private var _binding: FragmentReminderListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ReminderAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReminderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ReminderAdapter(
            onItemClick = { schedule ->
                val bundle = Bundle().apply { putLong("scheduleId", schedule.id) }
                findNavController().navigate(R.id.action_reminderList_to_reminderSettings, bundle)
            },
            onToggleChanged = { schedule, isEnabled ->
                val updated = schedule.copy(isEnabled = isEnabled)
                lifecycleScope.launch(Dispatchers.IO) {
                    GymLogDatabase.getDatabase(requireContext()).reminderScheduleDao().update(updated)
                }
                if (isEnabled) {
                    scheduleWorker(updated)
                } else {
                    cancelWorker(updated.id)
                }
            }
        )
        binding.rvReminders.adapter = adapter

        binding.fabAddReminder.setOnClickListener {
            findNavController().navigate(R.id.action_reminderList_to_reminderSettings)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val dao = GymLogDatabase.getDatabase(requireContext()).reminderScheduleDao()
            dao.getAllSchedules().collectLatest { schedules ->
                adapter.submitList(schedules)
                binding.tvEmptyState.visibility = if (schedules.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun scheduleWorker(schedule: com.hari.gymlog.data.db.entity.ReminderScheduleEntity) {
        val workManager = WorkManager.getInstance(requireContext())
        val inputData = Data.Builder()
            .putLong(ReminderWorker.KEY_SCHEDULE_ID, schedule.id)
            .putString(ReminderWorker.KEY_EXERCISE, schedule.customName.ifEmpty { schedule.exerciseName })
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
