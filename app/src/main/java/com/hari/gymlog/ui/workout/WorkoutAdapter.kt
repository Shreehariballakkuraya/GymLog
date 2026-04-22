package com.hari.gymlog.ui.workout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hari.gymlog.data.db.entity.WorkoutEntity
import com.hari.gymlog.databinding.ItemWorkoutBinding

class WorkoutAdapter(
    private val onItemClick: (WorkoutEntity) -> Unit
) : ListAdapter<WorkoutEntity, WorkoutAdapter.WorkoutViewHolder>(WorkoutDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkoutViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class WorkoutViewHolder(
        private val binding: ItemWorkoutBinding,
        private val onItemClick: (WorkoutEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(workout: WorkoutEntity) {
            binding.apply {
                tvExerciseName.text = workout.exerciseName
                chipMuscleGroup.text = workout.muscleGroup
                tvSetsReps.text = "${workout.sets} sets • ${workout.reps} reps"
                tvDuration.text = "${workout.duration} min"
                tvWeight.text = "${workout.weight} kg"

                root.setOnClickListener {
                    onItemClick(workout)
                }
            }
        }
    }

    class WorkoutDiffCallback : DiffUtil.ItemCallback<WorkoutEntity>() {
        override fun areItemsTheSame(oldItem: WorkoutEntity, newItem: WorkoutEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WorkoutEntity, newItem: WorkoutEntity): Boolean {
            return oldItem == newItem
        }
    }
}
