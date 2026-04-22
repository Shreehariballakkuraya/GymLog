package com.hari.gymlog.ui.progress

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.hari.gymlog.databinding.FragmentProgressBinding
import com.hari.gymlog.util.CsvExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProgressViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCharts()
        setupExportButton()
        observeData()
    }

    private fun setupCharts() {
        binding.chartWeight.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            axisRight.isEnabled = false
            legend.isEnabled = false
        }

        binding.chartActivity.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            axisRight.isEnabled = false
            legend.isEnabled = true
        }
    }

    private fun setupExportButton() {
        binding.btnExport.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val file = withContext(Dispatchers.IO) {
                        CsvExporter.exportAllData(requireContext())
                    }

                    val uri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.fileprovider",
                        file
                    )

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "GymLog Data Export")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    startActivity(Intent.createChooser(shareIntent, "Share GymLog Data"))
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                launch {
                    viewModel.weightData.collect { weights ->
                        if (weights.isNotEmpty()) {
                            val entries = ArrayList<Entry>()
                            weights.forEachIndexed { index, weightEntity ->
                                entries.add(Entry(index.toFloat(), weightEntity.bodyWeight.toFloat()))
                            }

                            val dataSet = LineDataSet(entries, "Body Weight (kg)").apply {
                                color = Color.parseColor("#1976D2")
                                lineWidth = 2.5f
                                circleRadius = 4f
                                setCircleColor(Color.parseColor("#1976D2"))
                                setDrawValues(false)
                                mode = LineDataSet.Mode.CUBIC_BEZIER
                            }
                            binding.chartWeight.data = LineData(dataSet)
                            binding.chartWeight.invalidate()
                        } else {
                            binding.chartWeight.clear()
                        }
                    }
                }

                launch {
                    viewModel.activeData.collect { activities ->
                        if (activities.isNotEmpty()) {
                            val activeMinEntries = ArrayList<BarEntry>()

                            activities.forEachIndexed { index, activity ->
                                activeMinEntries.add(BarEntry(index.toFloat(), activity.activeMinutes.toFloat()))
                            }

                            val minDataSet = BarDataSet(activeMinEntries, "Active Minutes").apply {
                                color = Color.parseColor("#388E3C")
                            }

                            binding.chartActivity.data = BarData(minDataSet)
                            binding.chartActivity.invalidate()
                        } else {
                            binding.chartActivity.clear()
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
