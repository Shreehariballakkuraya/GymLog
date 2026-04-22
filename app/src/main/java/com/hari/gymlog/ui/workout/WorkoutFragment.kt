package com.hari.gymlog.ui.workout

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import com.google.android.material.snackbar.Snackbar
import com.hari.gymlog.R
import com.hari.gymlog.databinding.FragmentWorkoutBinding
import com.hari.gymlog.util.DateUtils
import kotlinx.coroutines.launch

class WorkoutFragment : Fragment() {

    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkoutViewModel by activityViewModels()
    private lateinit var adapter: WorkoutAdapter

    private var countDownTimer: CountDownTimer? = null
    private var selectedTimerSeconds = 60L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDate.text = DateUtils.getTodayDisplayDate()

        setupRecyclerView()
        setupFab()
        setupRestTimer()
        setupTemplateButtons()
        observeWorkouts()
        observePrDetection()
    }

    private fun setupRecyclerView() {
        adapter = WorkoutAdapter { workout ->
            val dialog = WorkoutEntryDialogFragment(workoutToEdit = workout)
            dialog.show(parentFragmentManager, WorkoutEntryDialogFragment.TAG)
        }
        binding.rvWorkouts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWorkouts.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val workout = adapter.currentList[position]
                viewModel.deleteWorkout(workout)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvWorkouts)
    }

    private fun setupFab() {
        binding.fabAddWorkout.setOnClickListener {
            val dialog = WorkoutEntryDialogFragment()
            dialog.show(parentFragmentManager, WorkoutEntryDialogFragment.TAG)
        }
    }

    // ========== REST TIMER ==========
    private fun setupRestTimer() {
        // Timer chip selection
        binding.chip30s.setOnClickListener { selectedTimerSeconds = 30L }
        binding.chip60s.setOnClickListener { selectedTimerSeconds = 60L }
        binding.chip90s.setOnClickListener { selectedTimerSeconds = 90L }
        binding.chip120s.setOnClickListener { selectedTimerSeconds = 120L }

        binding.btnStartTimer.setOnClickListener {
            startTimer()
        }
    }

    private fun startTimer() {
        countDownTimer?.cancel()

        val totalMs = selectedTimerSeconds * 1000
        binding.progressTimer.max = totalMs.toInt()
        binding.progressTimer.progress = totalMs.toInt()

        binding.btnStartTimer.text = "Running..."
        binding.btnStartTimer.isEnabled = false

        countDownTimer = object : CountDownTimer(totalMs, 50) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                val mins = seconds / 60
                val secs = seconds % 60
                binding.tvTimerCountdown.text = String.format("%02d:%02d", mins, secs)
                binding.progressTimer.progress = millisUntilFinished.toInt()
            }

            override fun onFinish() {
                binding.tvTimerCountdown.text = "00:00"
                binding.progressTimer.progress = 0
                binding.btnStartTimer.text = "Start Rest"
                binding.btnStartTimer.isEnabled = true

                // Haptic vibration
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        val vibratorManager = requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        val vibrator = vibratorManager.defaultVibrator
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } catch (_: Exception) {}

                Snackbar.make(binding.root, "⏰ Rest period over! Get back to it!", Snackbar.LENGTH_SHORT).show()
            }
        }.start()
    }

    // ========== TEMPLATE BUTTONS ==========
    private fun setupTemplateButtons() {
        binding.btnSaveTemplate.setOnClickListener {
            val currentWorkouts = adapter.currentList
            if (currentWorkouts.isEmpty()) {
                Toast.makeText(requireContext(), "No workouts to save as template", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val editText = EditText(requireContext()).apply {
                hint = "e.g. Chest Day, Leg Day"
                setPadding(60, 40, 60, 20)
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Save as Template")
                .setMessage("Give this workout routine a name:")
                .setView(editText)
                .setPositiveButton("Save") { _, _ ->
                    val name = editText.text.toString().trim()
                    if (name.isNotEmpty()) {
                        viewModel.saveAsTemplate(name, currentWorkouts)
                        Toast.makeText(requireContext(), "Template '$name' saved!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnLoadTemplate.setOnClickListener {
            showLoadTemplateDialog()
        }
    }

    private fun showLoadTemplateDialog() {
        val templates = viewModel.templates.value
        if (templates.isEmpty()) {
            Toast.makeText(requireContext(), "No saved templates", Toast.LENGTH_SHORT).show()
            return
        }

        val names = templates.map { it.templateName }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Load Template")
            .setItems(names) { _, which ->
                viewModel.loadTemplate(templates[which])
                Toast.makeText(requireContext(), "Template '${names[which]}' loaded!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ========== OBSERVERS ==========
    private fun observeWorkouts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.workouts.collect { workoutsList ->
                    adapter.submitList(workoutsList)
                    if (workoutsList.isEmpty()) {
                        binding.emptyView.visibility = View.VISIBLE
                        binding.rvWorkouts.visibility = View.GONE
                        binding.cardRestTimer.visibility = View.GONE
                    } else {
                        binding.emptyView.visibility = View.GONE
                        binding.rvWorkouts.visibility = View.VISIBLE
                        binding.cardRestTimer.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun observePrDetection() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.prDetected.collect { message ->
                    if (message != null) {
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                        viewModel.clearPrMessage()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        countDownTimer?.cancel()
        super.onDestroyView()
        _binding = null
    }
}
