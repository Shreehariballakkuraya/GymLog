package com.hari.gymlog.ui.reminder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hari.gymlog.data.db.entity.ReminderScheduleEntity
import com.hari.gymlog.databinding.ItemReminderScheduleBinding
import com.hari.gymlog.reminder.ReminderSettingsFragment

class ReminderAdapter(
    private val onItemClick: (ReminderScheduleEntity) -> Unit,
    private val onToggleChanged: (ReminderScheduleEntity, Boolean) -> Unit
) : ListAdapter<ReminderScheduleEntity, ReminderAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemReminderScheduleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReminderScheduleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val binding = holder.binding

        binding.tvExerciseName.text = item.customName.ifEmpty { item.exerciseName }

        val modeText = if (item.mode == ReminderSettingsFragment.MODE_TIME) {
            "${item.holdSecs}s hold"
        } else {
            "${item.targetReps} reps"
        }

        val amPmStart = if (item.startHour < 12) "AM" else "PM"
        val displayStart = if (item.startHour % 12 == 0) 12 else item.startHour % 12

        val amPmEnd = if (item.endHour < 12) "AM" else "PM"
        val displayEnd = if (item.endHour % 12 == 0) 12 else item.endHour % 12

        val activeText = "Active: $displayStart:00 $amPmStart - $displayEnd:00 $amPmEnd"

        binding.tvDetails.text = "$modeText • Every ${item.intervalMinutes} min\n$activeText"

        binding.switchEnabled.setOnCheckedChangeListener(null) // Prevent unwanted triggers during bind
        binding.switchEnabled.isChecked = item.isEnabled
        
        binding.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
            onToggleChanged(item, isChecked)
        }

        binding.root.setOnClickListener {
            onItemClick(item)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ReminderScheduleEntity>() {
        override fun areItemsTheSame(oldItem: ReminderScheduleEntity, newItem: ReminderScheduleEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ReminderScheduleEntity, newItem: ReminderScheduleEntity): Boolean {
            return oldItem == newItem
        }
    }
}
