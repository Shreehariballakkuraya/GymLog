package com.hari.gymlog.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.hari.gymlog.data.db.entity.ActivityEntity
import com.hari.gymlog.databinding.DialogActivityEntryBinding
import com.hari.gymlog.util.DateUtils

class ActivityEntryDialogFragment(
    private val currentActivity: ActivityEntity? = null
) : DialogFragment() {

    private var _binding: DialogActivityEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ActivityViewModel by activityViewModels()

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
        _binding = DialogActivityEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (currentActivity != null) {
            binding.tvDialogTitle.text = "Update Activity"
            binding.etSteps.setText(currentActivity.steps.toString())
            binding.etActiveMinutes.setText(currentActivity.activeMinutes.toString())
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            saveActivity()
        }
    }

    private fun saveActivity() {
        val stepsStr = binding.etSteps.text.toString().trim()
        val minutesStr = binding.etActiveMinutes.text.toString().trim()

        if (stepsStr.isEmpty() || minutesStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val steps = stepsStr.toIntOrNull() ?: 0
        val activeMinutes = minutesStr.toIntOrNull() ?: 0

        val newActivity = if (currentActivity != null) {
            currentActivity.copy(steps = steps, activeMinutes = activeMinutes)
        } else {
            ActivityEntity(
                date = DateUtils.getCurrentDateForDb(),
                steps = steps,
                activeMinutes = activeMinutes
            )
        }

        viewModel.saveActivity(newActivity)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ActivityEntryDialog"
    }
}
