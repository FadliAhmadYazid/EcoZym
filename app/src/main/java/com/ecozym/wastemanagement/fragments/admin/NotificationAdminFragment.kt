package com.ecozym.wastemanagement.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecozym.wastemanagement.adapters.NotificationAdapter
import com.ecozym.wastemanagement.databinding.FragmentNotificationAdminBinding
import com.ecozym.wastemanagement.models.Notification
import com.ecozym.wastemanagement.viewmodels.NotificationViewModel
import com.example.wastewiseapp.ui.production.ProductionManagementFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import java.util.*

class NotificationAdminFragment : Fragment() {

    private var _binding: FragmentNotificationAdminBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotificationViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        setupRecyclerView()
        setupObservers()

        // Load mock data instead of real data for testing
        loadMockNotifications()
        // Uncomment this line when you want to use real data
        // loadNotifications()
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter { notification ->
            // Handle notification click dengan navigation
            handleNotificationClick(notification)
        }

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
        }
    }

    private fun setupObservers() {
        viewModel.adminNotifications.observe(viewLifecycleOwner) { notifications ->
            if (notifications.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvNotifications.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvNotifications.visibility = View.VISIBLE
                notificationAdapter.submitList(notifications)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: Show loading indicator
        }
    }

    private fun loadNotifications() {
        viewModel.loadAdminNotifications()
    }

    // Add this method to create mock data
    private fun loadMockNotifications() {
        val mockNotifications = listOf(
            Notification(
                id = "1",
                title = "Fruit Heaven Ltd. has submitted a new registration application",
                message = "Waste Type: CitrusCycle, BioPeel • Est. Volume: 0.8t/week",
                type = "admin",
                isRead = false,
                createdAt = Timestamp(Date(System.currentTimeMillis() - 15 * 60 * 1000)), // 15 minutes ago
                data = mapOf(
                    "action" to "waste_approval",
                    "registrationId" to "REG-001"
                )
            ),
            Notification(
                id = "2",
                title = "Transport status updated for Tropical Fruits Inc.",
                message = "ID: TR-2025-042 • 0.8t BioPeel • Status: In Transit",
                type = "admin",
                isRead = false,
                createdAt = Timestamp(Date(System.currentTimeMillis() - 45 * 60 * 1000)), // 45 minutes ago
                data = mapOf(
                    "action" to "progress_update",
                    "transportId" to "TR-2025-042"
                )
            ),
            Notification(
                id = "3",
                title = "Batch #ECO-2025-075 has moved to next stage",
                message = "Citrus Enzyme • Stage: Fermentation (2/4)",
                type = "admin",
                isRead = true,
                createdAt = Timestamp(Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000)), // 2 hours ago
                data = mapOf(
                    "action" to "production_update",
                    "batchId" to "ECO-2025-075"
                )
            ),
            Notification(
                id = "4",
                title = "Batch #ECO-2025-073 completed successfully",
                message = "Ferma Enzyme • Final Yield: 1.2t • Quality: A+",
                type = "admin",
                isRead = true,
                createdAt = Timestamp(Date(System.currentTimeMillis() - 4 * 60 * 60 * 1000)), // 4 hours ago
                data = mapOf(
                    "action" to "production_update",
                    "batchId" to "ECO-2025-073"
                )
            ),
            Notification(
                id = "5",
                title = "New partnership request from GreenTech Solutions",
                message = "Industrial waste processing partnership proposal",
                type = "admin",
                isRead = false,
                createdAt = Timestamp(Date(System.currentTimeMillis() - 6 * 60 * 60 * 1000)), // 6 hours ago
                data = mapOf(
                    "action" to "partner_request",
                    "partnerId" to "PARTNER-001"
                )
            )
        )

        // Hide loading and empty state, show RecyclerView
        binding.tvEmptyState.visibility = View.GONE
        binding.rvNotifications.visibility = View.VISIBLE

        // Submit mock data to adapter
        notificationAdapter.submitList(mockNotifications)
    }

    private fun handleNotificationClick(notification: Notification) {
        // Mark as read
        viewModel.markAsRead(notification.id)

        // Navigate based on notification type and action
        when (notification.type) {
            "admin" -> {
                when (notification.data["action"]) {
                    "waste_approval" -> {
                        // Navigate to waste approval fragment
                        navigateToWasteApproval(notification.data["registrationId"])
                    }
                    "progress_update" -> {
                        navigateToProgressManagement()
                    }
                    "production_update" -> {
                        navigateToProductionManagement()
                    }
                    "partner_request" -> {
                        navigateToPartnerManagement()
                    }
                    else -> {
                        showNotificationDetails(notification)
                    }
                }
            }
            else -> {
                showNotificationDetails(notification)
            }
        }
    }

    private fun navigateToWasteApproval(registrationId: String?) {
        val fragment = WasteApprovalFragment()

        // Pass registration ID if available to highlight specific item
        if (registrationId != null) {
            val bundle = Bundle()
            bundle.putString("highlight_registration_id", registrationId)
            fragment.arguments = bundle
        }

        parentFragmentManager.beginTransaction()
            .replace(com.ecozym.wastemanagement.R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

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

    private fun navigateToPartnerManagement() {
        parentFragmentManager.beginTransaction()
            .replace(com.ecozym.wastemanagement.R.id.fragmentContainer, PartnerManagementFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun showNotificationDetails(notification: Notification) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(notification.title)
            .setMessage(
                "${notification.message}\n\n" +
                    "Type: ${notification.type}\n" +
                    "Time: ${android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", notification.createdAt.toDate())}"
            )
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}