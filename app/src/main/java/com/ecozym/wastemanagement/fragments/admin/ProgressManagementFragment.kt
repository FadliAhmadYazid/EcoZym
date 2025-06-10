package com.ecozym.wastemanagement.fragments.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecozym.wastemanagement.R
import com.ecozym.wastemanagement.adapters.ProgressAdminAdapter
import com.ecozym.wastemanagement.databinding.FragmentProgressManagementBinding
import com.ecozym.wastemanagement.models.ProgressTracking
import com.ecozym.wastemanagement.viewmodels.ProgressViewModel

class ProgressManagementFragment : Fragment() {

    private var _binding: FragmentProgressManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var progressViewModel: ProgressViewModel
    private lateinit var progressAdapter: ProgressAdminAdapter

    private var currentFilter = "All Status"
    private var searchQuery = ""

    // Mock data
    private val mockProgressList = listOf(
        createMockProgress("Citrus Fresh Co.", "CitrusCycle", "#TR-2025-005", 1.2, "Scheduled", "Samuel", "S", "B 2253 CEX"),
        createMockProgress("Tropical Fruits Inc.", "BioPeel", "#TR-2025-004", 0.8, "In Transit", "Ahmad", "A", "B 1234 CD"),
        createMockProgress("Smoothie Factory", "FermaFruit", "#TR-2025-003", 1.5, "Processing", "Rudi", "R", "B 5678 GH"),
        createMockProgress("Green Juice Co.", "CitrusCycle", "#TR-2025-002", 2.1, "Returning", "Budi", "B", "B 9999 XY"),
        createMockProgress("Fresh Market Ltd.", "BioPeel", "#TR-2025-001", 0.9, "Delivered", "Sari", "S", "B 1111 AB")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressViewModel = ViewModelProvider(this)[ProgressViewModel::class.java]

        setupRecyclerView()
        setupSearchFunctionality()
        setupFilterChips()
        setupClickListeners()

        // Initialize with all data
        filterAndDisplayData()
    }

    private fun setupRecyclerView() {
        progressAdapter = ProgressAdminAdapter(
            isPreview = false
        ) { progress ->
            // Handle item click
            Toast.makeText(context, "Clicked: ${progress.companyName}", Toast.LENGTH_SHORT).show()
        }

        binding.rvProgressItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = progressAdapter
        }
    }

    private fun setupSearchFunctionality() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString()?.lowercase() ?: ""
                filterAndDisplayData()
            }
        })
    }

    private fun setupFilterChips() {
        val chips = listOf(
            binding.chipAllStatus to "All Status",
            binding.chipScheduled to "Scheduled",
            binding.chipInTransit to "In Transit",
            binding.chipProcessing to "Processing",
            binding.chipReturning to "Returning",
            binding.chipDelivered to "Delivered"
        )

        chips.forEach { (chip, filter) ->
            chip.setOnClickListener {
                updateChipSelection(chips, chip)
                currentFilter = filter
                filterAndDisplayData()
            }
        }
    }

    private fun updateChipSelection(chips: List<Pair<TextView, String>>, selectedChip: TextView) {
        chips.forEach { (chip, _) ->
            if (chip == selectedChip) {
                // Selected state
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                // Unselected state
                chip.setBackgroundResource(R.drawable.bg_chip_unselected)
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_gray))
            }
        }
    }

    private fun setupClickListeners() {
        // Update status buttons
        binding.btnUpdateStatus1.setOnClickListener {
            updateProgressStatus(0, "In Transit", "Start Transit →", "Mark Arrived →")
        }

        binding.btnUpdateStatus2.setOnClickListener {
            updateProgressStatus(1, "Processing", "Mark Arrived →", "Start Return →")
        }

        binding.btnUpdateStatus3.setOnClickListener {
            updateProgressStatus(2, "Returning", "Start Return →", "Mark Delivered →")
        }

        // Call buttons
        binding.btnCall1.setOnClickListener {
            makeCall("Samuel", "+62812345671")
        }
        binding.btnCall2.setOnClickListener {
            makeCall("Ahmad", "+62812345672")
        }
        binding.btnCall3.setOnClickListener {
            makeCall("Rudi", "+62812345673")
        }

        // Message buttons
        binding.btnMessage1.setOnClickListener {
            sendMessage("Samuel")
        }
        binding.btnMessage2.setOnClickListener {
            sendMessage("Ahmad")
        }
        binding.btnMessage3.setOnClickListener {
            sendMessage("Rudi")
        }
    }

    private fun updateProgressStatus(itemIndex: Int, newStatus: String, oldButtonText: String, newButtonText: String) {
        when (itemIndex) {
            0 -> {
                // Update status badge
                binding.tvStatus1.text = newStatus
                binding.tvStatus1.setBackgroundResource(getStatusBackground(newStatus))
                binding.tvStatus1.setTextColor(ContextCompat.getColor(requireContext(), getStatusTextColor(newStatus)))

                // Update button text
                binding.btnUpdateStatus1.text = newButtonText

                // Update progress stepper visual
                updateProgressStepper(binding.llProgressStepper1, newStatus)

                Toast.makeText(context, "Citrus Fresh Co. status updated to $newStatus", Toast.LENGTH_SHORT).show()
            }
            1 -> {
                // Update status badge for item 2
                binding.tvStatus2.text = newStatus
                binding.tvStatus2.setBackgroundResource(getStatusBackground(newStatus))
                binding.tvStatus2.setTextColor(ContextCompat.getColor(requireContext(), getStatusTextColor(newStatus)))

                // Update button text
                binding.btnUpdateStatus2.text = newButtonText

                // Update progress stepper visual
                updateProgressStepper(binding.llProgressStepper2, newStatus)

                Toast.makeText(context, "Tropical Fruits Inc. status updated to $newStatus", Toast.LENGTH_SHORT).show()
            }
            2 -> {
                // Update status badge for item 3
                binding.tvStatus3.text = newStatus
                binding.tvStatus3.setBackgroundResource(getStatusBackground(newStatus))
                binding.tvStatus3.setTextColor(ContextCompat.getColor(requireContext(), getStatusTextColor(newStatus)))

                // Update button text
                binding.btnUpdateStatus3.text = newButtonText

                // Update progress stepper visual
                updateProgressStepper(binding.llProgressStepper3, newStatus)

                Toast.makeText(context, "Smoothie Factory status updated to $newStatus", Toast.LENGTH_SHORT).show()
            }
        }

        // Refresh the filtered data
        filterAndDisplayData()
    }

    private fun updateProgressStepper(stepperLayout: ViewGroup, status: String) {
        updateStepperByStatus(stepperLayout, status)
    }

    private fun updateStepperByStatus(stepperLayout: ViewGroup, status: String) {
        val steps = listOf("Scheduled", "In Transit", "Processing", "Returning", "Delivered")
        val currentStepIndex = steps.indexOf(status)

        if (currentStepIndex == -1) return

        // Get all step circles and connecting lines
        val stepCircles = mutableListOf<View>()
        val stepTexts = mutableListOf<TextView>()
        val connectingLines = mutableListOf<View>()

        // Parse the stepper layout to find circles, texts, and lines
        for (i in 0 until stepperLayout.childCount) {
            val child = stepperLayout.getChildAt(i)

            if (child is LinearLayout) {
                // This is a step container
                val stepCircle = child.getChildAt(0) // ImageView circle
                val stepText = child.getChildAt(1) as? TextView // TextView label

                stepCircles.add(stepCircle)
                stepText?.let { stepTexts.add(it) }
            } else if (child is View && child.layoutParams.width == 0) {
                // This is a connecting line
                connectingLines.add(child)
            }
        }

        // Update circles and texts based on current status
        for (i in stepCircles.indices) {
            val circle = stepCircles[i]
            val text = stepTexts.getOrNull(i)

            if (i <= currentStepIndex) {
                // Active/Completed step
                circle.setBackgroundResource(R.drawable.bg_step_active)
                text?.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            } else {
                // Inactive step
                circle.setBackgroundResource(R.drawable.bg_step_inactive)
                text?.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            }
        }

        // Update connecting lines
        for (i in connectingLines.indices) {
            val line = connectingLines[i]

            if (i < currentStepIndex) {
                // Completed connection
                line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
            } else {
                // Inactive connection
                line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_gray))
            }
        }
    }

    private fun getStatusBackground(status: String): Int {
        return when (status) {
            "Scheduled" -> R.drawable.bg_light_green
            "In Transit" -> R.drawable.bg_status_yellow
            "Processing" -> R.drawable.bg_blue
            "Returning" -> R.drawable.bg_yellow
            "Delivered" -> R.drawable.bg_status_green
            else -> R.drawable.bg_light_green
        }
    }

    private fun getStatusTextColor(status: String): Int {
        return when (status) {
            "Scheduled" -> R.color.dark_green
            else -> R.color.white
        }
    }

    private fun makeCall(driverName: String, phoneNumber: String) {
        // Implement call functionality
        Toast.makeText(context, "Calling $driverName at $phoneNumber", Toast.LENGTH_SHORT).show()

        // In real implementation, you would use:
        // val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
        // startActivity(intent)
    }

    private fun sendMessage(driverName: String) {
        // Implement messaging functionality
        Toast.makeText(context, "Opening chat with $driverName", Toast.LENGTH_SHORT).show()

        // In real implementation, you would navigate to messaging screen
    }

    private fun filterAndDisplayData() {
        val filteredData = mockProgressList.filter { progress ->
            val matchesFilter = if (currentFilter == "All Status") {
                true
            } else {
                progress.status == currentFilter
            }

            val matchesSearch = if (searchQuery.isEmpty()) {
                true
            } else {
                progress.companyName.lowercase().contains(searchQuery) ||
                    progress.transportId.lowercase().contains(searchQuery) ||
                    progress.wasteType.lowercase().contains(searchQuery)
            }

            matchesFilter && matchesSearch
        }

        updateDisplayedItems(filteredData)

        // Show/hide empty state
        if (filteredData.isEmpty()) {
            binding.llProgressItems.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.tvEmptyState.text = if (searchQuery.isNotEmpty()) {
                "No results found for \"$searchQuery\""
            } else {
                "No $currentFilter items available"
            }
        } else {
            binding.llProgressItems.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
        }
    }

    private fun updateDisplayedItems(filteredData: List<ProgressTracking>) {
        // Show/hide cards based on filtered data
        val cards = listOf(
            binding.cardProgress1,
            binding.cardProgress2,
            binding.cardProgress3
        )

        cards.forEachIndexed { index, card ->
            if (index < filteredData.size) {
                card.visibility = View.VISIBLE
                // Update card data here if needed
            } else {
                card.visibility = View.GONE
            }
        }

        // If you want to use RecyclerView instead of static cards:
        // progressAdapter.submitList(filteredData)
        // binding.rvProgressItems.visibility = View.VISIBLE
        // binding.llProgressItems.visibility = View.GONE
    }

    private fun createMockProgress(
        companyName: String,
        wasteType: String,
        transportId: String,
        quantity: Double,
        status: String,
        driverName: String,
        driverInitial: String,
        licensePlate: String
    ): ProgressTracking {
        return ProgressTracking(
            id = transportId,
            companyName = companyName,
            wasteType = wasteType,
            transportId = transportId,
            quantity = quantity,
            status = status,
            driverName = driverName,
            vehicleType = "Truck",
            licensePlate = licensePlate,
            pickupDate = com.google.firebase.Timestamp.now(),
            estimatedArrival = com.google.firebase.Timestamp.now(),
            estimatedReturn = com.google.firebase.Timestamp.now(),
            createdAt = com.google.firebase.Timestamp.now(),
            updatedAt = com.google.firebase.Timestamp.now()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}