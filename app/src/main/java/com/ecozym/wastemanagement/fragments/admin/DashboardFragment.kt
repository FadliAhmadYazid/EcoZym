package com.ecozym.wastemanagement.fragments.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecozym.wastemanagement.adapters.ProgressAdminAdapter
import com.ecozym.wastemanagement.adapters.ProductionAdminAdapter
import com.ecozym.wastemanagement.adapters.NotificationAdapter
import com.ecozym.wastemanagement.adapters.TopContributorsAdapter
import com.ecozym.wastemanagement.databinding.FragmentDashboardAdminBinding
import com.ecozym.wastemanagement.models.Notification
import com.ecozym.wastemanagement.viewmodels.ProgressViewModel
import com.ecozym.wastemanagement.viewmodels.ProductionViewModel
import com.ecozym.wastemanagement.viewmodels.NotificationViewModel
import com.ecozym.wastemanagement.viewmodels.AdminReportViewModel
import com.example.wastewiseapp.ui.production.ProductionManagementFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardAdminBinding? = null
    private val binding get() = _binding!!

    private lateinit var progressViewModel: ProgressViewModel
    private lateinit var productionViewModel: ProductionViewModel
    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var adminReportViewModel: AdminReportViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var progressPreviewAdapter: ProgressAdminAdapter
    private lateinit var productionPreviewAdapter: ProductionAdminAdapter
    private lateinit var recentActivityAdapter: NotificationAdapter

    // Variables for filters
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedWasteType: String = "All"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        progressViewModel = ViewModelProvider(this)[ProgressViewModel::class.java]
        productionViewModel = ViewModelProvider(this)[ProductionViewModel::class.java]
        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]
        adminReportViewModel = ViewModelProvider(this)[AdminReportViewModel::class.java]

        setupRecyclerViews()
        setupSpinners()
        setupClickListeners()
        setupObservers()
        loadData()
    }

    private fun setupRecyclerViews() {
        // Setup Progress RecyclerView
        progressPreviewAdapter = ProgressAdminAdapter(
            isPreview = true
        ) { progress ->
            // Navigate to full progress management
            navigateToProgressManagement()
        }
        binding.rvProgressPreview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = progressPreviewAdapter
        }

        // Setup Production RecyclerView
        productionPreviewAdapter = ProductionAdminAdapter(
            isPreview = true
        ) { production ->
            // Navigate to full production management
            navigateToProductionManagement()
        }
        binding.rvProductionPreview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productionPreviewAdapter
        }

        // Setup Recent Activity RecyclerView with click handling
        recentActivityAdapter = NotificationAdapter { notification ->
            handleNotificationClick(notification)
        }
        binding.rvRecentActivity.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentActivityAdapter
        }
    }

    private fun setupSpinners() {
        // Setup Month Spinner
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter
        binding.spinnerMonth.setSelection(selectedMonth)

        // Setup Waste Type Spinner
        val wasteTypes = arrayOf("All Waste Types", "BioPeel", "CitrusCycle", "FermaFruit")
        val wasteTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, wasteTypes)
        wasteTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerWasteType.adapter = wasteTypeAdapter
    }

    private fun setupClickListeners() {
        // Navigation click listeners
        binding.tvSeeAllTransportation.setOnClickListener {
            navigateToProgressManagement()
        }

        binding.tvSeeAllProduction.setOnClickListener {
            navigateToProductionManagement()
        }

        binding.tvSeeAllReport.setOnClickListener {
            navigateToReportManagement()
        }

        binding.tvSeeAllActivity.setOnClickListener {
            navigateToNotifications()
        }

        // Spinner selection listeners
        binding.spinnerMonth.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMonth = position
                loadData()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        binding.spinnerWasteType.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val wasteTypes = arrayOf("All", "BioPeel", "CitrusCycle", "FermaFruit")
                selectedWasteType = wasteTypes[position]
                loadData()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun setupObservers() {
        adminReportViewModel.adminReportData.observe(viewLifecycleOwner) { report ->
            // Update dashboard stats
            binding.tvTotalWaste.text = "${report.totalWaste}t"
            binding.tvEcoEnzyme.text = "${report.totalEnzyme}t"
            binding.tvCompanies.text = report.totalCompanies.toString()

            // Update environmental impact section
            binding.tvWasteTotal.text = "${report.totalWaste} kg"
            binding.tvEnzymeTotal.text = "${report.totalEnzyme} L"
            binding.tvCO2Offset.text = "${report.co2Offset} kg"

            // Update preview lists (show only first few items)
            report.recentProgress?.let { progressList ->
                progressPreviewAdapter.submitList(progressList.take(3))
            }

            report.recentProduction?.let { productionList ->
                productionPreviewAdapter.submitList(productionList.take(3))
            }

            report.recentActivities?.let { activitiesList ->
                recentActivityAdapter.submitList(activitiesList.take(5))
            }
        }

        adminReportViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: Show/hide loading indicator
            // You can add a ProgressBar to your layout and control its visibility here
        }

        // Observe individual ViewModels for more detailed data
        progressViewModel.progressList.observe(viewLifecycleOwner) { progressList ->
            progressPreviewAdapter.submitList(progressList.take(3))
        }

        productionViewModel.productionList.observe(viewLifecycleOwner) { productionList ->
            productionPreviewAdapter.submitList(productionList.take(3))
        }

        notificationViewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            recentActivityAdapter.submitList(notifications.take(5))
        }
    }

    private fun loadData() {
        // Load data from all ViewModels
        adminReportViewModel.loadAdminReport(selectedMonth, selectedYear, selectedWasteType)
        progressViewModel.loadProgressPreview()
        productionViewModel.loadProductionPreview()
        notificationViewModel.loadRecentActivity()
    }

    // Navigation methods
    private fun navigateToProgressManagement() {
        parentFragmentManager.beginTransaction()
            .replace(com.ecozym.wastemanagement.R.id.fragmentContainer, ProgressManagementFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToProductionManagement() {
        parentFragmentManager.beginTransaction()
            .replace(com.ecozym.wastemanagement.R.id.fragmentContainer, ProductionManagementFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToReportManagement() {
        parentFragmentManager.beginTransaction()
            .replace(com.ecozym.wastemanagement.R.id.fragmentContainer, ReportAdminFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToNotifications() {
        parentFragmentManager.beginTransaction()
            .replace(com.ecozym.wastemanagement.R.id.fragmentContainer, NotificationAdminFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToWasteApproval() {
        parentFragmentManager.beginTransaction()
            .replace(com.ecozym.wastemanagement.R.id.fragmentContainer, WasteApprovalFragment())
            .addToBackStack(null)
            .commit()
    }

    // Enhanced notification click handling
    private fun handleNotificationClick(notification: Notification) {
        // Mark as read
        notificationViewModel.markAsRead(notification.id)

        // Navigate based on notification type and action
        when (notification.type) {
            "admin" -> {
                when (notification.data["action"]) {
                    "waste_approval" -> {
                        // Navigate to waste approval fragment
                        navigateToWasteApproval()
                    }
                    "progress_update" -> {
                        // Navigate to progress management
                        navigateToProgressManagement()
                    }
                    "production_update" -> {
                        // Navigate to production management
                        navigateToProductionManagement()
                    }
                    else -> {
                        // Default: navigate to full notifications
                        navigateToNotifications()
                    }
                }
            }
            else -> {
                // For other notification types, just navigate to notifications
                navigateToNotifications()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}