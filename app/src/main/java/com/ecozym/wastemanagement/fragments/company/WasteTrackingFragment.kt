package com.ecozym.wastemanagement.fragments.company

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecozym.wastemanagement.R
import com.ecozym.wastemanagement.adapters.WasteTrackingAdapter
import com.ecozym.wastemanagement.databinding.FragmentWasteTrackingBinding
import com.ecozym.wastemanagement.viewmodels.WasteViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WasteTrackingFragment : Fragment() {

    private var _binding: FragmentWasteTrackingBinding? = null
    private val binding get() = _binding!!

    // FIX: Use Hilt's viewModels() delegate instead of ViewModelProvider
    private val viewModel: WasteViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var trackingAdapter: WasteTrackingAdapter
    private var currentTab = "ongoing"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWasteTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        // REMOVED: viewModel = ViewModelProvider(this)[WasteViewModel::class.java]
        // ViewModel is now injected via viewModels() delegate

        setupRecyclerView()
        setupClickListeners()
        setupObservers()

        // Initialize tab appearance
        selectTab("ongoing")
        loadTrackingData()
    }

    private fun setupRecyclerView() {
        trackingAdapter = WasteTrackingAdapter { tracking ->
            when (currentTab) {
                "ongoing" -> {
                    // Handle ongoing item click (call driver, message, etc.)
                    showDriverContactOptions(tracking.driverPhone)
                }
                "history" -> {
                    // Handle history item click (show details)
                    // TODO: Show tracking details
                }
            }
        }

        binding.rvWasteTracking.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = trackingAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnOngoing.setOnClickListener {
            selectTab("ongoing")
        }

        binding.btnHistory.setOnClickListener {
            selectTab("history")
        }
    }

    private fun setupObservers() {
        viewModel.trackingData.observe(viewLifecycleOwner) { trackingList ->
            if (trackingList.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvWasteTracking.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvWasteTracking.visibility = View.VISIBLE
                trackingAdapter.submitList(trackingList)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: Show loading indicator
        }
    }

    private fun selectTab(tab: String) {
        currentTab = tab

        // Update button states and colors
        if (tab == "ongoing") {
            // Ongoing active (green background, white text)
            binding.btnOngoing.apply {
                backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                strokeWidth = 0
            }

            // History inactive (white background, green text, green border)
            binding.btnHistory.apply {
                backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_green))
                strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.dark_green)
                strokeWidth = 1
            }
        } else {
            // History active (green background, white text)
            binding.btnHistory.apply {
                backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                strokeWidth = 0
            }

            // Ongoing inactive (white background, green text, green border)
            binding.btnOngoing.apply {
                backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_green))
                strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.dark_green)
                strokeWidth = 1
            }
        }

        loadTrackingData()
    }

    private fun loadTrackingData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            when (currentTab) {
                "ongoing" -> viewModel.loadOngoingTracking(user.uid)
                "history" -> viewModel.loadTrackingHistory(user.uid)
            }
        }
    }

    private fun showDriverContactOptions(phoneNumber: String?) {
        if (phoneNumber.isNullOrEmpty()) return

        val options = arrayOf("Call Driver", "Send Message")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Contact Driver")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> callDriver(phoneNumber)
                1 -> messageDriver(phoneNumber)
            }
        }
        builder.show()
    }

    private fun callDriver(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }

    private fun messageDriver(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", "Hello, I would like to inquire about my waste pickup status.")
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}