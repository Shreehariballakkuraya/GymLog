package com.hari.gymlog.ui.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hari.gymlog.databinding.FragmentFoodBinding
import com.hari.gymlog.util.DateUtils
import kotlinx.coroutines.launch

class FoodFragment : Fragment() {

    private var _binding: FragmentFoodBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels to share state with dialog
    private val viewModel: FoodViewModel by activityViewModels()
    private lateinit var adapter: FoodAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDate.text = DateUtils.getTodayDisplayDate()

        setupRecyclerView()
        setupFab()
        observeFoods()
    }

    private fun setupRecyclerView() {
        // Pass click listener directly
        adapter = FoodAdapter { food ->
            val dialog = FoodEntryDialogFragment(foodToEdit = food)
            dialog.show(parentFragmentManager, FoodEntryDialogFragment.TAG)
        }
        binding.rvFood.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFood.adapter = adapter

        // Swipe to delete
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val food = adapter.currentList[position]
                viewModel.deleteFood(food)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvFood)
    }

    private fun setupFab() {
        binding.fabAddFood.setOnClickListener {
            val dialog = FoodEntryDialogFragment()
            dialog.show(parentFragmentManager, FoodEntryDialogFragment.TAG)
        }
    }

    private fun observeFoods() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.foods.collect { foodsList ->
                    adapter.submitList(foodsList)
                    if (foodsList.isEmpty()) {
                        binding.emptyView.visibility = View.VISIBLE
                        binding.rvFood.visibility = View.GONE
                    } else {
                        binding.emptyView.visibility = View.GONE
                        binding.rvFood.visibility = View.VISIBLE
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
