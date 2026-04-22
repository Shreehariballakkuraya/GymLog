package com.hari.gymlog.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hari.gymlog.data.db.entity.ActivityEntity
import com.hari.gymlog.databinding.FragmentActivityBinding
import com.hari.gymlog.util.DateUtils
import kotlinx.coroutines.launch

class ActivityFragment : Fragment() {

    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ActivityViewModel by activityViewModels()
    private var currentActivityData: ActivityEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDate.text = DateUtils.getTodayDisplayDate()

        setupFab()
        observeActivity()
    }

    private fun setupFab() {
        binding.fabEditActivity.setOnClickListener {
            val dialog = ActivityEntryDialogFragment(currentActivity = currentActivityData)
            dialog.show(parentFragmentManager, ActivityEntryDialogFragment.TAG)
        }
    }

    private fun observeActivity() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentActivity.collect { activity ->
                    currentActivityData = activity
                    if (activity != null) {
                        binding.tvSteps.text = activity.steps.toString()
                        binding.tvActiveMinutes.text = activity.activeMinutes.toString()
                        binding.fabEditActivity.text = "Edit Activity"
                    } else {
                        binding.tvSteps.text = "0"
                        binding.tvActiveMinutes.text = "0"
                        binding.fabEditActivity.text = "Log Activity"
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
