// HomeFragment.kt
package com.ecozym.wastemanagement.fragments.company

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ecozym.wastemanagement.databinding.FragmentHomeBinding
import com.ecozym.wastemanagement.viewmodels.CompanyViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CompanyViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        viewModel = ViewModelProvider(this)[CompanyViewModel::class.java]

        setupObservers()
        loadData()
    }

    private fun setupObservers() {
        // Observer untuk total waste
        viewModel.totalWaste.observe(viewLifecycleOwner) { total ->
            binding.tvTotalWasteAmount.text = "~${total.toInt()} kg"
            updateProgressBar(total)
        }

        // Observer untuk latest pickup
        viewModel.latestPickup.observe(viewLifecycleOwner) { pickup ->
            if (pickup != null) {
                binding.tvCompanyName.text = pickup.companyName

                // Format estimated arrival date
                pickup.estimatedArrival?.let { timestamp ->
                    val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                    val date = sdf.format(timestamp.toDate())
                    binding.tvEstimatedArrival.text = "Estimated Arrival: $date"
                } ?: run {
                    binding.tvEstimatedArrival.text = "Estimated Arrival: March 31, 2025"
                }

                binding.tvStatus.text = pickup.status

                // Update status background based on status
                updateStatusBackground(pickup.status)
            } else {
                binding.tvCompanyName.text = "No active pickup"
                binding.tvEstimatedArrival.text = ""
                binding.tvStatus.text = ""
            }
        }

        // Observer untuk active production
        viewModel.activeProduction.observe(viewLifecycleOwner) { production ->
            if (production != null) {
                binding.tvBatchId.text = "Batch ${production.batchId}"
                binding.tvFermentationStage.text = production.stage
                binding.progressCircular.progress = production.progress
                binding.tvProgressPercent.text = "${production.progress}%"
            } else {
                binding.tvBatchId.text = "No active batch"
                binding.tvFermentationStage.text = ""
                binding.progressCircular.progress = 0
                binding.tvProgressPercent.text = "0%"
            }
        }

        // Observer untuk company profile (untuk welcome message)
        viewModel.companyProfile.observe(viewLifecycleOwner) { company ->
            binding.tvWelcome.text = "Welcome, ${company.companyName}!"
        }
    }

    private fun loadData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModel.loadCompanyData(currentUser.uid)
            viewModel.loadCompanyProfile(currentUser.uid)
        } else {
            // Load dengan dummy user ID untuk testing
            val dummyUserId = "dummy_company_001"
            viewModel.loadCompanyData(dummyUserId)
            viewModel.loadCompanyProfile(dummyUserId)
        }
    }

    private fun updateProgressBar(total: Double) {
        // Calculate progress based on monthly target
        val monthlyTarget = 5000.0 // 5 tons target per month
        val progress = ((total / monthlyTarget) * 100).toInt().coerceAtMost(100)
        binding.progressWaste.progress = progress
    }

    private fun updateStatusBackground(status: String) {
        // You can add logic here to change background based on status
        // For example, different colors for "In Transit", "Completed", "Pending", etc.
        when (status.toLowerCase(Locale.getDefault())) {
            "in transit" -> {
                // Keep current background (yellow)
            }
            "completed" -> {
                // Could change to green background
            }
            "pending" -> {
                // Could change to orange background
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}