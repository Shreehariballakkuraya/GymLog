package com.hari.gymlog.ui.weight

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hari.gymlog.databinding.FragmentWeightBinding
import kotlinx.coroutines.launch

class WeightFragment : Fragment() {

    private var _binding: FragmentWeightBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeightViewModel by activityViewModels()
    private lateinit var adapter: WeightAdapter

    private var userHeightCm: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadHeightPreference()
        setupRecyclerView()
        setupFab()
        observeData()
    }

    private fun loadHeightPreference() {
        val prefs = requireContext().getSharedPreferences("gymlog_prefs", Context.MODE_PRIVATE)
        userHeightCm = prefs.getFloat("user_height_cm", 0f)

        if (userHeightCm == 0f) {
            // Prompt for height the first time
            promptForHeight()
        }
    }

    private fun promptForHeight() {
        val editText = EditText(requireContext()).apply {
            hint = "Height in cm (e.g. 170)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setPadding(60, 40, 60, 20)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Your Height")
            .setMessage("Enter your height once for automatic BMI calculation:")
            .setView(editText)
            .setCancelable(false)
            .setPositiveButton("Save") { _, _ ->
                val height = editText.text.toString().toFloatOrNull()
                if (height != null && height > 0) {
                    userHeightCm = height
                    requireContext().getSharedPreferences("gymlog_prefs", Context.MODE_PRIVATE)
                        .edit().putFloat("user_height_cm", height).apply()
                } else {
                    Toast.makeText(requireContext(), "Invalid height, BMI won't be calculated", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Skip", null)
            .show()
    }

    private fun calculateBmi(weightKg: Double): Double? {
        if (userHeightCm <= 0) return null
        val heightM = userHeightCm / 100.0
        return weightKg / (heightM * heightM)
    }

    private fun getBmiCategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }

    private fun getBmiChipColor(bmi: Double): Int {
        return when {
            bmi < 18.5 -> android.graphics.Color.parseColor("#42A5F5") // Blue
            bmi < 25.0 -> android.graphics.Color.parseColor("#66BB6A") // Green
            bmi < 30.0 -> android.graphics.Color.parseColor("#FFC107") // Amber
            else -> android.graphics.Color.parseColor("#EF5350")       // Red
        }
    }

    private fun setupRecyclerView() {
        adapter = WeightAdapter { weight ->
            val dialog = WeightEntryDialogFragment(weightToEdit = weight)
            dialog.show(parentFragmentManager, WeightEntryDialogFragment.TAG)
        }
        binding.rvRecentWeights.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentWeights.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val weight = adapter.currentList[position]
                viewModel.deleteWeight(weight)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvRecentWeights)
    }

    private fun setupFab() {
        binding.fabAddWeight.setOnClickListener {
            val dialog = WeightEntryDialogFragment()
            dialog.show(parentFragmentManager, WeightEntryDialogFragment.TAG)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.latestWeight.collect { latest ->
                        if (latest != null) {
                            binding.tvLatestWeightValue.text = "${latest.bodyWeight} kg"
                            var info = "Current Weight (Recorded ${latest.date})"
                            if (latest.bodyFatPercentage != null) {
                                info += " • ${latest.bodyFatPercentage}% BF"
                            }
                            binding.tvLatestWeightInfo.text = info

                            // BMI calculation
                            val bmi = calculateBmi(latest.bodyWeight)
                            if (bmi != null) {
                                val category = getBmiCategory(bmi)
                                binding.chipBmi.text = "BMI: ${"%.1f".format(bmi)} — $category"
                                binding.chipBmi.setChipBackgroundColorResource(android.R.color.transparent)
                                binding.chipBmi.setTextColor(getBmiChipColor(bmi))
                                binding.chipBmi.chipStrokeColor = android.content.res.ColorStateList.valueOf(getBmiChipColor(bmi))
                                binding.chipBmi.chipStrokeWidth = 2f
                                binding.chipBmi.visibility = View.VISIBLE
                            } else {
                                binding.chipBmi.visibility = View.GONE
                            }
                        } else {
                            binding.tvLatestWeightValue.text = "—"
                            binding.tvLatestWeightInfo.text = "No weight recorded"
                            binding.chipBmi.visibility = View.GONE
                        }
                    }
                }

                launch {
                    viewModel.recentWeights.collect { list ->
                        adapter.submitList(list)
                        if (list.isEmpty()) {
                            binding.emptyView.visibility = View.VISIBLE
                            binding.rvRecentWeights.visibility = View.GONE
                        } else {
                            binding.emptyView.visibility = View.GONE
                            binding.rvRecentWeights.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
