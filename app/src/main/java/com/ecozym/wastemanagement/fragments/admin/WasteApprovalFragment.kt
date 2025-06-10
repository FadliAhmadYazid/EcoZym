package com.ecozym.wastemanagement.fragments.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecozym.wastemanagement.adapters.WasteApprovalAdapter
import com.ecozym.wastemanagement.databinding.FragmentWasteApprovalBinding
import com.ecozym.wastemanagement.models.WasteRegistration
import com.ecozym.wastemanagement.viewmodels.WasteApprovalViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WasteApprovalFragment : Fragment() {

    private var _binding: FragmentWasteApprovalBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WasteApprovalViewModel by viewModels()
    private lateinit var wasteApprovalAdapter: WasteApprovalAdapter

    private val TAG = "WasteApprovalFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWasteApprovalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "🎯 WasteApprovalFragment onViewCreated")

        setupRecyclerView()
        setupObservers()
        setupSearchFunctionality()
        setupSwipeRefresh()

        // Debug current state
        viewModel.debugCurrentState()
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "🔧 Setting up RecyclerView")

        wasteApprovalAdapter = WasteApprovalAdapter(
            onApproveClick = { registration ->
                Log.d(TAG, "👍 Approve clicked for: ${registration.id}")
                approveRegistration(registration)
            },
            onRejectClick = { registration ->
                Log.d(TAG, "👎 Reject clicked for: ${registration.id}")
                showRejectDialog(registration)
            }
        )

        // Check if recyclerView exists in binding, use appropriate name
        binding.recyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = wasteApprovalAdapter
        } ?: run {
            // Alternative: if the RecyclerView has a different ID
            Log.e(TAG, "❌ RecyclerView not found in binding. Check your layout XML file.")
        }
    }

    private fun setupObservers() {
        Log.d(TAG, "👁️ Setting up StateFlow observers")

        // Observe pending registrations using StateFlow
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pendingRegistrations.collect { registrations ->
                Log.d(TAG, "📊 Received ${registrations.size} pending registrations")

                registrations.forEachIndexed { index, reg ->
                    Log.d(TAG, "📋 Registration $index: ${reg.wasteType} - ${reg.getDisplayCompanyName()}")
                }

                wasteApprovalAdapter.submitList(registrations)
                updateEmptyState(registrations.isEmpty())
            }
        }

        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                Log.d(TAG, "⏳ Loading state: $isLoading")

                // Check if swipeRefreshLayout exists
                binding.swipeRefreshLayout?.isRefreshing = isLoading

                // Show/hide progress indicator
                binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Observe errors
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Log.e(TAG, "❌ Error: $it")
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }

        // Observe approval results
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.approvalResult.collect { result ->
                result?.let {
                    Log.d(TAG, "✅ Approval result: $it")
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    viewModel.clearApprovalResult()
                }
            }
        }
    }

    private fun setupSearchFunctionality() {
        Log.d(TAG, "🔍 Setting up search functionality")

        // Check if search EditText exists
        binding.searchEditText?.addTextChangedListener { text ->
            val query = text.toString().trim()
            Log.d(TAG, "🔍 Search query: '$query'")

            if (query.isEmpty()) {
                // Show all pending registrations
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.pendingRegistrations.collect { registrations ->
                        wasteApprovalAdapter.submitList(registrations)
                    }
                }
            } else {
                // Filter current list
                filterRegistrations(query)
            }
        } ?: run {
            Log.w(TAG, "⚠️ Search EditText not found in binding")
        }
    }

    private fun filterRegistrations(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pendingRegistrations.collect { registrations ->
                val filteredList = registrations.filter { registration ->
                    registration.wasteType.contains(query, ignoreCase = true) ||
                        registration.getDisplayCompanyName().contains(query, ignoreCase = true) ||
                        registration.location.contains(query, ignoreCase = true)
                }

                Log.d(TAG, "🔍 Filtered ${registrations.size} -> ${filteredList.size} registrations")
                wasteApprovalAdapter.submitList(filteredList)
                updateEmptyState(filteredList.isEmpty())
            }
        }
    }

    private fun setupSwipeRefresh() {
        Log.d(TAG, "🔄 Setting up swipe refresh")

        binding.swipeRefreshLayout?.setOnRefreshListener {
            Log.d(TAG, "🔄 Swipe refresh triggered")
            viewModel.loadPendingRegistrations()
        } ?: run {
            Log.w(TAG, "⚠️ SwipeRefreshLayout not found in binding")
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        Log.d(TAG, "📝 Updating empty state: isEmpty = $isEmpty")

        if (isEmpty) {
            binding.emptyStateLayout?.visibility = View.VISIBLE
            binding.recyclerView?.visibility = View.GONE
        } else {
            binding.emptyStateLayout?.visibility = View.GONE
            binding.recyclerView?.visibility = View.VISIBLE
        }
    }

    private fun approveRegistration(registration: WasteRegistration) {
        Log.d(TAG, "👍 Approving registration: ${registration.id}")

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            viewModel.approveRegistration(registration.id, currentUser.uid)
        } else {
            Log.e(TAG, "❌ Current user is null")
            Toast.makeText(requireContext(), "Authentication error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRejectDialog(registration: WasteRegistration) {
        Log.d(TAG, "👎 Showing reject dialog for: ${registration.id}")

        // Simple implementation - you can enhance this with a proper dialog
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // For now, reject without reason. You can add a dialog for reason input
            viewModel.rejectRegistration(registration.id, currentUser.uid, "Rejected by admin")
        } else {
            Log.e(TAG, "❌ Current user is null")
            Toast.makeText(requireContext(), "Authentication error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "🔄 onResume - reloading data")
        viewModel.loadPendingRegistrations()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "🗑️ WasteApprovalFragment destroyed")
    }
}

// Extension function to help with migration
fun WasteRegistration.getDisplayCompanyName(): String {
    return if (companyName.isBlank()) {
        "Company ID: ${companyId.take(8)}..."
    } else {
        companyName
    }
}