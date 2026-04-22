package com.hari.gymlog.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hari.gymlog.databinding.FragmentHistoryBinding
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendar()
        observeData()
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            viewModel.setSelectedDate(year, month, dayOfMonth)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.tvSelectedDateLabel.text = "Data for ${state.selectedDate}"

                    binding.tvHistoryWorkout.text = "${state.workoutsLogged} Workouts logged"
                    binding.tvHistoryFood.text = "${state.mealsLogged} Meals logged"
                    binding.tvHistoryActivity.text = "${state.steps} Steps • ${state.activeMinutes} Active Min"
                    
                    if (state.weight != null) {
                        binding.tvHistoryWeight.text = "${state.weight} kg Recorded"
                    } else {
                        binding.tvHistoryWeight.text = "No Weight Recorded"
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
