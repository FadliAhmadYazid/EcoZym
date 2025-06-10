package com.ecozym.wastemanagement.fragments.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecozym.wastemanagement.adapters.TopContributorsAdapter
import com.ecozym.wastemanagement.databinding.FragmentReportAdminBinding
import com.ecozym.wastemanagement.viewmodels.ReportViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class ReportAdminFragment : Fragment() {

    private var _binding: FragmentReportAdminBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ReportViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var contributorsAdapter: TopContributorsAdapter
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedWasteType = "All"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[ReportViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        loadReportData()
    }

    private fun setupRecyclerView() {
        contributorsAdapter = TopContributorsAdapter()

        binding.rvTopContributors.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = contributorsAdapter
        }
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
        viewModel.adminReportData.observe(viewLifecycleOwner) { report ->
            binding.tvWasteAmount.text = "${report.totalWaste} kg"
            binding.tvEnzymeAmount.text = "${report.totalEnzyme} L"
            binding.tvProcessingTime.text = "${report.avgProcessingTime} days"
            binding.tvCompaniesCount.text = report.totalCompanies.toString()
            binding.tvMonthlyGrowth.text = "${report.monthlyGrowth}%"
            binding.tvAverageLoadSize.text = "${report.avgLoadSize} kg"

            contributorsAdapter.submitList(report.topContributors)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: Show loading indicator
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
        viewModel.loadAdminReport(selectedMonth, selectedYear, selectedWasteType)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
