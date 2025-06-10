package com.ecozym.wastemanagement.fragments.company

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ecozym.wastemanagement.databinding.FragmentReportBinding
import com.ecozym.wastemanagement.viewmodels.CompanyViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CompanyViewModel
    private lateinit var auth: FirebaseAuth
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedWasteType = "All"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[CompanyViewModel::class.java]

        setupClickListeners()
        setupObservers()
        loadReportData()
    }

    private fun setupClickListeners() {
        binding.btnMonthSelect.setOnClickListener {
            showMonthPicker()
        }

        binding.btnWasteTypeSelect.setOnClickListener {
            showWasteTypeSelector()
        }
    }

    private fun setupObservers() {
        viewModel.reportData.observe(viewLifecycleOwner) { report ->
            // Map the actual ReportData properties to the UI
            binding.tvEnzymeTotal.text = "${report.totalEnzymeProduced} L"
            binding.tvWasteTotal.text = "${report.totalWasteSubmitted} kg"
            binding.tvEnzymeAmount.text = "${report.totalEnzymeProduced} L"
            binding.tvWasteAmount.text = "${report.totalWasteProcessed} kg"

            // Update waste distribution percentages using wasteBreakdown
            updateWasteDistribution(report.wasteBreakdown)

            // Update progress bars
            updateProgressBars(report)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: Show loading indicator
        }
    }

    private fun updateWasteDistribution(wasteBreakdown: Map<String, Double>) {
        val bioPeelAmount = wasteBreakdown["BioPeel"] ?: 0.0
        val citrusCycleAmount = wasteBreakdown["CitrusCycle"] ?: 0.0
        val fermaFruitAmount = wasteBreakdown["FermaFruit"] ?: 0.0

        val total = bioPeelAmount + citrusCycleAmount + fermaFruitAmount

        if (total > 0) {
            binding.tvBioPeelPercent.text = "${((bioPeelAmount / total) * 100).toInt()}%"
            binding.tvCitrusCyclePercent.text = "${((citrusCycleAmount / total) * 100).toInt()}%"
            binding.tvFermaFruitPercent.text = "${((fermaFruitAmount / total) * 100).toInt()}%"
        } else {
            binding.tvBioPeelPercent.text = "0%"
            binding.tvCitrusCyclePercent.text = "0%"
            binding.tvFermaFruitPercent.text = "0%"
        }
    }

    private fun showMonthPicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, _ ->
                this.selectedYear = selectedYear
                this.selectedMonth = selectedMonth

                val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    .format(Calendar.getInstance().apply {
                        set(Calendar.YEAR, selectedYear)
                        set(Calendar.MONTH, selectedMonth)
                    }.time)

                binding.btnMonthSelect.text = monthName
                loadReportData()
            },
            year,
            month,
            1
        )

        datePickerDialog.show()
    }

    private fun showWasteTypeSelector() {
        val wasteTypes = arrayOf("All Waste Types", "BioPeel", "CitrusCycle", "FermaFruit")

        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Select Waste Type")
        builder.setItems(wasteTypes) { _, which ->
            selectedWasteType = if (which == 0) "All" else wasteTypes[which]
            binding.btnWasteTypeSelect.text = wasteTypes[which]
            loadReportData()
        }
        builder.show()
    }

    private fun loadReportData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            viewModel.loadReport(user.uid, selectedMonth, selectedYear, selectedWasteType)
        }
    }

    private fun updateProgressBars(report: com.ecozym.wastemanagement.models.ReportData) {
        val bioPeelAmount = report.wasteBreakdown["BioPeel"] ?: 0.0
        val citrusCycleAmount = report.wasteBreakdown["CitrusCycle"] ?: 0.0
        val fermaFruitAmount = report.wasteBreakdown["FermaFruit"] ?: 0.0

        // Calculate progress based on total amounts
        val maxProgress = maxOf(bioPeelAmount, citrusCycleAmount, fermaFruitAmount)

        if (maxProgress > 0) {
            binding.progressBioPeel.progress = ((bioPeelAmount / maxProgress) * 100).toInt()
            binding.progressCitrusCycle.progress = ((citrusCycleAmount / maxProgress) * 100).toInt()
            binding.progressFermaFruit.progress = ((fermaFruitAmount / maxProgress) * 100).toInt()
        } else {
            binding.progressBioPeel.progress = 0
            binding.progressCitrusCycle.progress = 0
            binding.progressFermaFruit.progress = 0
        }

        binding.tvBioPeelAmount.text = "${bioPeelAmount} L (BioPeel)"
        binding.tvCitrusCycleAmount.text = "${citrusCycleAmount} L (CitrusCycle)"
        binding.tvFermaFruitAmount.text = "${fermaFruitAmount} L (FermaFruit)"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}