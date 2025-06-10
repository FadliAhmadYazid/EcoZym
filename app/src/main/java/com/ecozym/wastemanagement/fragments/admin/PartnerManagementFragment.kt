package com.ecozym.wastemanagement.fragments.admin

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecozym.wastemanagement.R
import com.ecozym.wastemanagement.adapters.PartnerPendingAdapter
import com.ecozym.wastemanagement.adapters.PartnerRegisteredAdapter
import com.ecozym.wastemanagement.databinding.FragmentPartnerManagementBinding
import com.ecozym.wastemanagement.databinding.DialogAddPartnerBinding
import com.ecozym.wastemanagement.viewmodels.PartnerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class PartnerManagementFragment : Fragment() {

    private var _binding: FragmentPartnerManagementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PartnerViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var pendingAdapter: PartnerPendingAdapter
    private lateinit var registeredAdapter: PartnerRegisteredAdapter
    private var currentTab = "Pending"
    private var currentFilter = "All Partner"

    private val TAG = "PartnerManagement"

    // Job untuk tracking search operations
    private var searchJob: Job? = null
    private var loadDataJob: Job? = null

    // Progress Dialog Helper
    private var progressDialog: androidx.appcompat.app.AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPartnerManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "PartnerManagementFragment created")

        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        setupSearchFilter()
        setupTabs()
        setupFilterChips()
        setupFAB()
        setupObservers()

        // Initialize with Pending tab setelah view sudah siap
        viewLifecycleOwner.lifecycleScope.launch {
            delay(100) // Small delay untuk memastikan view sudah ready
            if (isAdded && _binding != null) {
                selectTab("Pending")
                loadPartnerData()
            }
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView")

        pendingAdapter = PartnerPendingAdapter(
            onViewDetails = { partner ->
                if (isAdded && _binding != null) {
                    showPartnerDetails(partner)
                }
            },
            onApprove = { partner ->
                if (isAdded && _binding != null) {
                    Log.d(TAG, "Approving partner: ${partner.uid}")
                    viewModel.approvePartner(partner.uid)
                }
            },
            onReject = { partner ->
                if (isAdded && _binding != null) {
                    Log.d(TAG, "Rejecting partner: ${partner.uid}")
                    viewModel.rejectPartner(partner.uid)
                }
            }
        )

        registeredAdapter = PartnerRegisteredAdapter(
            onEdit = { partner ->
                if (isAdded && _binding != null) {
                    showEditPartnerDialog(partner)
                }
            },
            onDelete = { partner ->
                if (isAdded && _binding != null) {
                    showDeleteConfirmation(partner)
                }
            }
        )

        binding.rvPartnerList.layoutManager = LinearLayoutManager(context)
        binding.rvPartnerList.adapter = pendingAdapter // Default to pending

        Log.d(TAG, "RecyclerView setup complete")
    }

    private fun setupSearchFilter() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isAdded && _binding != null) {
                    Log.d(TAG, "Search query: $s")
                    filterPartnersWithDebounce(s.toString())
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupTabs() {
        binding.tabPending.setOnClickListener {
            if (isAdded && _binding != null) {
                Log.d(TAG, "Pending tab clicked")
                selectTab("Pending")
            }
        }
        binding.tabRegistered.setOnClickListener {
            if (isAdded && _binding != null) {
                Log.d(TAG, "Registered tab clicked")
                selectTab("Registered")
            }
        }
    }

    private fun setupFilterChips() {
        binding.chipAllPartner.setOnClickListener {
            if (isAdded && _binding != null) {
                Log.d(TAG, "All Partner filter selected")
                selectFilter("All Partner")
            }
        }
        binding.chipCompany.setOnClickListener {
            if (isAdded && _binding != null) {
                Log.d(TAG, "Company filter selected")
                selectFilter("Company")
            }
        }
        binding.chipAdmin.setOnClickListener {
            if (isAdded && _binding != null) {
                Log.d(TAG, "Admin filter selected")
                selectFilter("Admin")
            }
        }
        binding.chipDriver.setOnClickListener {
            if (isAdded && _binding != null) {
                Log.d(TAG, "Driver filter selected")
                selectFilter("Driver")
            }
        }
    }

    private fun setupFAB() {
        binding.fabAddPartner.setOnClickListener {
            if (isAdded && _binding != null) {
                showAddPartnerDialog()
            }
        }
    }

    private fun setupObservers() {
        Log.d(TAG, "Setting up observers")

        viewModel.partnerStats.observe(viewLifecycleOwner) { stats ->
            if (isAdded && _binding != null) {
                Log.d(TAG, "Partner stats updated: Pending=${stats.pendingCount}, Registered=${stats.registeredCount}, Admin=${stats.adminCount}")
                binding.tvPendingCount.text = stats.pendingCount.toString()
                binding.tvRegisteredCount.text = stats.registeredCount.toString()
                binding.tvAdminCount.text = stats.adminCount.toString()
            }
        }

        viewModel.pendingPartners.observe(viewLifecycleOwner) { partners ->
            if (isAdded && _binding != null && currentTab == "Pending") {
                Log.d(TAG, "Pending partners updated: ${partners.size} partners")
                updatePartnerList(partners.isEmpty())
                pendingAdapter.submitList(partners) {
                    if (isAdded && _binding != null) {
                        Log.d(TAG, "Pending adapter list updated")
                    }
                }
            }
        }

        viewModel.registeredPartners.observe(viewLifecycleOwner) { partners ->
            if (isAdded && _binding != null && currentTab == "Registered") {
                Log.d(TAG, "Registered partners updated: ${partners.size} partners")
                updatePartnerList(partners.isEmpty())
                registeredAdapter.submitList(partners) {
                    if (isAdded && _binding != null) {
                        Log.d(TAG, "Registered adapter list updated")
                    }
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isAdded && _binding != null) {
                Log.d(TAG, "Loading state: $isLoading")
                if (isLoading) {
                    binding.tvEmptyState.text = "Loading..."
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvPartnerList.visibility = View.GONE
                }
            }
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            if (isAdded && _binding != null) {
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Action completed successfully")
                        loadPartnerData() // Reload data after action
                        android.widget.Toast.makeText(context, "Action completed successfully", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Action failed", error)
                        android.widget.Toast.makeText(context, "Action failed: ${error.message}", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onLoading = {
                        Log.d(TAG, "Action in progress")
                    }
                )
            }
        }
    }

    private fun selectTab(tab: String) {
        if (!isAdded || _binding == null) return

        Log.d(TAG, "Selecting tab: $tab")
        currentTab = tab

        // Cancel ongoing operations when switching tabs
        cancelOngoingOperations()

        // Update tab visual state with new colors
        if (tab == "Pending") {
            binding.tabPending.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
            binding.tabRegistered.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        } else {
            binding.tabPending.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            binding.tabRegistered.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light))
        }

        // Switch adapter
        if (tab == "Pending") {
            binding.rvPartnerList.adapter = pendingAdapter
            binding.hsvFilterChips.visibility = View.GONE
            Log.d(TAG, "Switched to pending adapter")
        } else {
            binding.rvPartnerList.adapter = registeredAdapter
            binding.hsvFilterChips.visibility = View.VISIBLE
            Log.d(TAG, "Switched to registered adapter")
        }

        // Clear search when switching tabs
        binding.etSearch.setText("")

        // Load data for the selected tab with slight delay
        viewLifecycleOwner.lifecycleScope.launch {
            delay(100)
            if (isAdded && _binding != null) {
                loadPartnerData()
            }
        }
    }

    private fun selectFilter(filter: String) {
        if (!isAdded || _binding == null) return

        Log.d(TAG, "Selecting filter: $filter")
        currentFilter = filter

        // Update chip states with custom colors
        updateFilterChipColors(filter)

        if (currentTab == "Registered") {
            loadPartnerData()
        }
    }

    private fun updateFilterChipColors(selectedFilter: String) {
        if (!isAdded || _binding == null) return

        val context = requireContext()

        // Reset all chips to default state
        binding.chipAllPartner.isSelected = false
        binding.chipCompany.isSelected = false
        binding.chipAdmin.isSelected = false
        binding.chipDriver.isSelected = false

        // Reset background colors to default
        binding.chipAllPartner.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        binding.chipCompany.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        binding.chipAdmin.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        binding.chipDriver.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))

        // Set text color to white for better contrast
        binding.chipAllPartner.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        binding.chipCompany.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        binding.chipAdmin.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        binding.chipDriver.setTextColor(ContextCompat.getColor(context, android.R.color.white))

        // Apply selected state and color
        when (selectedFilter) {
            "All Partner" -> {
                binding.chipAllPartner.isSelected = true
                binding.chipAllPartner.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
            }
            "Company" -> {
                binding.chipCompany.isSelected = true
                binding.chipCompany.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            }
            "Admin" -> {
                binding.chipAdmin.isSelected = true
                binding.chipAdmin.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_purple))
            }
            "Driver" -> {
                binding.chipDriver.isSelected = true
                binding.chipDriver.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark))
            }
        }
    }

    private fun filterPartnersWithDebounce(query: String) {
        if (!isAdded || _binding == null) return

        // Cancel previous search
        searchJob?.cancel()

        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(300) // Debounce delay
            if (isAdded && _binding != null) {
                filterPartners(query)
            }
        }
    }

    private fun filterPartners(query: String) {
        if (!isAdded || _binding == null) return

        Log.d(TAG, "Filtering partners with query: '$query' in tab: $currentTab")
        if (currentTab == "Pending") {
            viewModel.searchPendingPartners(query)
        } else {
            viewModel.searchRegisteredPartners(query)
        }
    }

    private fun loadPartnerData() {
        if (!isAdded || _binding == null) return

        Log.d(TAG, "Loading partner data - Tab: $currentTab, Filter: $currentFilter")

        // Cancel previous load operation
        loadDataJob?.cancel()

        loadDataJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Always load stats first
                viewModel.loadPartnerStats()

                when (currentTab) {
                    "Pending" -> {
                        Log.d(TAG, "Loading pending partners")
                        viewModel.loadPendingPartners()
                    }
                    "Registered" -> {
                        val filterType = when (currentFilter) {
                            "All Partner" -> null
                            "Company" -> "company"
                            "Admin" -> "admin"
                            "Driver" -> "driver"
                            else -> null
                        }
                        Log.d(TAG, "Loading registered partners with filter: $filterType")
                        viewModel.loadRegisteredPartners(filterType)
                    }
                }
            } catch (e: Exception) {
                if (isAdded && _binding != null) {
                    Log.e(TAG, "Error loading partner data", e)
                }
            }
        }
    }

    private fun updatePartnerList(isEmpty: Boolean) {
        if (!isAdded || _binding == null) return

        Log.d(TAG, "Updating partner list - isEmpty: $isEmpty")
        if (isEmpty) {
            binding.tvEmptyState.text = "No partners found"
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvPartnerList.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvPartnerList.visibility = View.VISIBLE
        }
    }

    // =================================================================================
    // PARTNER DETAILS IMPLEMENTATION
    // =================================================================================

    private fun showPartnerDetails(partner: com.ecozym.wastemanagement.models.User) {
        if (!isAdded || _binding == null) return

        try {
            Log.d(TAG, "Showing details for partner: ${partner.uid}")

            val dialog = Dialog(requireContext())
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_company_details, null)
            dialog.setContentView(dialogView)

            // Make dialog full width
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            setupPartnerDetailsDialog(dialog, dialogView, partner)
            dialog.show()

        } catch (e: Exception) {
            Log.e(TAG, "Error showing partner details", e)
            // Fallback to simple dialog if layout not found
            showSimplePartnerDetails(partner)
        }
    }

    private fun setupPartnerDetailsDialog(dialog: Dialog, dialogView: View, partner: com.ecozym.wastemanagement.models.User) {
        try {
            // Find views
            val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
            val tvPartnerType = dialogView.findViewById<TextView>(R.id.tvPartnerType)
            val tvName = dialogView.findViewById<TextView>(R.id.tvName)
            val tvEmail = dialogView.findViewById<TextView>(R.id.tvEmail)
            val tvPhone = dialogView.findViewById<TextView>(R.id.tvPhone)
            val tvAddress = dialogView.findViewById<TextView>(R.id.tvAddress)
            val tvStatus = dialogView.findViewById<TextView>(R.id.tvStatus)
            val tvCreatedAt = dialogView.findViewById<TextView>(R.id.tvCreatedAt)
            val btnClose = dialogView.findViewById<ImageView>(R.id.ivClose)
            val btnApprove = dialogView.findViewById<Button>(R.id.btnApprove)
            val btnReject = dialogView.findViewById<Button>(R.id.btnReject)

            // Role specific layouts
            val layoutIndustry = dialogView.findViewById<LinearLayout>(R.id.layoutIndustry)
            val tvIndustryType = dialogView.findViewById<TextView>(R.id.tvIndustryType)
            val layoutVehicle = dialogView.findViewById<LinearLayout>(R.id.layoutVehicle)
            val tvVehicleInfo = dialogView.findViewById<TextView>(R.id.tvVehicleInfo)
            val layoutAdminRole = dialogView.findViewById<LinearLayout>(R.id.layoutAdminRole)
            val tvAdminRole = dialogView.findViewById<TextView>(R.id.tvAdminRole)
            val layoutDocument = dialogView.findViewById<LinearLayout>(R.id.layoutDocument)
            val tvDocument = dialogView.findViewById<TextView>(R.id.tvDocument)

            // Set basic info
            tvTitle?.text = "Partner Details"
            tvPartnerType?.text = when (partner.role) {
                "company" -> "COMPANY"
                "admin" -> "ADMIN"
                "driver" -> "DRIVER"
                else -> "PARTNER"
            }

            // Set partner type badge color with updated colors
            tvPartnerType?.setBackgroundColor(when (partner.role) {
                "company" -> ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                "admin" -> ContextCompat.getColor(requireContext(), android.R.color.holo_purple)
                "driver" -> ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark)
                else -> ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
            })

            // Basic information
            tvEmail?.text = partner.email
            tvPhone?.text = partner.phone ?: "Not provided"
            tvAddress?.text = partner.address ?: "Not provided"
            tvStatus?.text = partner.status.uppercase()

            // Format created date
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            tvCreatedAt?.text = "Registered: ${dateFormat.format(partner.createdAt.toDate())}"

            // Hide all role-specific layouts first
            layoutIndustry?.visibility = View.GONE
            layoutVehicle?.visibility = View.GONE
            layoutAdminRole?.visibility = View.GONE
            layoutDocument?.visibility = View.GONE

            // Show role-specific information
            when (partner.role) {
                "company" -> {
                    tvName?.text = partner.companyName ?: "Unknown Company"

                    // Show industry if available
                    if (!partner.industryType.isNullOrBlank()) {
                        layoutIndustry?.visibility = View.VISIBLE
                        tvIndustryType?.text = partner.industryType
                    }

                    // Show document if available
                    if (!partner.documentUrl.isNullOrBlank()) {
                        layoutDocument?.visibility = View.VISIBLE
                        val fileName = partner.documentUrl.substringAfterLast("/")
                        tvDocument?.text = if (fileName.isNotBlank()) fileName else "View Document"

                        // Handle document click
                        tvDocument?.setOnClickListener {
                            openDocument(partner.documentUrl)
                        }
                    }
                }
                "admin" -> {
                    tvName?.text = partner.name ?: "Unknown Admin"

                    // Show admin role
                    layoutAdminRole?.visibility = View.VISIBLE
                    val roleText = when (partner.adminRole) {
                        "super_admin" -> "Super Admin"
                        "progress_admin" -> "Progress Admin"
                        "production_admin" -> "Production Admin"
                        else -> "Admin"
                    }
                    tvAdminRole?.text = roleText
                }
                "driver" -> {
                    tvName?.text = partner.name ?: "Unknown Driver"

                    // Show vehicle info if available
                    if (!partner.vehicleType.isNullOrBlank() || !partner.licensePlate.isNullOrBlank()) {
                        layoutVehicle?.visibility = View.VISIBLE
                        val vehicleInfo = buildString {
                            if (!partner.vehicleType.isNullOrBlank()) {
                                append(partner.vehicleType)
                            }
                            if (!partner.licensePlate.isNullOrBlank()) {
                                if (isNotEmpty()) append(" - ")
                                append(partner.licensePlate)
                            }
                            if (isEmpty()) append("Vehicle info not provided")
                        }
                        tvVehicleInfo?.text = vehicleInfo
                    }
                }
            }

            // Set status color with updated colors
            when (partner.status) {
                "pending" -> {
                    tvStatus?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark))
                    btnApprove?.visibility = View.VISIBLE
                    btnReject?.visibility = View.VISIBLE
                }
                "approved" -> {
                    tvStatus?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                    btnApprove?.visibility = View.GONE
                    btnReject?.visibility = View.GONE
                }
                "rejected" -> {
                    tvStatus?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                    btnApprove?.visibility = View.VISIBLE
                    btnReject?.visibility = View.GONE
                }
            }

            // Set click listeners
            btnClose?.setOnClickListener {
                dialog.dismiss()
            }

            btnApprove?.setOnClickListener {
                dialog.dismiss()
                viewModel.approvePartner(partner.uid)
            }

            btnReject?.setOnClickListener {
                dialog.dismiss()
                showRejectConfirmation(partner)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up partner details dialog", e)
            dialog.dismiss()
            showSimplePartnerDetails(partner)
        }
    }

    private fun showSimplePartnerDetails(partner: com.ecozym.wastemanagement.models.User) {
        try {
            val details = buildString {
                append("Partner Details\n\n")
                append("Type: ${partner.role.uppercase()}\n")
                append("Name: ${partner.companyName ?: partner.name ?: "Unknown"}\n")
                append("Email: ${partner.email}\n")
                append("Phone: ${partner.phone ?: "Not provided"}\n")
                append("Address: ${partner.address ?: "Not provided"}\n")
                append("Status: ${partner.status.uppercase()}\n")

                when (partner.role) {
                    "company" -> {
                        append("\nCompany Information:\n")
                        append("Industry: ${partner.industryType ?: "Not specified"}\n")
                        if (!partner.documentUrl.isNullOrBlank()) {
                            append("Document: Available\n")
                        }
                    }
                    "driver" -> {
                        append("\nDriver Information:\n")
                        append("Vehicle: ${partner.vehicleType ?: "Not specified"}\n")
                        append("License Plate: ${partner.licensePlate ?: "Not provided"}\n")
                        append("Driver License: ${partner.driverLicense ?: "Not provided"}\n")
                    }
                    "admin" -> {
                        append("\nAdmin Information:\n")
                        val roleText = when (partner.adminRole) {
                            "super_admin" -> "Super Admin"
                            "progress_admin" -> "Progress Admin"
                            "production_admin" -> "Production Admin"
                            else -> "Admin"
                        }
                        append("Role: $roleText\n")
                    }
                }
            }

            val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Partner Details")
                .setMessage(details)
                .setPositiveButton("Close", null)

            // Add document view button if available
            if (!partner.documentUrl.isNullOrBlank()) {
                dialogBuilder.setNeutralButton("View Document") { _, _ ->
                    openDocument(partner.documentUrl)
                }
            }

            // Add action buttons if pending
            if (partner.status == "pending") {
                dialogBuilder.setNegativeButton("Reject") { _, _ ->
                    showRejectConfirmation(partner)
                }
                dialogBuilder.setPositiveButton("Approve") { _, _ ->
                    viewModel.approvePartner(partner.uid)
                }
            }

            dialogBuilder.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing simple partner details", e)
            android.widget.Toast.makeText(context, "Error showing partner details", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // =================================================================================
    // DOCUMENT VIEWER IMPLEMENTATION
    // =================================================================================

    private fun openDocument(documentUrl: String?) {
        if (documentUrl.isNullOrBlank()) {
            android.widget.Toast.makeText(context, "Document not available", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Opening document: $documentUrl")

        // Check if it's a Cloudinary URL
        if (documentUrl.contains("cloudinary.com")) {
            downloadAndOpenCloudinaryDocument(documentUrl)
        } else {
            // Handle Firebase Storage or other URLs
            downloadAndOpenFirebaseDocument(documentUrl)
        }
    }

    private fun downloadAndOpenCloudinaryDocument(cloudinaryUrl: String) {
        if (!isAdded || _binding == null) return

        showProgressDialog("Loading document...")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Get file name and extension
                val fileName = cloudinaryUrl.substringAfterLast("/")
                val fileExtension = fileName.substringAfterLast(".").lowercase()

                Log.d(TAG, "Downloading from Cloudinary: $fileName")

                // Download file using coroutines
                val file = downloadFileFromUrl(cloudinaryUrl, fileName)

                hideProgressDialog()

                if (file != null && file.exists()) {
                    openLocalFile(file, fileExtension)
                } else {
                    android.widget.Toast.makeText(context, "Failed to download document", android.widget.Toast.LENGTH_SHORT).show()
                    showDocumentAlternatives(cloudinaryUrl, fileName)
                }

            } catch (e: Exception) {
                hideProgressDialog()
                Log.e(TAG, "Error downloading Cloudinary document", e)
                android.widget.Toast.makeText(context, "Error loading document: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                showDocumentAlternatives(cloudinaryUrl, cloudinaryUrl.substringAfterLast("/"))
            }
        }
    }

    private fun downloadAndOpenFirebaseDocument(firebaseUrl: String) {
        if (!isAdded || _binding == null) return

        showProgressDialog("Loading document...")

        try {
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(firebaseUrl)
            val fileName = storageRef.name
            val fileExtension = fileName.substringAfterLast(".").lowercase()

            Log.d(TAG, "Downloading from Firebase: $fileName")

            // Create local file
            val localFile = File(requireContext().cacheDir, fileName)

            storageRef.getFile(localFile)
                .addOnSuccessListener {
                    hideProgressDialog()
                    Log.d(TAG, "Firebase download successful: ${localFile.absolutePath}")
                    openLocalFile(localFile, fileExtension)
                }
                .addOnFailureListener { exception ->
                    hideProgressDialog()
                    Log.e(TAG, "Firebase download failed", exception)
                    android.widget.Toast.makeText(context, "Failed to load document", android.widget.Toast.LENGTH_SHORT).show()
                    showDocumentAlternatives(firebaseUrl, fileName)
                }

        } catch (e: Exception) {
            hideProgressDialog()
            Log.e(TAG, "Error with Firebase document", e)
            android.widget.Toast.makeText(context, "Error loading document", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun downloadFileFromUrl(url: String, fileName: String): File? {
        return try {
            withContext(Dispatchers.IO) {
                val connection = java.net.URL(url).openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 30000

                val inputStream = connection.getInputStream()
                val file = File(requireContext().cacheDir, fileName)
                val outputStream = FileOutputStream(file)

                inputStream.copyTo(outputStream)
                outputStream.close()
                inputStream.close()

                Log.d(TAG, "File downloaded successfully: ${file.absolutePath}, Size: ${file.length()}")
                file
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            null
        }
    }

    private fun openLocalFile(file: File, fileExtension: String) {
        if (!isAdded || _binding == null) return

        try {
            when (fileExtension) {
                "pdf" -> openPdfDocument(file)
                "jpg", "jpeg", "png", "gif", "webp" -> openImageDocument(file)
                "doc", "docx" -> openDocumentWithIntent(file, "application/msword")
                "txt" -> openTextDocument(file)
                else -> openDocumentWithIntent(file, "*/*")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening local file", e)
            android.widget.Toast.makeText(context, "Cannot open document type: $fileExtension", android.widget.Toast.LENGTH_SHORT).show()
            showDocumentInfo(file)
        }
    }

    private fun openPdfDocument(file: File) {
        try {
            // Method 1: Try to open with external PDF viewer
            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                // Method 2: Show in WebView
                showPdfInWebView(file)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error opening PDF", e)
            showPdfInWebView(file)
        }
    }

    private fun showPdfInWebView(file: File) {
        if (!isAdded || _binding == null) return

        try {
            val dialog = Dialog(requireContext())
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pdf_viewer, null)
            dialog.setContentView(dialogView)

            // Make dialog full screen
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            val webView = dialogView.findViewById<android.webkit.WebView>(R.id.webView)
            val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
            val btnClose = dialogView.findViewById<ImageView>(R.id.ivClose)
            val tvFileName = dialogView.findViewById<TextView>(R.id.tvFileName)
            val btnDownload = dialogView.findViewById<Button>(R.id.btnDownload)
            val btnShare = dialogView.findViewById<Button>(R.id.btnShare)

            tvFileName?.text = file.name

            // Configure WebView for PDF
            webView?.apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.allowFileAccess = true

                webViewClient = object : android.webkit.WebViewClient() {
                    override fun onPageStarted(view: android.webkit.WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        progressBar?.visibility = View.VISIBLE
                    }

                    override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                        progressBar?.visibility = View.GONE
                    }

                    override fun onReceivedError(view: android.webkit.WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                        progressBar?.visibility = View.GONE
                        android.widget.Toast.makeText(context, "Cannot display PDF in WebView", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }

                // Load PDF file directly
                loadUrl("file://${file.absolutePath}")
            }

            btnClose?.setOnClickListener { dialog.dismiss() }
            btnDownload?.setOnClickListener {
                copyFileToDownloads(file)
                android.widget.Toast.makeText(context, "PDF saved to Downloads", android.widget.Toast.LENGTH_SHORT).show()
            }
            btnShare?.setOnClickListener {
                shareFile(file)
            }

            dialog.show()

        } catch (e: Exception) {
            Log.e(TAG, "Error showing PDF in WebView", e)
            showDocumentInfo(file)
        }
    }

    private fun openImageDocument(file: File) {
        try {
            showImagePreviewFromFile(file)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening image", e)
            showDocumentInfo(file)
        }
    }

    private fun showImagePreviewFromFile(file: File) {
        if (!isAdded || _binding == null) return

        try {
            val dialog = Dialog(requireContext())
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_image_preview, null)
            dialog.setContentView(dialogView)

            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            val imageView = dialogView.findViewById<ImageView>(R.id.ivPreview)
            val tvFileName = dialogView.findViewById<TextView>(R.id.tvFileName)
            val btnClose = dialogView.findViewById<ImageView>(R.id.ivClose)
            val btnDownload = dialogView.findViewById<Button>(R.id.btnDownload)

            tvFileName?.text = file.name

            // Load image from file
            val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap != null) {
                imageView?.setImageBitmap(bitmap)
            } else {
                android.widget.Toast.makeText(context, "Cannot load image", android.widget.Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return
            }

            btnClose?.setOnClickListener { dialog.dismiss() }
            btnDownload?.setOnClickListener {
                copyFileToDownloads(file)
                android.widget.Toast.makeText(context, "Image saved to Downloads", android.widget.Toast.LENGTH_SHORT).show()
            }

            dialog.show()

        } catch (e: Exception) {
            Log.e(TAG, "Error showing image preview", e)
            android.widget.Toast.makeText(context, "Cannot preview image", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun openTextDocument(file: File) {
        try {
            val content = file.readText()

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(file.name)
                .setMessage(content)
                .setPositiveButton("Close", null)
                .setNeutralButton("Save to Downloads") { _, _ ->
                    copyFileToDownloads(file)
                }
                .show()

        } catch (e: Exception) {
            Log.e(TAG, "Error reading text file", e)
            openDocumentWithIntent(file, "text/plain")
        }
    }

    private fun openDocumentWithIntent(file: File, mimeType: String) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                android.widget.Toast.makeText(context, "No app available to open this document", android.widget.Toast.LENGTH_SHORT).show()
                showDocumentInfo(file)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error opening document with intent", e)
            showDocumentInfo(file)
        }
    }

    private fun copyFileToDownloads(file: File) {
        try {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val destFile = File(downloadsDir, file.name)
            file.copyTo(destFile, overwrite = true)

            // Notify media scanner
            val intent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = android.net.Uri.fromFile(destFile)
            requireContext().sendBroadcast(intent)

            android.widget.Toast.makeText(context, "File saved to Downloads: ${file.name}", android.widget.Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error copying file to downloads", e)
            android.widget.Toast.makeText(context, "Failed to save file", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareFile(file: File) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_TEXT, "Partner Document: ${file.name}")
                flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            startActivity(android.content.Intent.createChooser(intent, "Share Document"))
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing file", e)
            android.widget.Toast.makeText(context, "Cannot share file", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDocumentInfo(file: File) {
        val info = buildString {
            append("File: ${file.name}\n")
            append("Size: ${android.text.format.Formatter.formatFileSize(requireContext(), file.length())}\n")
            append("Type: ${file.extension.uppercase()}\n")
            append("Location: ${file.absolutePath}")
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Document Information")
            .setMessage(info)
            .setPositiveButton("Close", null)
            .setNeutralButton("Save to Downloads") { _, _ ->
                copyFileToDownloads(file)
            }
            .setNegativeButton("Share") { _, _ ->
                shareFile(file)
            }
            .show()
    }

    private fun showDocumentAlternatives(originalUrl: String, fileName: String) {
        val message = "Cannot open document directly. Choose an alternative:"

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Document Access")
            .setMessage(message)
            .setPositiveButton("Copy Link") { _, _ ->
                copyLinkToClipboard(originalUrl)
            }
            .setNeutralButton("Share Link") { _, _ ->
                shareLink(originalUrl, fileName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareLink(url: String, fileName: String) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, "Document: $fileName\n$url")
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Partner Document: $fileName")
            }
            startActivity(android.content.Intent.createChooser(intent, "Share Document Link"))
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing link", e)
            copyLinkToClipboard(url)
        }
    }

    private fun copyLinkToClipboard(url: String) {
        try {
            val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Document Link", url)
            clipboard.setPrimaryClip(clip)
            android.widget.Toast.makeText(context, "Link copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error copying to clipboard", e)
        }
    }

    // Progress Dialog Helper
    private fun showProgressDialog(message: String) {
        if (!isAdded || _binding == null) return

        try {
            hideProgressDialog()

            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            val inflater = LayoutInflater.from(requireContext())
            val view = inflater.inflate(android.R.layout.simple_list_item_1, null)

            val textView = view.findViewById<TextView>(android.R.id.text1)
            textView.text = message
            textView.gravity = android.view.Gravity.CENTER

            builder.setView(view)
            builder.setCancelable(false)

            progressDialog = builder.create()
            progressDialog?.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing progress dialog", e)
        }
    }

    private fun hideProgressDialog() {
        try {
            progressDialog?.dismiss()
            progressDialog = null
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding progress dialog", e)
        }
    }

    private fun showRejectConfirmation(partner: com.ecozym.wastemanagement.models.User) {
        try {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Reject Partner")
                .setMessage("Are you sure you want to reject ${partner.companyName ?: partner.name}?")
                .setPositiveButton("Reject") { _, _ ->
                    if (isAdded && _binding != null) {
                        Log.d(TAG, "Rejecting partner: ${partner.uid}")
                        viewModel.rejectPartner(partner.uid)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing reject confirmation", e)
        }
    }

    // =================================================================================
    // ADD PARTNER DIALOG IMPLEMENTATION
    // =================================================================================

    private fun showAddPartnerDialog() {
        if (!isAdded || _binding == null) return

        Log.d(TAG, "Showing add partner dialog")

        val dialog = Dialog(requireContext())
        val dialogBinding = DialogAddPartnerBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        // Make dialog full width
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        setupAddPartnerDialog(dialogBinding, dialog)
        dialog.show()
    }

    private fun setupAddPartnerDialog(dialogBinding: DialogAddPartnerBinding, dialog: Dialog) {
        // Setup partner type spinner
        val partnerTypes = arrayOf("Company", "Admin", "Driver")
        val partnerTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, partnerTypes)
        partnerTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerPartnerType.adapter = partnerTypeAdapter

        // Setup industry type spinner
        val industryTypes = arrayOf("Food Processing", "Manufacturing", "Technology", "Healthcare", "Education", "Finance", "Other")
        val industryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, industryTypes)
        industryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerIndustryType.adapter = industryAdapter

        // Setup admin role spinner
        val adminRoles = arrayOf("Super Admin", "Progress Admin", "Production Admin")
        val adminRoleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, adminRoles)
        adminRoleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerRole.adapter = adminRoleAdapter

        // Handle partner type selection
        dialogBinding.spinnerPartnerType.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = partnerTypes[position]
                showFieldsForPartnerType(dialogBinding, selectedType)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        // Close button
        dialogBinding.ivClose.setOnClickListener {
            dialog.dismiss()
        }

        // Cancel button
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Save button
        dialogBinding.btnSave.setOnClickListener {
            savePartner(dialogBinding, dialog)
        }

        // Initial setup - show company fields by default
        showFieldsForPartnerType(dialogBinding, "Company")
    }

    private fun showFieldsForPartnerType(dialogBinding: DialogAddPartnerBinding, partnerType: String) {
        // Hide all specific fields first
        dialogBinding.layoutCompanyFields.visibility = View.GONE
        dialogBinding.layoutAdminFields.visibility = View.GONE
        dialogBinding.layoutDriverFields.visibility = View.GONE

        // Show fields based on partner type
        when (partnerType) {
            "Company" -> {
                dialogBinding.layoutCompanyFields.visibility = View.VISIBLE
            }
            "Admin" -> {
                dialogBinding.layoutAdminFields.visibility = View.VISIBLE
            }
            "Driver" -> {
                dialogBinding.layoutDriverFields.visibility = View.VISIBLE
            }
        }
    }

    private fun savePartner(dialogBinding: DialogAddPartnerBinding, dialog: Dialog) {
        // Get common fields
        val email = dialogBinding.etEmail.text.toString().trim()
        val phone = dialogBinding.etPhoneNumber.text.toString().trim()
        val password = dialogBinding.etPassword.text.toString().trim()
        val partnerType = dialogBinding.spinnerPartnerType.selectedItem.toString()

        // Validate common fields
        if (email.isEmpty()) {
            dialogBinding.etEmail.error = "Email is required"
            return
        }
        if (phone.isEmpty()) {
            dialogBinding.etPhoneNumber.error = "Phone number is required"
            return
        }
        if (password.isEmpty()) {
            dialogBinding.etPassword.error = "Password is required"
            return
        }

        // Create partner data map based on type
        val partnerData = mutableMapOf<String, Any>(
            "email" to email,
            "phone" to phone,
            "role" to partnerType.lowercase(),
            "status" to "approved" // Direct addition means approved
        )

        when (partnerType) {
            "Company" -> {
                val companyName = dialogBinding.etCompanyName.text.toString().trim()
                val industryType = dialogBinding.spinnerIndustryType.selectedItem.toString()
                val address = dialogBinding.etCompanyAddress.text.toString().trim()

                if (companyName.isEmpty()) {
                    dialogBinding.etCompanyName.error = "Company name is required"
                    return
                }
                if (address.isEmpty()) {
                    dialogBinding.etCompanyAddress.error = "Address is required"
                    return
                }

                partnerData.putAll(mapOf(
                    "companyName" to companyName,
                    "industryType" to industryType,
                    "address" to address
                ))
            }
            "Admin" -> {
                val adminName = dialogBinding.etAdminName.text.toString().trim()
                val adminRole = dialogBinding.spinnerRole.selectedItem.toString()

                if (adminName.isEmpty()) {
                    dialogBinding.etAdminName.error = "Admin name is required"
                    return
                }

                val roleValue = when (adminRole) {
                    "Super Admin" -> "super_admin"
                    "Progress Admin" -> "progress_admin"
                    "Production Admin" -> "production_admin"
                    else -> "admin"
                }

                partnerData.putAll(mapOf(
                    "name" to adminName,
                    "adminRole" to roleValue
                ))
            }
            "Driver" -> {
                val driverName = dialogBinding.etDriverName.text.toString().trim()
                val vehicleType = dialogBinding.etVehicleType.text.toString().trim()
                val licensePlate = dialogBinding.etLicensePlate.text.toString().trim()

                if (driverName.isEmpty()) {
                    dialogBinding.etDriverName.error = "Driver name is required"
                    return
                }
                if (vehicleType.isEmpty()) {
                    dialogBinding.etVehicleType.error = "Vehicle type is required"
                    return
                }
                if (licensePlate.isEmpty()) {
                    dialogBinding.etLicensePlate.error = "License plate is required"
                    return
                }

                partnerData.putAll(mapOf(
                    "name" to driverName,
                    "vehicleType" to vehicleType,
                    "licensePlate" to licensePlate
                ))
            }
        }

        // Save partner
        Log.d(TAG, "Saving partner with data: $partnerData")
        viewModel.addPartner(partnerData)
        dialog.dismiss()
    }

    private fun showEditPartnerDialog(partner: com.ecozym.wastemanagement.models.User) {
        if (!isAdded || _binding == null) return

        Log.d(TAG, "Showing edit dialog for partner: ${partner.uid}")
        // TODO: Implement edit partner dialog
        android.widget.Toast.makeText(context, "Edit ${partner.companyName ?: partner.name}", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmation(partner: com.ecozym.wastemanagement.models.User) {
        if (!isAdded || _binding == null) return

        Log.d(TAG, "Showing delete confirmation for partner: ${partner.uid}")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Partner")
            .setMessage("Are you sure you want to delete ${partner.companyName ?: partner.name}?")
            .setPositiveButton("Delete") { _, _ ->
                if (isAdded && _binding != null) {
                    Log.d(TAG, "Deleting partner: ${partner.uid}")
                    viewModel.deletePartner(partner.uid)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun cancelOngoingOperations() {
        searchJob?.cancel()
        loadDataJob?.cancel()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Fragment paused - cancelling operations")
        cancelOngoingOperations()
        hideProgressDialog()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "PartnerManagementFragment destroyed")
        cancelOngoingOperations()
        hideProgressDialog()

        // Clear adapters to prevent memory leaks
        binding.rvPartnerList.adapter = null

        _binding = null
    }
}