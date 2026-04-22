package com.hari.gymlog.ui.weight

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hari.gymlog.data.db.entity.WeightEntity
import com.hari.gymlog.databinding.ItemWeightBinding

class WeightAdapter(
    private val onItemClick: (WeightEntity) -> Unit
) : ListAdapter<WeightEntity, WeightAdapter.WeightViewHolder>(WeightDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightViewHolder {
        val binding = ItemWeightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WeightViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: WeightViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class WeightViewHolder(
        private val binding: ItemWeightBinding,
        private val onItemClick: (WeightEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(weight: WeightEntity) {
            binding.apply {
                tvDate.text = weight.date
                tvWeight.text = "${weight.bodyWeight} kg"
                
                root.setOnClickListener {
                    onItemClick(weight)
                }
            }
        }
    }

    class WeightDiffCallback : DiffUtil.ItemCallback<WeightEntity>() {
        override fun areItemsTheSame(oldItem: WeightEntity, newItem: WeightEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WeightEntity, newItem: WeightEntity): Boolean {
            return oldItem == newItem
        }
    }
}
