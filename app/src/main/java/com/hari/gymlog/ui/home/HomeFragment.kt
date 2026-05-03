package com.hari.gymlog.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.hari.gymlog.R
import com.hari.gymlog.databinding.FragmentHomeBinding
import com.hari.gymlog.util.DateUtils
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvGreeting.text = getGreeting()
        binding.tvDate.text = DateUtils.getTodayDisplayDate()

        setupClickListeners()
        observeUiState()
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good Morning, Champ! 💪"
            hour < 17 -> "Good Afternoon! Keep Going 🔥"
            else -> "Good Evening! Stay Strong 🌙"
        }
    }

    private fun setupClickListeners() {
        binding.cardWorkout.setOnClickListener {
            findNavController().navigate(R.id.workoutFragment)
        }
        binding.cardFood.setOnClickListener {
            findNavController().navigate(R.id.foodFragment)
        }
        binding.cardActivity.setOnClickListener {
            findNavController().navigate(R.id.activityFragment)
        }
        binding.cardWeight.setOnClickListener {
            findNavController().navigate(R.id.weightFragment)
        }
        binding.btnReminder.setOnClickListener {
            findNavController().navigate(R.id.reminderListFragment)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Workout Card
                    binding.tvWorkoutSummary.text = if (state.workoutCount == 1) {
                        "1 exercise logged"
                    } else {
                        "${state.workoutCount} exercises logged"
                    }

                    // Food Card
                    binding.tvFoodSummary.text = if (state.foodCount == 1) {
                        "1 meal logged"
                    } else {
                        "${state.foodCount} meals logged"
                    }

                    // Macro Bar
                    val totalMacros = state.totalProtein + state.totalCarbs + state.totalFat
                    if (totalMacros > 0) {
                        binding.macroBar.setMacros(state.totalProtein, state.totalCarbs, state.totalFat)
                        binding.llMacroLabels.visibility = View.VISIBLE
                        binding.tvProtein.text = "🔴 P: ${state.totalProtein}g"
                        binding.tvCarbs.text = "🟡 C: ${state.totalCarbs}g"
                        binding.tvFat.text = "🟢 F: ${state.totalFat}g"
                    } else {
                        binding.llMacroLabels.visibility = View.GONE
                    }

                    // Activity Card
                    binding.tvActivitySummary.text = "${state.steps} steps · ${state.activeMinutes} min active"

                    // Weight Card
                    binding.tvWeightSummary.text = if (state.latestWeight != null) {
                        "${state.latestWeight} kg"
                    } else {
                        "No weight logged yet"
                    }

                    // Streak Badge
                    val streakVal = state.weeklyDaysWorkedOut // Use weekly as streak proxy
                    binding.chipStreak.text = if (streakVal > 0) {
                        "🔥 $streakVal day streak"
                    } else {
                        "Start your streak!"
                    }

                    // Weekly Ring
                    binding.weeklyRing.setProgress(state.weeklyDaysWorkedOut)
                    binding.tvWeeklyProgress.text = "${state.weeklyDaysWorkedOut} of 7 days this week"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
