package com.hari.gymlog.ui.weight

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.hari.gymlog.data.db.entity.WeightEntity
import com.hari.gymlog.databinding.DialogWeightEntryBinding
import com.hari.gymlog.util.DateUtils

class WeightEntryDialogFragment(
    private val weightToEdit: WeightEntity? = null
) : DialogFragment() {

    private var _binding: DialogWeightEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeightViewModel by activityViewModels()

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
        _binding = DialogWeightEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etDate.setText(DateUtils.getCurrentDateForDb())

        if (weightToEdit != null) {
            binding.tvDialogTitle.text = "Edit Body Weight"
            binding.etDate.setText(weightToEdit.date)
            binding.etWeight.setText(weightToEdit.bodyWeight.toString())
            if (weightToEdit.bodyFatPercentage != null) {
                binding.etBodyFat.setText(weightToEdit.bodyFatPercentage.toString())
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            saveWeight()
        }
    }

    private fun saveWeight() {
        val dateStr = binding.etDate.text.toString().trim()
        val weightStr = binding.etWeight.text.toString().trim()
        val bodyFatStr = binding.etBodyFat.text.toString().trim()

        if (dateStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(requireContext(), "Date and Weight are required", Toast.LENGTH_SHORT).show()
            return
        }

        val weightVal = weightStr.toDoubleOrNull()
        if (weightVal == null) {
            Toast.makeText(requireContext(), "Invalid weight", Toast.LENGTH_SHORT).show()
            return
        }

        val bodyFat = bodyFatStr.toDoubleOrNull()

        if (weightToEdit != null) {
            val updated = weightToEdit.copy(
                date = dateStr,
                bodyWeight = weightVal,
                bodyFatPercentage = bodyFat
            )
            viewModel.updateWeight(updated)
        } else {
            val newEntry = WeightEntity(
                date = dateStr,
                bodyWeight = weightVal,
                bodyFatPercentage = bodyFat
            )
            viewModel.addWeight(newEntry)
        }

        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "WeightEntryDialog"
    }
}
