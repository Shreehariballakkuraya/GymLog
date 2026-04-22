package com.hari.gymlog.ui.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.hari.gymlog.data.db.entity.FoodEntity
import com.hari.gymlog.databinding.DialogFoodEntryBinding
import com.hari.gymlog.util.DateUtils

class FoodEntryDialogFragment(
    private val foodToEdit: FoodEntity? = null
) : DialogFragment() {

    private var _binding: DialogFoodEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FoodViewModel by activityViewModels()

    private val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")

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
        _binding = DialogFoodEntryBinding.inflate(inflater, container, false)
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
            saveFood()
        }
    }

    private fun setupDropdown() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mealTypes)
        binding.acMealType.setAdapter(adapter)
    }

    private fun populateIfEditing() {
        if (foodToEdit != null) {
            binding.tvDialogTitle.text = "Edit Meal"
            binding.acMealType.setText(foodToEdit.mealType, false)
            binding.etFoodName.setText(foodToEdit.foodName)
            binding.etQuantity.setText(foodToEdit.quantity)
            if (foodToEdit.calories != null) {
                binding.etCalories.setText(foodToEdit.calories.toString())
            }
            if (foodToEdit.protein != null) {
                binding.etProtein.setText(foodToEdit.protein.toString())
            }
            if (foodToEdit.carbs != null) {
                binding.etCarbs.setText(foodToEdit.carbs.toString())
            }
            if (foodToEdit.fat != null) {
                binding.etFat.setText(foodToEdit.fat.toString())
            }
        }
    }

    private fun saveFood() {
        val mealType = binding.acMealType.text.toString().trim()
        val foodName = binding.etFoodName.text.toString().trim()
        val quantity = binding.etQuantity.text.toString().trim()
        val caloriesStr = binding.etCalories.text.toString().trim()
        val proteinStr = binding.etProtein.text.toString().trim()
        val carbsStr = binding.etCarbs.text.toString().trim()
        val fatStr = binding.etFat.text.toString().trim()

        if (mealType.isEmpty() || foodName.isEmpty() || quantity.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val calories = caloriesStr.toIntOrNull()
        val protein = proteinStr.toIntOrNull()
        val carbs = carbsStr.toIntOrNull()
        val fat = fatStr.toIntOrNull()

        if (foodToEdit != null) {
            val updatedFood = foodToEdit.copy(
                mealType = mealType,
                foodName = foodName,
                quantity = quantity,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat
            )
            viewModel.updateFood(updatedFood)
        } else {
            val newFood = FoodEntity(
                date = DateUtils.getCurrentDateForDb(),
                mealType = mealType,
                foodName = foodName,
                quantity = quantity,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat
            )
            viewModel.addFood(newFood)
        }
        
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FoodEntryDialog"
    }
}
