package com.hari.gymlog.ui.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hari.gymlog.data.db.entity.FoodEntity
import com.hari.gymlog.databinding.ItemFoodBinding

class FoodAdapter(
    private val onItemClick: (FoodEntity) -> Unit
) : ListAdapter<FoodEntity, FoodAdapter.FoodViewHolder>(FoodDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FoodViewHolder(
        private val binding: ItemFoodBinding,
        private val onItemClick: (FoodEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(food: FoodEntity) {
            binding.apply {
                tvFoodName.text = food.foodName
                tvQuantity.text = food.quantity
                tvMealType.text = food.mealType
                
                if (food.calories != null) {
                    tvCalories.text = "${food.calories} kcal"
                } else {
                    tvCalories.text = ""
                }

                root.setOnClickListener {
                    onItemClick(food)
                }
            }
        }
    }

    class FoodDiffCallback : DiffUtil.ItemCallback<FoodEntity>() {
        override fun areItemsTheSame(oldItem: FoodEntity, newItem: FoodEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FoodEntity, newItem: FoodEntity): Boolean {
            return oldItem == newItem
        }
    }
}
