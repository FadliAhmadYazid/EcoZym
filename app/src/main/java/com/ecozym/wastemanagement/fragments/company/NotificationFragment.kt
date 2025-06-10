package com.ecozym.wastemanagement.fragments.company

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecozym.wastemanagement.adapters.NotificationAdapter
import com.ecozym.wastemanagement.databinding.FragmentNotificationBinding
import com.ecozym.wastemanagement.models.Notification
import com.ecozym.wastemanagement.viewmodels.NotificationViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotificationViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        setupRecyclerView()
        setupObservers()

        // Load dummy data for testing
        loadDummyData()
        // Uncomment this to load real data
        // loadNotifications()
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter { notification ->
            // Handle notification click
            viewModel.markAsRead(notification.id)
        }

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
        }
    }

    private fun setupObservers() {
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
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

    private fun loadDummyData() {
        val calendar = Calendar.getInstance()
        val dummyNotifications = listOf(
            // Today
            Notification(
                id = "1",
                title = "CitrusCycle - 200kg",
                message = "Your waste is now being processed at WasteExchange Center",
                type = "processing",
                createdAt = Timestamp(calendar.apply { add(Calendar.HOUR, -2) }.time)
            ),
            Notification(
                id = "2",
                title = "CitrusCycle - 200kg",
                message = "Your waste has been picked up and is on the way to processing center",
                type = "transit",
                createdAt = Timestamp(calendar.apply { add(Calendar.HOUR, -3) }.time)
            ),
            // Yesterday
            Notification(
                id = "3",
                title = "PaperCycle - 150kg",
                message = "Processed materials are being returned to your facility",
                type = "returning",
                createdAt = Timestamp(calendar.apply {
                    set(Calendar.DAY_OF_YEAR, get(Calendar.DAY_OF_YEAR) - 1)
                    set(Calendar.HOUR_OF_DAY, 15)
                    set(Calendar.MINUTE, 30)
                }.time)
            ),
            Notification(
                id = "4",
                title = "PlasticCycle - 80kg",
                message = "Your waste recycling process has been completed",
                type = "completed",
                createdAt = Timestamp(calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 15)
                }.time)
            ),
            // Last week
            Notification(
                id = "5",
                title = "GlassCycle - 120kg",
                message = "Your waste recycling process has been completed",
                type = "completed",
                createdAt = Timestamp(calendar.apply {
                    set(Calendar.DAY_OF_YEAR, get(Calendar.DAY_OF_YEAR) - 5)
                }.time)
            ),
            Notification(
                id = "6",
                title = "GlassCycle - 120kg",
                message = "Your waste is now being processed at WasteExchange Center",
                type = "processing",
                createdAt = Timestamp(calendar.apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                }.time)
            )
        )

        // Directly set dummy data to adapter
        binding.tvEmptyState.visibility = View.GONE
        binding.rvNotifications.visibility = View.VISIBLE
        notificationAdapter.submitList(dummyNotifications)
    }

    private fun loadNotifications() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            viewModel.loadNotifications(user.uid)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}